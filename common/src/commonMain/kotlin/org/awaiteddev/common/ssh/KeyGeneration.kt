package org.awaiteddev.common.ssh

import org.awaiteddev.common.data.KeyData
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.*

class KeyGeneration {

    companion object {

        fun generateKeyPair(username: String, keyName: String): KeyData? {
            return try {
                val keyGen = KeyPairGenerator.getInstance("RSA")
                keyGen.initialize(2048)
                val key = keyGen.generateKeyPair()

                val rsaKey = encodeAsOpenSSH(key.public as RSAPublicKey, username)
                val path = KeyFileHandler.saveKeyToFile(keyName,rsaKey, key.private)
                if (path != "")
                    KeyData(keyName, username, rsaKey, path)
                else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun encodeAsOpenSSH(key: RSAPublicKey): String {
            val keyBlob: ByteArray? = keyBlob(key.publicExponent, key.modulus)
            val encodedByteArray = Base64.getEncoder().encode(keyBlob)
            val encodedString = String(encodedByteArray)
            return "ssh-rsa $encodedString"
        }

        private fun encodeAsOpenSSH(key: RSAPublicKey?, subject: String): String {
            return encodeAsOpenSSH(key!!) + " " + subject
        }

        private fun writeLengthFirst(array: ByteArray, out: ByteArrayOutputStream) {
            out.write(array.size ushr 24 and 0xFF)
            out.write(array.size ushr 16 and 0xFF)
            out.write(array.size ushr 8 and 0xFF)
            out.write(array.size ushr 0 and 0xFF)
            if (array.size == 1 && array[0] == 0x00.toByte()) out.write(ByteArray(0)) else out.write(array)
        }

        private fun keyBlob(publicExponent: BigInteger, modulus: BigInteger): ByteArray? {
            try {
                val out = ByteArrayOutputStream()
                writeLengthFirst("ssh-rsa".toByteArray(), out)
                writeLengthFirst(publicExponent.toByteArray(), out)
                writeLengthFirst(modulus.toByteArray(), out)
                return out.toByteArray()
            } catch (e: IOException) {
                println("Failed")
                e.printStackTrace()
            }
            return null
        }
    }
}