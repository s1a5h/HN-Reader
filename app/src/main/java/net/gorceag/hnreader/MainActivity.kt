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
    inner class MenuManager(menu: Menu) {
        val menuItems = mapOf(
                R.id.action_add to menu.findItem(R.id.action_add),
                R.id.action_remove to menu.findItem(R.id.action_remove),
                R.id.action_clear_visited to menu.findItem(R.id.action_clear_visited),
                R.id.action_clear_favorites to menu.findItem(R.id.action_clear_favorites),
                R.id.action_show_chart to menu.findItem(R.id.action_show_chart)
        )

        fun setListMenu() {
            menuItems[R.id.action_add]?.isVisible = false
            menuItems[R.id.action_remove]?.isVisible = false
            menuItems[R.id.action_clear_visited]?.isVisible = true
            menuItems[R.id.action_clear_favorites]?.isVisible = true
            menuItems[R.id.action_show_chart]?.isVisible = true
        }

        fun setChartMenu() {
            menuItems[R.id.action_add]?.isVisible = false
            menuItems[R.id.action_remove]?.isVisible = false
            menuItems[R.id.action_clear_visited]?.isVisible = false
            menuItems[R.id.action_clear_favorites]?.isVisible = false
            menuItems[R.id.action_show_chart]?.isVisible = false
        }

        fun setItemMenu() {
            menuItems[R.id.action_clear_visited]?.isVisible = false
            menuItems[R.id.action_clear_favorites]?.isVisible = false
            menuItems[R.id.action_show_chart]?.isVisible = false
            object : AsyncTask<String, Void, Boolean>() {
                override fun doInBackground(vararg params: String?): Boolean {
                    return HistoryApi.isInTable(lastArticleId, Table.FAVORITES)
                }

                override fun onPostExecute(result: Boolean) {
                    setMenuIU(result)
                }

                private fun setMenuIU(result: Boolean) {
                    if (result) {
                        menuItems[R.id.action_add]?.isVisible = false
                        menuItems[R.id.action_remove]?.isVisible = true
                    } else {
                        menuItems[R.id.action_add]?.isVisible = true
                        menuItems[R.id.action_remove]?.isVisible = false
                    }
                }
            }.execute()
        }
    }

    lateinit var menuManager: MenuManager
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

    override fun onDestroy() {
        super.onDestroy()
        HistoryApi.finalize()
    }

    private fun getBackGroundImage(): Bitmap? {
        articleListFragment.view?.setDrawingCacheEnabled(true)
        val bitmap = articleListFragment.view?.getDrawingCache(true)?.copy(
                Bitmap.Config.ARGB_8888, false)
        articleListFragment.view?.destroyDrawingCache()
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
        val bitmap = getBackGroundImage()
        if (fragment == null && bitmap != null) {
            lastArticleId = id
            markVisited()
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_container, net.gorceag.hnreader.detail.WebFragment(url, bitmap, coords), "Detail")
                    .commit()
        }
    }

    fun markVisited() {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                HistoryApi.insert(lastArticleId, Table.VISITED)
            }

            override fun onPostExecute(result: Unit?) {
                articleListFragment.updateModel(lastArticleId)
            }
        }.execute()
    }

    fun setListMenu() {
        menuManager.setListMenu()
    }

    fun setChartMenu() {
        menuManager.setChartMenu()
    }

    fun setItemMenu() {
        menuManager.setItemMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuManager = MenuManager(menu)
        setListMenu()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.action_add -> {
                setToFavorites(true)
            }
            R.id.action_remove -> {
                setToFavorites(false)
            }
            R.id.action_clear_favorites -> {
                clearHistory(Table.FAVORITES)
            }
            R.id.action_clear_visited -> {
                clearHistory(Table.VISITED)
            }
            R.id.action_show_chart -> {
                showChart()
            }
        }
        return true
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
                setItemMenu()
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
        val fragment = supportFragmentManager.findFragmentById(R.id.content_container)
        if (fragment != null) {
            (fragment as AnimatedFragment).removeSelf(supportFragmentManager)
            return
        }
        super.onBackPressed()
    }
}
