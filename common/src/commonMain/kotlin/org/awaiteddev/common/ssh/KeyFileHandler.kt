package org.awaiteddev.common.ssh

import java.security.PrivateKey

expect class KeyFileHandler {
    companion object {
        fun saveKeyToFile(name:String,rsaKey: String, pvtKey:PrivateKey):String
        fun saveKeyDataListToFile()

        fun readKeyDataFromFile()

    }
}