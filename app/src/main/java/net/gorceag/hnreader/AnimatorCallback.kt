package net.gorceag.hnreader

/**
 * Created by slash on 2/12/17.
 */

interface AnimatorCallback {
    fun terminated()

    fun initiated()
    fun enableBackgroundContent(visible: Boolean)
}