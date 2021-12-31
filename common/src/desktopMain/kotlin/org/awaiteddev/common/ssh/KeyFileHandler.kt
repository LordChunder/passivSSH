package org.awaiteddev.common.ssh

import org.awaiteddev.common.data.AppDataManager
import org.awaiteddev.common.data.KeyData
import org.bouncycastle.openssl.PEMWriter
import java.io.*
import java.security.PrivateKey

actual class KeyFileHandler {
    actual companion object {
        actual fun saveKeyToFile(name: String, rsaKey: String, pvtKey: PrivateKey): String {
            val privateKeyFile = File("data/keys/${name}.key")

            // Create files to store public and private key
            if (privateKeyFile.parentFile != null) {
                privateKeyFile.parentFile.mkdirs()
            }
            privateKeyFile.createNewFile()

            // Saving the Private key in a file
            val pemWriter = PEMWriter(FileWriter(privateKeyFile))
            pemWriter.writeObject(pvtKey)
            pemWriter.close()

            return privateKeyFile.path
        }

        actual fun saveKeyDataListToFile() {
            try {
                val keyDataFile = File("data/keys.dat")
                if (keyDataFile.parentFile != null) {
                    keyDataFile.parentFile.mkdirs()
                }
                keyDataFile.createNewFile()
                val fos = FileOutputStream(keyDataFile)
                val oos = ObjectOutputStream(fos)
                oos.writeObject(AppDataManager.keyList) // write MenuArray to ObjectOutputStream
                oos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        actual fun readKeyDataFromFile() {
            try {
                val readData = FileInputStream("data/keys.dat")
                val readStream = ObjectInputStream(readData)
                AppDataManager.keyList = readStream.readObject() as ArrayList<KeyData>

                readStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }
}