package org.awaiteddev.common.data

import org.awaiteddev.common.ssh.SSHClient
import java.util.concurrent.ConcurrentLinkedQueue

object AppDataManager {
    var keyList = ArrayList<KeyData>()
    var ssh: SSHClient? = null
    val cmdQueue = ConcurrentLinkedQueue<String>()
}