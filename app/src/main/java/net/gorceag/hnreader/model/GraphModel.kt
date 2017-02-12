package net.gorceag.hnreader.model

import android.graphics.Color
import java.util.*

/**
 * Created by slash on 2/8/17.
 */

class GraphModel {
    val rand = Random()
    val distance: Int
    val data: Array<Array<Int>>

    init {
        distance = randomInt(50, 10000)
        val ceiling = randomInt(20, 4000)
        data = Array(randomInt(3, 15), { arrayOf(randomInt(ceiling), randomColor()) })
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
        return rand.nextInt(range) + from
    }

    private fun randomInt(toSentinel: Int): Int {
        return randomInt(0, toSentinel)
    }
}