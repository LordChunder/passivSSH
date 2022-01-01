package org.awaiteddev.common.ssh

import com.jcraft.jsch.*
import org.awaiteddev.common.data.AppDataManager.cmdQueue
import org.awaiteddev.common.data.KeyData
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class SSHClient(host: String, username: String, port: Int, keyData: KeyData, onConnected: (Boolean) -> Unit) {
    private lateinit var session: Session
    var shellOpen = false
    private var channel: Channel? = null

    init {
        buildClient(host, username, port, keyData.privateKeyPath, onConnected)
    }

    private fun buildClient(host: String, username: String, port: Int, path: String, onConnected: (Boolean) -> Unit) {
        Thread {
            try {
                session = Builder(host, username, port, path).build()!!
                session.connect()
                onConnected.invoke(true)
            } catch (e: JSchException) {
                onConnected.invoke(false)
                return@Thread
            }
        }.start()
    }

    fun closeShell() {
        try {
            if (channel == null) return
            if (channel!!.isConnected)
                channel?.disconnect()
            shellOpen = false
        } catch (e: JSchException) {
            System.err.print(e)
        }
    }


    fun disconnect() {
        try {
            if (session.isConnected)
                session.disconnect()
        } catch (e: JSchException) {
            System.err.print(e)
        }
    }

    fun openShell(onResponse: (String) -> Unit) {
        try {
            channel = session.openChannel("shell") as ChannelShell
            val pip = PipedInputStream()
            val pop = PipedOutputStream(pip)
            val baos = ByteArrayOutputStream()
            channel!!.setOutputStream(baos, true)
            channel!!.setInputStream(pip, true)

            channel!!.connect()

            var lastCmd = ""
            shellOpen = channel!!.isConnected
            Thread {
                while (true) {
                    while (!cmdQueue.isEmpty()) {

                        try {
                            lastCmd = cmdQueue.remove()
                            if (lastCmd.lowercase().contains("ctrl-b")) {
                                lastCmd = lastCmd.replace("ctrl-b", "\u0002", true)
                            }
                            if (lastCmd.lowercase().contains("ctrl-d")) {
                                lastCmd = lastCmd.replace("ctrl-d", "\u0004", true)
                            }
                            lastCmd += "\r"
                            pop.write(lastCmd.toByteArray())
                            pop.flush()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (baos.toString() != "") {
                        try {
                            baos.toString(Charset.forName("UTF-8")).split("\n").forEach {
                                if (it != lastCmd) onResponse.invoke(it)
                            }
                            baos.reset()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (channel == null || channel!!.isClosed) {
                        print("Channel Closed")

                        shellOpen = false
                        break
                    }
                    try {
                        Thread.sleep(500)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.start()
        } catch (e: Exception) {

            e.printStackTrace()
        }

    }

    class Builder(private val host: String, private val username: String, private val port: Int, path: String) {

        private val privateKeyPath: Path
        private lateinit var jschSession: Session

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
                session.setConfig("PreferredAuthentications", "publickey")

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
