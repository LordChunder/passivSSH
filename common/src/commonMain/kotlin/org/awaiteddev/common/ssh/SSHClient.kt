package org.awaiteddev.common.ssh

import com.jcraft.jsch.*
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


object SSHClient {
    private var session: Session? = null
    val isConnected
        get() = session != null && session!!.isConnected
    private val shellOpen
        get() = channel != null && channel!!.isConnected
    private var channel: Channel? = null
    private val cmdQueue = ConcurrentLinkedQueue<String>()

    fun sendCommand(cmd: String) {
        if (shellOpen && isConnected)
            cmdQueue.add(cmd)
    }

    fun buildClient(host: String, username: String, port: Int, path: String, onConnected: (Boolean) -> Unit) {
        if (!isConnected)
            Thread {
                try {
                    session = Builder(host, username, port, path).build()
                    session?.connect()
                    onConnected.invoke(true)
                } catch (e: JSchException) {
                    onConnected.invoke(false)
                    return@Thread
                }
            }.start()
    }

    fun closeShell() {
        if (!shellOpen) return
        try {
            if (channel!!.isConnected)
                channel?.disconnect()
        } catch (e: JSchException) {
            System.err.print(e)
        }
    }


    fun disconnect() {
        if (!isConnected)
            try {
                session?.disconnect()
            } catch (e: JSchException) {
                e.printStackTrace()
            }
    }

    fun openShell(onResponse: (String) -> Unit) {
        if (!isConnected || shellOpen) return
        try {
            channel = session?.openChannel("shell") as ChannelShell
            val pip = PipedInputStream()
            val pop = PipedOutputStream(pip)
            val baos = ByteArrayOutputStream()
            channel?.setOutputStream(baos, true)
            channel?.setInputStream(pip, true)

            channel?.connect()

            var lastCmd = ""

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
                            Charset.forName("UTF-8").decode(
                                ByteBuffer.wrap(baos.toByteArray())
                            ).toString().split("\n").forEach {
                                if (it != lastCmd) onResponse.invoke(
                                    it
                                        .replace("\u001B\\[[;\\d]*m".toRegex(), "")
                                        .replace("\u001B\\[[;\\d]*[ -/]*[@-~]".toRegex(),"")

                                )
                            }
                            baos.reset()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (channel == null || channel!!.isClosed) {
                        print("Channel Closed")
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
