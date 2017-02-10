package net.gorceag.hnreader

import android.app.Fragment
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.webkit.WebViewFragment
import android.widget.ImageView
import net.gorceag.hnreader.chart.GraphFragment
import net.gorceag.hnreader.db.HistoryApi
import net.gorceag.hnreader.db.Table
import net.gorceag.hnreader.detail.WebFragment
import net.gorceag.hnreader.list.ArticleListFragment
import net.gorceag.hnreader.model.Article
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var menu: Menu
    var lastArticleId: String = ""
    lateinit var articleListFragment: ArticleListFragment
//    lateinit var button: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        HistoryApi.initialize(this)
        articleListFragment = ArticleListFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.list_content_container, articleListFragment, "List")
                .commit()

//        button = findViewById(R.id.fab) as FloatingActionButton
//        button.setOnClickListener { view ->
//            showChart()
//        }
    }

    private fun showChart() {
        val fragment = supportFragmentManager.findFragmentById(R.id.content_container)
        if (fragment == null) {
            articleListFragment.view?.setDrawingCacheEnabled(true);
            val bitmap = articleListFragment.view?.getDrawingCache(true)?.copy(
                    Bitmap.Config.ARGB_8888, false);
            articleListFragment.view?.destroyDrawingCache();

//            button = findViewById(R.id.fab) as FloatingActionButton
            var dimens = IntArray(2)
//            button.getLocationInWindow(dimens)
            if (bitmap != null) {
                supportFragmentManager.beginTransaction()
                        .add(R.id.content_container, GraphFragment(bitmap, dimens), "Graph")
                        .commit()
            }
        }
    }

    fun showDetail(id: String, url: String, coords: Array<Float>) {

        val fragment = supportFragmentManager.findFragmentById(R.id.content_container)
        if (fragment == null) {
            lastArticleId = id

            object : AsyncTask<String, Void, Unit>() {
                override fun doInBackground(vararg params: String?) {
                    HistoryApi.insert(id, Table.VISITED)
                }

                override fun onPostExecute(result: Unit?) {
                    articleListFragment.updateModel(id)
                }
            }.execute()

            articleListFragment.view?.setDrawingCacheEnabled(true);
            val bitmap = articleListFragment.view?.getDrawingCache(true)?.copy(
                    Bitmap.Config.ARGB_8888, false);
            articleListFragment.view?.destroyDrawingCache();
            if (bitmap != null) {
                supportFragmentManager.beginTransaction()
                        .add(R.id.content_container, net.gorceag.hnreader.detail.WebFragment(url, bitmap, coords), "Detail")
                        .commit()
            }
            updateItemMenu(id)
//            hideButton()
        }
    }

    private fun updateItemMenu(id: String) {
        menu.findItem(R.id.action_clear_visited).setVisible(false)
        menu.findItem(R.id.action_clear_favorites).setVisible(false)
        menu.findItem(R.id.action_show_chart).setVisible(false)
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
        menu.findItem(R.id.action_show_chart).setVisible(true)
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
            R.id.action_show_chart -> {
                showChart()
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

//    private fun hideButton() {
//        button.visibility = INVISIBLE
//    }
//
//    private fun showButton() {
//        button.visibility = VISIBLE
//    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.content_container)
        if (fragment != null) {
            updateListMenu()
//            showButton()
            (fragment as AnimatedFragment).removeSelf(supportFragmentManager)
            return
        }
        super.onBackPressed()
    }
}
