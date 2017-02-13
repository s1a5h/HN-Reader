package net.gorceag.hnreader.db

import android.content.Context
import java.util.*

/**
 * Created by slash on 2/8/17.
 *
 * A Singlethon access point to the history database
 * initialize() method should be called before any interactions with the database
 * finalize() closes the database, so any subsequent queries will fail
 */

object HistoryApi {
    lateinit var helper: HistoryDBHelper

    fun initialize(context: Context) {
        synchronized(this, {
            helper = HistoryDBHelper(context)
            helper.openDB()
        })
    }

    fun finalize() {
        helper.closeDB()
    }

    fun insert(id: String, table: Table) {
        synchronized(this, { helper.insert(id, table) })
    }

    fun clear(table: Table) {
        synchronized(this, { helper.clear(table) })
    }

    fun getList(table: Table): ArrayList<String> {
        return helper.getList(table)
    }

    fun delete(id: String, table: Table) {
        synchronized(this, { helper.delete(id, table) })
    }

    fun isInTable(id: String, table: Table): Boolean {
        return helper.isInTable(id, table)
    }
}