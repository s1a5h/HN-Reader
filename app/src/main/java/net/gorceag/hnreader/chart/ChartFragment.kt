package net.gorceag.hnreader.chart

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.gorceag.hnreader.AnimatedFragment
import net.gorceag.hnreader.AnimatorCallback
import net.gorceag.hnreader.MainActivity
import net.gorceag.hnreader.R
import net.gorceag.hnreader.model.RandomChartModel
import net.gorceag.hnreader.chart.ChartFragmentAnimator

/**
 * Created by slash on 2/6/17.
 *
 * Is created by the activity, used to host the chart
 * As any descendant of AnimatedFragment its removal is initiated by calling removeSelf() method.
 * The fragment is removed only after the hosted SurfaceView (e. g.ChartFragmentAnimator)
 * calls the terminated() method
 */

class ChartFragment(val background: Bitmap) : AnimatedFragment(), AnimatorCallback {

    private lateinit var animator: ChartFragmentAnimator

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater?.inflate(R.layout.fragment_graph, container, false)
        val surfaceContainer = root?.findViewById(R.id.surface_container) as FrameLayout
        animator = ChartFragmentAnimator(this, activity, background, RandomChartModel())
        animator.setZOrderOnTop(true)
        animator.isClickable = true
        surfaceContainer.addView(animator)
        return root
    }

    override fun terminated() {
        (activity as MainActivity).setListMenu()
        fragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }

    override fun initiated() {
        (activity as MainActivity).setChartMenu()
    }

    override fun removeSelf(fragmentManager: FragmentManager) {
        animator.roundUp()
    }

    override fun enableBackgroundContent(visible: Boolean) {
        throw UnsupportedOperationException("not implemented")
    }
}