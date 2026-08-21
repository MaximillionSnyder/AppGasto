package com.example.appgasto.data.local

import android.content.Context
import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File

object SqlCipherMigration {

    private const val TAG = "SqlCipherMigration"
    private const val SQLITE_HEADER = "SQLite format 3"

    fun migrateIfNeeded(context: Context, dbName: String, passphraseHex: String) {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists() || !isPlaintext(dbFile)) return

        val parent = dbFile.parentFile ?: return
        val tempEncrypted = File(parent, "$dbName.enc-tmp")
        tempEncrypted.delete()

        var schemaVersion = 0
        try {
            val plain = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
            try {
                schemaVersion = plain.version
                plain.execSQL(
                    "ATTACH DATABASE ? AS encrypted_copy KEY ?;",
                    arrayOf(tempEncrypted.absolutePath, passphraseHex)
                )
                plain.rawExecSQL("SELECT sqlcipher_export('encrypted_copy');")
                plain.execSQL("DETACH DATABASE encrypted_copy;")
            } finally {
                plain.close()
            }

            val encrypted = SQLiteDatabase.openDatabase(
                tempEncrypted.absolutePath,
                passphraseHex,
                null,
                SQLiteDatabase.OPEN_READWRITE,
                null
            )
            try {
                encrypted.version = schemaVersion
            } finally {
                encrypted.close()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Migration to SQLCipher failed", t)
            tempEncrypted.delete()
            return
        }

        val backupPlain = File(parent, "$dbName.plain-bak")
        backupPlain.delete()

        val renamedOriginal = dbFile.renameTo(backupPlain)
        if (!renamedOriginal) {
            Log.e(TAG, "Could not back up plaintext database; aborting swap")
            tempEncrypted.delete()
            return
        }

        if (tempEncrypted.renameTo(dbFile)) {
            deleteSidecars(backupPlain)
            backupPlain.delete()
        } else {
            Log.e(TAG, "Could not promote encrypted database; restoring plaintext")
            backupPlain.renameTo(dbFile)
            tempEncrypted.delete()
        }
    }

    fun isPlaintext(file: File): Boolean = try {
        file.inputStream().use { stream ->
            val header = ByteArray(SQLITE_HEADER.length)
            val read = stream.read(header)
            read == header.length && String(header, Charsets.US_ASCII) == SQLITE_HEADER
        }
    } catch (t: Throwable) {
        false
    }

    private fun deleteSidecars(base: File) {
        listOf("$base-wal", "$base-shm", "$base-journal").forEach { name ->
            val sidecar = File(name)
            if (sidecar.exists()) {
                sidecar.delete()
            }
        }
    }
}
