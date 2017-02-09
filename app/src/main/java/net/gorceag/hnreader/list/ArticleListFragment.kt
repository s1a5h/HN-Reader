package net.gorceag.hnreader.list

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import net.gorceag.hnreader.MainActivity
import net.gorceag.hnreader.R
import net.gorceag.hnreader.db.HistoryApi
import net.gorceag.hnreader.db.Table
import net.gorceag.hnreader.model.Article
import java.util.*

/**
 * Created by slash on 2/6/17.
 */

class ArticleListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArticleListAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater?.inflate(R.layout.fragment_article_list, container, false)
        recyclerView = root?.findViewById(R.id.recycle_view) as RecyclerView
        return root
    }

    override fun onResume() {
        super.onResume()
        val queue = Volley.newRequestQueue(activity)
        queue.add(buildRequest())
    }

    private fun buildRequest(): StringRequest {
        return StringRequest(Request.Method.GET, context.resources.getString(R.string.list_url), fun(response: String) {
            val articles = parseManualy(response)
            setAdapter(articles)
        }, fun(error: VolleyError) {})
    }

    private fun setAdapter(articles: Array<Article>) {
        adapter = ArticleListAdapter(activity as MainActivity, articles)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        parseHistory(articles)
    }

    private fun parseHistory(articles: Array<Article>) {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                val visited = HistoryApi.getList(Table.VISITED)
                val favorites = HistoryApi.getList(Table.FAVORITES)
                updateHistory(visited, favorites, articles)
            }

            private fun updateHistory(visited: ArrayList<String>, favorites: ArrayList<String>, articles: Array<Article>) {
                articles.forEach {
                    updateVisited(it, visited)
                    updateFavorite(it, favorites)
                }
            }

            private fun updateVisited(article: Article, visited: ArrayList<String>) {
                val id = visited.firstOrNull {it == article.id}
                if (id != null) article.isVisited = true
            }

            private fun updateFavorite(article: Article, favorites: ArrayList<String>) {
                val id = favorites.firstOrNull {it == article.id}
                if (id != null) article.isVisited = true
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                adapter.notifyDataSetChanged()
            }
        }.execute()
    }

    private fun parseManualy(response: String): Array<Article> {  // a lot faster than with Gson
        val ids = response.substring(2, response.length - 3).split(", ")
        return Array(ids.size, { i -> Article(ids[i]) })
    }

    fun updateModel(id: String) {
        object : AsyncTask<String, Void, Unit>() {
            override fun doInBackground(vararg params: String?) {
                val isFavorite = HistoryApi.isInTable(id, Table.FAVORITES)
                val isVisited = HistoryApi.isInTable(id, Table.VISITED)
                updateArticle(isFavorite, isVisited, id)
            }

            private fun updateArticle(isFavorite: Boolean, isVisited: Boolean, id: String) {
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
        }.execute()
    }

    fun updateModel() {
        clearArticlesHistory()
        parseHistory(adapter.articles)
    }

    private fun clearArticlesHistory() {
        adapter.articles.forEach {
            it.isFavorite = false
            it.isVisited = false
        }
    }
}