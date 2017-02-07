package net.gorceag.hnreader

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebViewFragment
import net.gorceag.hnreader.model.Article
import net.gorceag.hnreader.model.ArticleSummary
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val listFragment = ArticleListFragment()
        val fragmentManager = fragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.list_content_container, listFragment, "List")
                .commit()

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            showGraph()
        }
    }

    private fun showGraph() {
        val summary = ArrayList<ArticleSummary>()
        summary.add(ArticleSummary(10, 330))
        summary.add(ArticleSummary(14, 300))
        summary.add(ArticleSummary(28, 50))
        val fragment = GraphFragment(summary)
        val fragmentManager = fragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.graph_container, fragment, "Graph")
                .addToBackStack("Graph")
                .commit()
    }

    fun showDetail(article: Article) {
        val fragment = ArticleWebViewFragment(article.url)
        val fragmentManager = fragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.web_content_container, fragment, "Detail")
                .addToBackStack("Detail")
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
