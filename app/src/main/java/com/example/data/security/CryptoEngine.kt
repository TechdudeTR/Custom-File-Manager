package com.example.data.security

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

object CryptoEngine {

    fun hashPin(pin: String, salt: String = "FILE_MANAGER_SALT"): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((pin + salt).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateChecksum(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(content.toByteArray(Charsets.UTF_8))
        return bytes.take(8).joinToString("") { "%02x".format(it) }.uppercase()
    }

    /**
     * Simulated AES-256 Encryption marker & payload transformation
     */
    fun encryptFileContent(rawText: String, secretKey: String): String {
        val key = secretKey.padEnd(16, 'X').substring(0, 16).toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val keySpec = SecretKeySpec(key, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encryptedBytes = cipher.doFinal(rawText.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT)
    }

    fun decryptFileContent(encryptedText: String, secretKey: String): String {
        return try {
            val key = secretKey.padEnd(16, 'X').substring(0, 16).toByteArray(Charsets.UTF_8)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keySpec = SecretKeySpec(key, "AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = android.util.Base64.decode(encryptedText, android.util.Base64.DEFAULT)
            String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            "*** DECRYPTION FAILED: Invalid Key or Corrupted Cipher ***"
        }
    }
}
