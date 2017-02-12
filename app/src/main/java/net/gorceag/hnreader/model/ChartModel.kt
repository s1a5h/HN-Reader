package net.gorceag.hnreader.model

import android.graphics.Color
import java.util.*

/**
 * Created by slash on 2/8/17.
 */

class ChartModel {
    val rand = Random()
    val data: Array<Array<Int>>

    init {
        val ceiling = randomInt(20, 4000)
        data = Array(randomInt(2, 10), { arrayOf(randomInt(ceiling), randomColor()) })
    }

    private fun randomColor(): Int {
        return Color.argb(
                255,
                randomInt(256),
                randomInt(256),
                randomInt(256)
        )
    }

    private fun randomInt(from: Int, toSentinel: Int): Int {
        val range = toSentinel - from
        return if (range != 0) rand.nextInt(range) + from else from
    }

    private fun randomInt(toSentinel: Int): Int {
        return randomInt(0, toSentinel)
    }
}