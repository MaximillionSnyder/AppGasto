package com.example.appgasto.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object DbKeyStore {

    private const val KEY_ALIAS = "gastosec_db_master"
    private const val KEY_FILE = "gastosec_db.key"
    private const val GCM_TAG_BITS = 128
    private const val IV_BYTES = 12

    fun getOrCreatePassphraseHex(context: Context): String {
        val file = File(context.applicationContext.filesDir, KEY_FILE)
        return if (file.exists()) {
            decrypt(file.readBytes()).decodeToString()
        } else {
            val random = ByteArray(32)
            SecureRandom().nextBytes(random)
            val hex = random.toHex()
            file.writeBytes(encrypt(hex.toByteArray()))
            hex
        }
    }

    fun wipeKeyFile(context: Context) {
        val file = File(context.applicationContext.filesDir, KEY_FILE)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun masterKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, masterKey())
        val iv = cipher.iv
        require(iv.size == IV_BYTES)
        return iv + cipher.doFinal(plain)
    }

    private fun decrypt(blob: ByteArray): ByteArray {
        require(blob.size > IV_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            masterKey(),
            GCMParameterSpec(GCM_TAG_BITS, blob, 0, IV_BYTES)
        )
        return cipher.doFinal(blob, IV_BYTES, blob.size - IV_BYTES)
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}
