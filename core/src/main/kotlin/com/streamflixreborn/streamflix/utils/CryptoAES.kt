package com.streamflixreborn.streamflix.utils

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoAES {
    fun decrypt(encryptedData: String, key: String): String {
        return try {
            val decodedData = Base64.getDecoder().decode(encryptedData)
            val iv = decodedData.copyOfRange(0, 16)
            val ciphertext = decodedData.copyOfRange(16, decodedData.size)
            val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
            String(cipher.doFinal(ciphertext))
        } catch (e: Exception) { e.printStackTrace(); "" }
    }
}
