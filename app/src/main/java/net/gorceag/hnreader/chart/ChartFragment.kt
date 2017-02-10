package net.gorceag.hnreader.chart

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.gorceag.hnreader.AnimatedFragment
import net.gorceag.hnreader.MainActivity
import net.gorceag.hnreader.R
import net.gorceag.hnreader.model.GraphModel
import net.gorceag.kotlinblade.ChartFragmentAnimator
import java.util.*

/**
 * Created by slash on 2/6/17.
 */
class ChartFragment(val background: Bitmap, val position: IntArray) : AnimatedFragment() {

    lateinit var surfaceContainer: FrameLayout
    lateinit var animator: ChartFragmentAnimator

    private fun dummyData(): GraphModel {
        return GraphModel()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater?.inflate(R.layout.fragment_graph, container, false)
        surfaceContainer = root?.findViewById(R.id.surface_container) as FrameLayout
        animator = ChartFragmentAnimator(this, dummyData(), background, position)
        animator.setZOrderOnTop(true)
        animator.isClickable = true
        surfaceContainer.addView(animator)
        return root
    }

    fun terminate() {
        (activity as MainActivity).updateListMenu()
        fragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }
    override fun removeSelf(fragmentManager: FragmentManager) {
        animator.roundUp()
    }
}