package net.gorceag.hnreader.model

import android.graphics.Color
import java.util.*

/**
 * Created by slash on 2/8/17.
 */

class GraphModel {
    val distance: Int
    val data: Array<Array<Int>>

    init {
        distance = 500
        data = arrayOf(
                arrayOf(50, Color.BLACK),
                arrayOf(60, Color.GRAY),
                arrayOf(102, Color.MAGENTA),
                arrayOf(5, Color.BLUE)
        )
    }
}