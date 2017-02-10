package net.gorceag.hnreader.detail

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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

class WebFragment(val url: String, val background: Bitmap, val coords: Array<Float>) : AnimatedFragment() {

    lateinit var webView: WebView
    lateinit var animator: WebFragmentAnimator


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater?.inflate(R.layout.fragment_web, container, false)
        webView = root?.findViewById(R.id.web_content) as WebView
        webView.visibility = INVISIBLE
        val container = root?.findViewById(R.id.animator_container) as FrameLayout
        animator = WebFragmentAnimator(this, activity, background, coords)
        animator.setZOrderOnTop(true)
        animator.isClickable = true
        container.addView(animator)
        return root
    }

    fun enableBackGround() {
        webView.visibility = VISIBLE
    }
    fun disableBackGround() {
        webView.visibility = INVISIBLE
    }

    fun terminate() {
        fragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }

    override fun onResume() {
        super.onResume()
        webView.loadUrl(url)
    }

    override fun removeSelf(fragmentManager: FragmentManager) {
        webView.setDrawingCacheEnabled(true);
        val bitmap = webView.getDrawingCache(true)?.copy(
                Bitmap.Config.ARGB_8888, false);
        webView.destroyDrawingCache();
        if (bitmap != null) {
            animator.finalize(bitmap)
        }
    }
}