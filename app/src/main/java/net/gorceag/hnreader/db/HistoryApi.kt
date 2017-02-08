package net.gorceag.hnreader.db

import android.content.Context
import java.util.*

/**
 * Created by slash on 2/8/17.
 */

object HistoryApi {
    lateinit var helper: HistoryDBHelper

    fun initialize(context: Context) {
        synchronized(this, {helper = HistoryDBHelper(context)})
    }

    fun insert(id: String, table: Table) {
        synchronized(this, {helper.insert(id, table)})
    }

    fun clear(table: Table) {
        synchronized(this, {helper.clear(table)})
    }

    fun getList(table: Table): ArrayList<String> {
        return helper.getList(table)
    }

    fun delete(id: String, table: Table) {
        synchronized(this, {helper.delete(id, table)})
    }

    fun isInTable(id: String, table: Table): Boolean {
        return helper.isInTable(id, table)
    }
}