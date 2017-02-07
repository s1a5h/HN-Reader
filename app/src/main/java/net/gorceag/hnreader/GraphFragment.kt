package net.gorceag.hnreader

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.gorceag.hnreader.model.ArticleSummary
import net.gorceag.kotlinblade.GraphSurfaceView
import java.util.*

/**
 * Created by slash on 2/6/17.
 */
class GraphFragment(val data: List<ArticleSummary>) : Fragment() {

    lateinit var surfaceContainer: FrameLayout
    lateinit var graph: GraphSurfaceView

    private fun dummyMap(): Map<Int, Int> {
        val map = LinkedHashMap<Int, Int>()
        map.put(80, 300)
        map.put(88, 340)
        map.put(3, 20)
        map.put(24, 100)
        return map
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

        var root = super.onCreateView(inflater, container, savedInstanceState)
        root = inflater?.inflate(R.layout.fragment_graph, container, false)
        surfaceContainer = root.findViewById(R.id.surface_container) as FrameLayout
        graph = GraphSurfaceView(activity, dummyMap())
        surfaceContainer.addView(graph)

        return root
    }

    override fun onResume() {
        super.onResume()

    }
}