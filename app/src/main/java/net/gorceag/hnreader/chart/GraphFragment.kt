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
import net.gorceag.hnreader.R
import net.gorceag.hnreader.model.GraphModel
import net.gorceag.kotlinblade.GraphSurfaceView
import java.util.*

/**
 * Created by slash on 2/6/17.
 */
class GraphFragment(val background: Bitmap, val position: IntArray) : AnimatedFragment() {

    lateinit var surfaceContainer: FrameLayout
    lateinit var graph: GraphSurfaceView

    private fun dummyData(): GraphModel {
        return GraphModel()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater?.inflate(R.layout.fragment_graph, container, false)
        surfaceContainer = root?.findViewById(R.id.surface_container) as FrameLayout
        graph = GraphSurfaceView(activity, dummyData(), background, position)
//        graph = SurfaceView(context)
        graph.setZOrderOnTop(true)
        surfaceContainer.addView(graph)
        return root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()

    }

    override fun removeSelf(fragmentManager: FragmentManager) {
//        graph.stopDraw()
        fragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }
}