package net.gorceag.hnreader

import android.app.Fragment
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import net.gorceag.hnreader.db.HistoryApi
import net.gorceag.hnreader.db.HistoryDBHelper
import net.gorceag.hnreader.db.Table
import net.gorceag.hnreader.list.ArticleListAdapter
import net.gorceag.hnreader.model.Article
import java.util.*

/**
 * Created by slash on 2/6/17.
 */

open class ArticleListFragment() : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: ArticleListAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        var root = super.onCreateView(inflater, container, savedInstanceState)
        if (inflater != null) {
            root = inflater.inflate(R.layout.fragment_article_list, container, false)
            recyclerView = root.findViewById(R.id.recycle_view) as RecyclerView
        }
        return root
    }

    override fun onResume() {
        super.onResume()

        var request = StringRequest(Request.Method.GET, context.resources.getString(R.string.list_url), fun(response: String) {
            parseManualy(response)
            var articles = parseManualy(response)

            adapter = ArticleListAdapter(activity as MainActivity, articles)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(activity)
            parseHistory(articles)
        }, fun(error: VolleyError) {})

        val queue = Volley.newRequestQueue(activity)
        queue.add(request)
    }

    private fun parseHistory(articles: Array<Article>) {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                val visited = HistoryApi.getList(Table.VISITED)
                val favorites = HistoryApi.getList(Table.FAVORITES)
                populateMatching(visited, favorites, articles)
            }

            private fun populateMatching(visited: ArrayList<String>, favorites: ArrayList<String>, articles: Array<Article>) {
                for (article in articles) {
                    for (visit in visited) {
                        if (article.id.equals(visit)) {
                            article.isVisited = true
                            break
                        }
                    }
                    for (favorit in favorites) {
                        if (article.id.equals(favorit)) {
                            article.isFavorite = true
                            break
                        }
                    }
                }
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                adapter.notifyDataSetChanged()
            }
        }.execute()
    }

    fun parseManualy(response: String): Array<Article> {  // a lot faster than with Gson
        val ids = response.substring(2, response.length - 3).split(", ")
        return Array(ids.size, { i -> Article(ids[i]) })
    }

    fun updateModel(id: String) {

        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                val isFavorite = HistoryApi.isInTable(id, Table.FAVORITES)
                val isVisited = HistoryApi.isInTable(id, Table.VISITED)
                val article = adapter.getArticle(id)
                if (article != null) {
                    article.isVisited = isVisited
                    article.isFavorite = isFavorite
                }
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                val index = adapter.getArticleIndex(id)
                if (index != null) {
                    adapter.notifyItemChanged(index)
                }
            }
        }.execute();
    }

    fun updateModel() {
        adapter.articles.forEach {
            it.isFavorite = false
            it.isVisited = false
        }
        parseHistory(adapter.articles)
    }
}