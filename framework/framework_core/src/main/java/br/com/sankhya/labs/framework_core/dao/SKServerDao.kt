package br.com.sankhya.labs.framework_core.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.Telephony.Carriers.SERVER
import br.com.sankhya.labs.framework_core.service.SKServerService
import br.com.sankhya.labs.framework_core.model.SKServer
import br.com.sankhya.labs.framework_core.utils.SKAppConfigUtil

class SKServerDao(context: Context?) : SQLiteOpenHelper(
        context,
        "SK_${SKAppConfigUtil.ProductCode}_${SERVER_TABLE_NAME}_DATABASE",
        null,
        DATABASE_VERSION
) {

    companion object {
        val DATABASE_VERSION = 1
        val SERVER_TABLE_NAME = SERVER
        val KEY_HOST = "HOST"
        val KEY_NOME = "NOME"
        val KEY_PROTOCOLO = "PROTOCOLO"
        val KEY_ESCOLHIDO = "ESCOLHIDO"
        val CREATE_TABLE = ("CREATE TABLE $SERVER_TABLE_NAME ($KEY_HOST TEXT, $KEY_NOME TEXT, $KEY_PROTOCOLO TEXT, $KEY_ESCOLHIDO REAL, PRIMARY KEY ($KEY_PROTOCOLO, $KEY_HOST))")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(("DROP TABLE IF EXISTS $SERVER_TABLE_NAME"))
        onCreate(db)
    }

    fun hasServer(server: SKServer): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $SERVER_TABLE_NAME WHERE $KEY_PROTOCOLO = ? " +
                "AND $KEY_HOST = ?", arrayOf(server.protocolo, server.host))
        return if (cursor != null) {
            if (cursor.count > 0) {
                cursor.close()
                db.close()
                true
            } else {
                cursor.close()
                db.close()
                false
            }
        } else false
    }

    fun addServer(server: SKServer) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(KEY_HOST, server.host.split("/")[0])
        values.put(KEY_NOME, server.nome)
        values.put(KEY_PROTOCOLO, server.protocolo)
        values.put(KEY_ESCOLHIDO, System.currentTimeMillis())
        db.insert(SERVER_TABLE_NAME, null, values)
        db.close()
    }

    fun getCurrentServer(): SKServer? {
        val db = readableDatabase
        val cursor = db.query(SERVER_TABLE_NAME, arrayOf(KEY_HOST, KEY_NOME, KEY_PROTOCOLO, KEY_ESCOLHIDO),
            null, null, null, null, "$KEY_ESCOLHIDO DESC")
        return if (cursor != null) {
            if (cursor.count > 0) {
                cursor.moveToFirst()
                val server = SKServer(host = cursor.getString(0), nome = cursor.getString(1), protocolo = cursor.getString(2))
                server.escolhido = cursor.getLong(3)
                cursor.close()
                db.close()
                server
            } else {
                cursor.close()
                db.close()
                //null todo remover
                SKServer(host = "", nome = "", protocolo = "")
            }
        } else {
            null
        }
    }

    fun getAllServers(): ArrayList<SKServer> {
        val db = readableDatabase
        val servers = ArrayList<SKServer>()
        val cursor = db.query(SERVER_TABLE_NAME, arrayOf(KEY_HOST, KEY_NOME, KEY_PROTOCOLO, KEY_ESCOLHIDO),
            null, null, null, null, "$KEY_ESCOLHIDO DESC")
        if (cursor != null) {
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val server = SKServer(host = cursor.getString(0), nome = cursor.getString(1), protocolo = cursor.getString(2))
                    server.escolhido = cursor.getLong(3)
                    servers.add(server)
                } while (cursor.moveToNext())
                cursor.close()
                db.close()
            }
        }
        return servers
    }

    fun getServerCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $SERVER_TABLE_NAME", null)
        return if (cursor != null) {
            val count = cursor.count
            cursor.close()
            db.close()
            count
        } else 0
    }

    fun deleteServer(server: SKServer) {
        val db = writableDatabase
        db.delete(SERVER_TABLE_NAME, "$KEY_PROTOCOLO = ? AND $KEY_HOST = ?", arrayOf(server.protocolo, server.host))
        db.close()
    }

    fun deleteAllServers() {
        val db = writableDatabase
        db.delete(SERVER_TABLE_NAME, null, null)
        db.close()
    }

    fun updateServer(server: SKServer) {
        val db = writableDatabase

        SKServerService.isSystemCompatible(server)
        val values = ContentValues()
        values.put(KEY_NOME, server.nome)
        values.put(KEY_ESCOLHIDO, System.currentTimeMillis())
        db.update(SERVER_TABLE_NAME, values, "$KEY_PROTOCOLO = ? AND $KEY_HOST = ?",
            arrayOf(server.protocolo, server.host))
        db.close()
    }

}