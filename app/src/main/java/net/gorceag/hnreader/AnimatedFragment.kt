package net.gorceag.hnreader

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

/**
 * Created by slash on 2/9/17.
 *
 * Its subclass should not be removed directly by the hosting activity.
 * Instead one should call removeSelf() and implement the removal logic
 */

abstract class AnimatedFragment : Fragment() {
    abstract fun removeSelf(fragmentManager: FragmentManager)
}