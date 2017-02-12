package net.gorceag.hnreader

import android.graphics.Bitmap
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

/**
 * Created by slash on 2/9/17.
 */
abstract class AnimatedFragment: Fragment() {
    abstract fun removeSelf(fragmentManager: FragmentManager)

}