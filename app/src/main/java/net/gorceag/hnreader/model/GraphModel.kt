package net.gorceag.hnreader.model

import android.graphics.Color
import java.util.*

/**
 * Created by slash on 2/8/17.
 */

class GraphModel {
    val distance: Int
    val data: Map<Int, Int>

    init {
        distance = 500
        data = LinkedHashMap<Int, Int>()
        data.put(50, Color.BLACK)
        data.put(60, Color.GRAY)
        data.put(102, Color.MAGENTA)
        data.put(5, Color.BLUE)
    }
}