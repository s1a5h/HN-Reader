package net.gorceag.hnreader.list

import android.content.Context
import android.os.AsyncTask
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import net.gorceag.hnreader.R
import net.gorceag.hnreader.model.Article
import android.view.LayoutInflater
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import net.gorceag.hnreader.MainActivity
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by slash on 2/6/17.
 */

class ArticleListAdapter(val context: MainActivity, val articles: Array<Article>) : RecyclerView.Adapter<ArticleListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView
        var dateText: TextView

        init {
            titleText = itemView.findViewById(R.id.title_text) as TextView
            dateText = itemView.findViewById(R.id.date_text) as TextView
            itemView.setOnClickListener {
                val article = articles.get(adapterPosition)
                if (article.isProcessed && !article.url.equals("")) {
                    context.showDetail(article)
                }}
        }

        private fun displayData(article: Article) {
            titleText.setText(article.title)

            var date = Date()
            date.time = article.time
//            dateText.setText(parser.format(date))
            dateText.setText(article.id)
        }

        private fun parse(article: Article, response: String) {
            val gson = Gson()
            val parsedArticle = gson.fromJson(response, Article::class.java)
            article.title = parsedArticle.title
            if (parsedArticle.url != null) {
                article.url = parsedArticle.url
            }
            article.time = parsedArticle.time
            article.isProcessed = true
        }

        private fun parseDownloaded(response: String, article: Article) {

            val parser = object : AsyncTask<String, Void, Unit>() {
                override fun doInBackground(vararg params: String?) {
                    parse(article, response)
                }

                override fun onPostExecute(result: Unit?) {
                    super.onPostExecute(result)
                    val currentIndex = adapterPosition
                    if (currentIndex < 0 || article.id.equals(articles.get(currentIndex).id)) {
                        displayData(article)
                    }
                }
            }
            parser.execute()
        }

        private fun downloadData(article: Article) {
            var request = StringRequest(Request.Method.GET, "https://hacker-news.firebaseio.com/v0/item/${article.id}.json?print=pretty", fun(response: String) {
                parseDownloaded(response, article)
            }, fun(error: VolleyError) {})
            queue.add(request)
        }

        fun populate(index: Int) {
            val article = articles.get(index)
            displayData(article)
            if (!article.isProcessed) {
                downloadData(article)
            }
        }
    }

    val queue = Volley.newRequestQueue(context)
    var parser = SimpleDateFormat("HH:mm:ss dd-MMM-yyyy")

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        var context: Context = this.context
        if (parent != null) {
            context = parent.getContext()
        }
        val inflater = LayoutInflater.from(context)

        val view = inflater.inflate(R.layout.article_list_item, parent, false)

        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.populate(position);
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}