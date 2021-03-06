package net.gorceag.hnreader.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import java.util.*


/**
 * Created by slash on 2/7/17.
 *
 * The class provides an API for the history database
 * should not be used directly, instead refer to the HistoryApi object
 */

class HistoryDBHelper(context: Context) : SQLiteOpenHelper(context, "hn_history.db", null, 1) {
    private val ID_COLUMN = "ID"
    private val SQL_CREATE_VISITED = "CREATE TABLE " + Table.VISITED.title + "(" + ID_COLUMN + " INTEGER PRIMARY KEY)"
    private val SQL_CREATE_FAVORITS = "CREATE TABLE " + Table.FAVORITES.title + "(" + ID_COLUMN + " INTEGER PRIMARY KEY)"

    lateinit var db: SQLiteDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_VISITED)
        db?.execSQL(SQL_CREATE_FAVORITS)
    }

    fun openDB() {
        db = writableDatabase
    }

    fun closeDB() {
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        throw UnsupportedOperationException("not implemented")
    }

    private fun hasEntry(value: String, table: Table, db: SQLiteDatabase): Boolean {
        val projection = arrayOf(ID_COLUMN)
        val selection = ID_COLUMN + " = ?"
        val selectionArgs = arrayOf(value)

        val cursor = db.query(
                table.title,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        )
        while (cursor.moveToNext()) {
            return true
        }
        cursor.close()
        return false
    }

    fun insert(id: String, table: Table) {
        if (!hasEntry(id, table, db)) {
            val values = ContentValues()
            values.put(ID_COLUMN, id)
            db.insert(table.title, null, values)
        }
    }

    fun isInTable(id: String, table: Table): Boolean {
        val result = hasEntry(id, table, db)
        return result
    }

    fun clear(table: Table) {
        db.delete(table.title, null, null)
    }

    fun delete(id: String, table: Table) {
        val selection = ID_COLUMN + " LIKE ?"
        val selectionArgs = arrayOf(id)
        db.delete(table.title, selection, selectionArgs)
    }

    fun getList(table: Table): ArrayList<String> {
        val projection = arrayOf(ID_COLUMN)
        val cursor = db.query(
                table.title,
                projection,
                null,
                null,
                null,
                null,
                null
        )
        val ids = ArrayList<String>()
        while (cursor.moveToNext()) {
            ids.add(cursor.getString(0))
        }
        cursor.close()
        return ids
    }
}