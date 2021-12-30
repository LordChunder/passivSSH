package org.awaiteddev.common.ssh

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class SSHClient(host: String, username: String, port: Int, onConnected: (Boolean) -> Unit) {
    private lateinit var session: Session

    init {
        buildClient(host, username, port, onConnected)
    }

    private fun buildClient(host: String, username: String, port: Int, onConnected: (Boolean) -> Unit) {
        Thread {
            try {
                session = Builder(host, username, port, "pvtKey").build()!!
                session.connect()
            } catch (e: JSchException) {
                onConnected.invoke(false)
                return@Thread
            }
            onConnected.invoke(true)
        }.start()
    }

    fun disconnect() {
        try {
            if (session.isConnected) session.disconnect()

        } catch (e: JSchException) {
            System.err.print(e)
        }
    }

    fun execute(command: List<String>, onResponse: (String) -> Unit) {
        if (command.isEmpty()) {
            print("SSH command is blank.")
            onResponse.invoke("ERROR (-1) Enter a valid command")
        }
        try {
            Thread {
                if (!session.isConnected) session.connect()
                val channel = session.openChannel("exec")
                command.forEach { (channel as ChannelExec).setCommand(it) }
                (channel as ChannelExec).setPty(false)
                val inputStream = BufferedReader(InputStreamReader(channel.getInputStream()))

                channel.connect()
                val tmp = CharArray(1024)
                val numTries = 5
                var receivedResponse = false
                while (numTries > 0) {
                    while (inputStream.ready()) {
                        val i: Int = inputStream.read(tmp, 0, 1024)
                        if (i < 0) break
                        val responseMsg = String(tmp, 0, i).replace("\n","");
                        onResponse.invoke(responseMsg)
                        receivedResponse = true
                    }
                    if (channel.isClosed()) {
                        val status = channel.getExitStatus()
                        if (status != 0) {
                            onResponse.invoke("ERROR ($status) Unknown Command")
                            receivedResponse = true
                        }
                        break
                    }
                    try {
                        Thread.sleep(1000)
                    } catch (e: Exception) {
                        println("EXCEPTION $e")
                    }
                }
                if (!receivedResponse) onResponse.invoke("")
                channel.disconnect()
            }.start()
        } catch (e: JSchException) {
            System.err.print(e)
        }
    }

    class Builder(private val host: String, private val username: String, private val port: Int, path: String) {

        private val privateKeyPath: Path
        lateinit var jschSession: Session

        init {
            this.privateKeyPath = Paths.get(path)
        }

        private fun validate() {
            if (port < 1) {
                throw IllegalArgumentException("Port number must start with 1.")
            }
        }

        fun build(): Session? {
            validate()
            val jsch = JSch()
            val session: Session?

            try {

                jsch.addIdentity(privateKeyPath.toString())

                session = jsch.getSession(username, host, port)
                session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password")

                val config = Properties()
                config["StrictHostKeyChecking"] = "no"

                session.setConfig(config)

            } catch (e: JSchException) {
                throw RuntimeException("Failed to create Jsch Session object.", e)
            }

            this.jschSession = session
            return session
        }

    }
}
