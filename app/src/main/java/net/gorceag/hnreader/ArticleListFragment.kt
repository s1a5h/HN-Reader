package net.gorceag.hnreader

import android.app.Fragment
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

        var request = StringRequest(Request.Method.GET, "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty", fun(response: String) {
            parseManualy(response)
            var articles = parseManualy(response)

            adapter = ArticleListAdapter(activity as MainActivity, articles)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(activity)
        }, fun(error: VolleyError) {})

        val queue = Volley.newRequestQueue(activity)
        queue.add(request)
    }

    fun parseWithGson(responce: String): Array<Article> {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(Article::class.java, SimpleArticleDeserializer())
        val gson = gsonBuilder.create()
        return gson.fromJson(responce, Array<Article>::class.java)
    }

    fun parseManualy(response: String): Array<Article> {
        val ids = response.substring(2, response.length - 3).split(", ")
        return Array(ids.size, { i -> Article(ids[i]) })
    }

}