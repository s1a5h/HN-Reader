package net.gorceag.hnreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewFragment

/**
 * Created by slash on 2/6/17.
 */

class ArticleWebViewFragment(val url: String) : WebViewFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        webView.loadUrl(url)
    }
}