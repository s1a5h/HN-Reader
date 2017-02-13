package net.gorceag.hnreader

/**
 * Created by slash on 2/12/17.
 *
 * Should be implemented by any Fragment or View that wishes to host a SurfaceView with an AnimationDrawer class
 * to receive the callbacks
 */

interface AnimatorCallback {
    fun terminated()
    fun initiated()
    fun enableBackgroundContent(visible: Boolean)
}