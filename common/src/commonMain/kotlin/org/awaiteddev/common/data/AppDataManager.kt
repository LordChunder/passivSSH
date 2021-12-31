package org.awaiteddev.common.data

import org.awaiteddev.common.ssh.SSHClient

object AppDataManager {
    var keyList = ArrayList<KeyData>()
    var ssh: SSHClient? = null
}