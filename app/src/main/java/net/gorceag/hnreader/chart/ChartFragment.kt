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
import net.gorceag.hnreader.AnimatorCallback
import net.gorceag.hnreader.MainActivity
import net.gorceag.hnreader.R
import net.gorceag.hnreader.model.ChartModel
import net.gorceag.kotlinblade.ChartFragmentAnimator
import java.util.*

/**
 * Created by slash on 2/6/17.
 */
class ChartFragment(val background: Bitmap) : AnimatedFragment(), AnimatorCallback {

    private lateinit var animator: ChartFragmentAnimator

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater?.inflate(R.layout.fragment_graph, container, false)
        val surfaceContainer = root?.findViewById(R.id.surface_container) as FrameLayout
        animator = ChartFragmentAnimator(this, activity, ChartModel(), background)
        animator.setZOrderOnTop(true)
        animator.isClickable = true
        surfaceContainer.addView(animator)
        return root
    }

    override fun terminated() {
        (activity as MainActivity).updateListMenu()
        fragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }

    override fun initiated() {
        (activity as MainActivity).updateChartMenu()
    }

    override fun removeSelf(fragmentManager: FragmentManager) {
        animator.roundUp()
    }

    override fun enableBackgroundContent(visible: Boolean) {
        throw UnsupportedOperationException("not implemented")
    }
}