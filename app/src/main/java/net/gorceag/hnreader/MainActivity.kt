package net.gorceag.hnreader

import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import net.gorceag.hnreader.chart.ChartFragment
import net.gorceag.hnreader.db.HistoryApi
import net.gorceag.hnreader.db.Table
import net.gorceag.hnreader.list.ArticleListFragment

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
        supportFragmentManager.beginTransaction()
                .replace(R.id.list_content_container, articleListFragment, "List")
                .commit()
    }

    private fun getBackGroundImage(): Bitmap? {
        articleListFragment.view?.setDrawingCacheEnabled(true);
        val bitmap = articleListFragment.view?.getDrawingCache(true)?.copy(
                Bitmap.Config.ARGB_8888, false);
        articleListFragment.view?.destroyDrawingCache();
        return bitmap
    }

    private fun showChart() {
        val fragment = supportFragmentManager.findFragmentById(R.id.content_container)
        val bitmap = getBackGroundImage()
        if (fragment == null && bitmap != null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_container, ChartFragment(bitmap), "Graph")
                    .commit()
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
            val bitmap = getBackGroundImage()
            if (bitmap != null) {
                supportFragmentManager.beginTransaction()
                        .add(R.id.content_container, net.gorceag.hnreader.detail.WebFragment(url, bitmap, coords), "Detail")
                        .commit()
            }
        }
    }

    fun updateItemMenu() {
        menu.findItem(R.id.action_clear_visited).setVisible(false)
        menu.findItem(R.id.action_clear_favorites).setVisible(false)
        menu.findItem(R.id.action_show_chart).setVisible(false)
        object : AsyncTask<String, Void, Boolean>() {
            override fun doInBackground(vararg params: String?): Boolean {
                return HistoryApi.isInTable(lastArticleId, Table.FAVORITES)
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

    fun updateListMenu() {
        menu.findItem(R.id.action_add).setVisible(false)
        menu.findItem(R.id.action_remove).setVisible(false)
        menu.findItem(R.id.action_clear_visited).setVisible(true)
        menu.findItem(R.id.action_clear_favorites).setVisible(true)
        menu.findItem(R.id.action_show_chart).setVisible(true)
    }

    fun updateChartMenu() {
        menu.findItem(R.id.action_add).setVisible(false)
        menu.findItem(R.id.action_remove).setVisible(false)
        menu.findItem(R.id.action_clear_visited).setVisible(false)
        menu.findItem(R.id.action_clear_favorites).setVisible(false)
        menu.findItem(R.id.action_show_chart).setVisible(false)
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
                setToFavorites(true)
                return true
            }
            R.id.action_remove -> {
                setToFavorites(false)
                return true
            }
            R.id.action_clear_favorites -> {
                clearHistory(Table.FAVORITES)
                return true
            }
            R.id.action_clear_visited -> {
                clearHistory(Table.VISITED)
                return true
            }
            R.id.action_show_chart -> {
                showChart()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setToFavorites(isFavorite: Boolean) {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                if (isFavorite) {
                    HistoryApi.insert(lastArticleId, Table.FAVORITES)
                } else {
                    HistoryApi.delete(lastArticleId, Table.FAVORITES)
                }
            }

            override fun onPostExecute(result: Unit?) {
                updateItemMenu()
                articleListFragment.updateModel(lastArticleId)
            }
        }.execute()
    }

    private fun clearHistory(table: Table) {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                HistoryApi.clear(table)
            }

            override fun onPostExecute(result: Unit?) {
                articleListFragment.updateModel()
            }
        }.execute()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.content_container) as AnimatedFragment
        if (fragment != null) {
            fragment.removeSelf(supportFragmentManager)
            return
        }
        super.onBackPressed()
    }
}
