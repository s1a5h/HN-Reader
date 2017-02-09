package net.gorceag.hnreader.detail

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewFragment
import android.widget.FrameLayout
import net.gorceag.hnreader.AnimatedFragment
import net.gorceag.hnreader.R
import net.gorceag.kotlinblade.GraphSurfaceView

/**
 * Created by slash on 2/6/17.
 */

class WebFragment(val url: String, val background: Bitmap, val y: Int) : AnimatedFragment() {

    lateinit var webView: WebView
    lateinit var animator: WebFragmentAnimator


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater?.inflate(R.layout.fragment_web, container, false)
        webView = root?.findViewById(R.id.web_content) as WebView
        val container = root?.findViewById(R.id.animator_container) as FrameLayout
        animator = WebFragmentAnimator(activity, background, y)
        animator.setZOrderOnTop(true)
        container.addView(animator)
        return root
    }

    override fun onResume() {
        super.onResume()
        webView.loadUrl(url)
    }

    override fun removeSelf(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }
}