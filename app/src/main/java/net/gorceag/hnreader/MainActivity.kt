package net.gorceag.hnreader

import android.app.Fragment
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebViewFragment
import net.gorceag.hnreader.db.HistoryApi
import net.gorceag.hnreader.db.Table
import net.gorceag.hnreader.model.Article
import net.gorceag.hnreader.model.ArticleSummary
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var menu: Menu
    var lastArticleId: String = ""
    lateinit var articleListFragment: ArticleListFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        HistoryApi.initialize(this)
        articleListFragment = ArticleListFragment()
        val fragmentManager = fragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.list_content_container, articleListFragment, "List")
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

    fun showDetail(id: String, url: String) {
        lastArticleId = id

        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                HistoryApi.insert(id, Table.VISITED)
            }

            override fun onPostExecute(result: Unit?) {
                articleListFragment.updateModel(id)
            }
        }.execute()

        val fragment = ArticleWebViewFragment(url)
        val fragmentManager = fragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.web_content_container, fragment, "Detail")
                .addToBackStack("Detail")
                .commit()
        updateItemMenu(id)
    }

    private fun updateItemMenu(id: String) {
        menu.findItem(R.id.action_clear_visited).setVisible(false)
        menu.findItem(R.id.action_clear_favorites).setVisible(false)
        object : AsyncTask<String, Void, Boolean>() {
            override fun doInBackground(vararg params: String?): Boolean {
                return HistoryApi.isInTable(id, Table.FAVORITES)
            }

            override fun onPostExecute(result: Boolean) {
                setMenuIU(result)
            }

            private fun setMenuIU(result: Boolean) {
                if (result) {
                    menu.findItem(R.id.action_add).setVisible(false)
                    menu.findItem(R.id.action_remove).setVisible(true)
                } else {
                    menu.findItem(R.id.action_add).setVisible(true)
                    menu.findItem(R.id.action_remove).setVisible(false)
                }
            }
        }.execute();
    }

    private fun updateListMenu() {
        menu.findItem(R.id.action_add).setVisible(false)
        menu.findItem(R.id.action_remove).setVisible(false)
        menu.findItem(R.id.action_clear_visited).setVisible(true)
        menu.findItem(R.id.action_clear_favorites).setVisible(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        this.menu = menu
        updateListMenu()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.action_add -> {
                addToFavorites()
                return true
            }
            R.id.action_remove -> {
                removeFromFavorites()
                return true
            }
            R.id.action_clear_favorites -> {
                clearFavorites()
                return true
            }
            R.id.action_clear_visited -> {
                clearVisited()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun addToFavorites() {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                HistoryApi.insert(lastArticleId, Table.FAVORITES)
            }

            override fun onPostExecute(result: Unit?) {
                updateItemMenu(lastArticleId)
                articleListFragment.updateModel(lastArticleId)
            }
        }.execute()
    }

    private fun removeFromFavorites() {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                HistoryApi.delete(lastArticleId, Table.FAVORITES)
            }

            override fun onPostExecute(result: Unit?) {
                updateItemMenu(lastArticleId)
                articleListFragment.updateModel(lastArticleId)
            }
        }.execute()
    }

    private fun clearFavorites() {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                HistoryApi.clear(Table.FAVORITES)
            }

            override fun onPostExecute(result: Unit?) {
                articleListFragment.updateModel()
            }
        }.execute()
    }

    private fun clearVisited() {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                HistoryApi.clear(Table.VISITED)
            }

            override fun onPostExecute(result: Unit?) {
                articleListFragment.updateModel()
            }
        }.execute()
    }

    override fun onBackPressed() {
        val fragmentManager = fragmentManager
        if (fragmentManager.findFragmentById(R.id.web_content_container) != null) {
            updateListMenu()
        }
        super.onBackPressed()
    }
}
