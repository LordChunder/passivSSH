package org.awaiteddev.common.ssh

import org.bouncycastle.openssl.PEMWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.*

class KeyGeneration {

    companion object {

        fun generateKeyPair(username:String) {
            try {
                val keyGen = KeyPairGenerator.getInstance("RSA")
                keyGen.initialize(2048)
                val key = keyGen.generateKeyPair()
                val privateKeyFile = File("pvtKey.key")
                val publicKeyFile = File("pubKey.pub")

                // Create files to store public and private key
                if (privateKeyFile.parentFile != null) {
                    privateKeyFile.parentFile.mkdirs()
                }
                privateKeyFile.createNewFile()
                if (publicKeyFile.parentFile != null) {
                    publicKeyFile.parentFile.mkdirs()
                }
                publicKeyFile.createNewFile()


                // Saving the Public key in a file
                val writer = FileWriter(publicKeyFile)
                val rsaKey = encodeAsOpenSSH(key.public as RSAPublicKey,username)

                writer.write(rsaKey)
                writer.close()
                // Saving the Private key in a file
                val pemWriter = PEMWriter(FileWriter(privateKeyFile))
                pemWriter.writeObject(key.private)
                pemWriter.close()

            } catch (e: Exception) {
                e.printStackTrace()
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