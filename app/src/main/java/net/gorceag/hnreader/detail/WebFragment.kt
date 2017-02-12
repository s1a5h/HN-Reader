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
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebViewFragment
import android.widget.FrameLayout
import net.gorceag.hnreader.AnimatedFragment
import net.gorceag.hnreader.AnimatorCallback
import net.gorceag.hnreader.MainActivity
import net.gorceag.hnreader.R
import net.gorceag.kotlinblade.ChartFragmentAnimator

/**
 * Created by slash on 2/6/17.
 */

class WebFragment(val url: String, val background: Bitmap, val coords: Array<Float>) : AnimatedFragment(), AnimatorCallback {

    lateinit var webView: WebView
    lateinit var animator: WebFragmentAnimator

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater?.inflate(R.layout.fragment_web, container, false)
        webView = root?.findViewById(R.id.web_content) as WebView
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: WebResourceRequest): Boolean = true
        })
        webView.visibility = INVISIBLE
        val container = root?.findViewById(R.id.animator_container) as FrameLayout
        animator = WebFragmentAnimator(this, activity, background, coords)
        animator.setZOrderOnTop(true)
        animator.isClickable = true
        container.addView(animator)
        return root
    }

    override fun enableBackgroundContent(visible: Boolean) {
        webView.visibility = if (visible) VISIBLE else INVISIBLE
    }

    override fun terminated() {
        (activity as MainActivity).updateListMenu()
        fragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }

    override fun initiated() {
        (activity as MainActivity).updateItemMenu()
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
            animator.roundUp(bitmap)
        }
    }
}