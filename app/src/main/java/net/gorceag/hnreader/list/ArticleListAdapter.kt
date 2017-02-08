package net.gorceag.hnreader.list

import android.content.Context
import android.os.AsyncTask
import android.os.Looper
import android.support.v4.content.ContextCompat
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
import com.google.gson.GsonBuilder
import net.gorceag.hnreader.ArticleDeserializer
import net.gorceag.hnreader.MainActivity
import net.gorceag.hnreader.db.HistoryApi
import net.gorceag.hnreader.db.Table
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by slash on 2/6/17.
 */

val dateFormat = SimpleDateFormat("dd MMM, yyyy")
val timeFormat = SimpleDateFormat("HH:mm")

class ArticleListAdapter(val context: MainActivity, val articles: Array<Article>) : RecyclerView.Adapter<ArticleListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var container: ViewGroup
        var titleText: TextView
        var scoreText: TextView
        var authorText: TextView
        var commentsText: TextView
        var dateText: TextView
        var timeText: TextView

        init {
            container = itemView.findViewById(R.id.container) as ViewGroup
            titleText = itemView.findViewById(R.id.title_text) as TextView
            scoreText = itemView.findViewById(R.id.score_text) as TextView
            authorText = itemView.findViewById(R.id.author_text) as TextView
            commentsText = itemView.findViewById(R.id.comments_text) as TextView
            dateText = itemView.findViewById(R.id.date_text) as TextView
            timeText = itemView.findViewById(R.id.time_text) as TextView
            itemView.setOnClickListener {
                val article = articles.get(adapterPosition)
                if (article.isProcessed && !article.url.equals("")) {
                    context.showDetail(article.id, article.url)
                }
            }
        }

        private fun displayData(article: Article) {
            if (article.isFavorite) {
                titleText.setTextColor(ContextCompat.getColor(context, R.color.green))
//                container.setBackgroundColor(ContextCompat.getColor(context, R.color.light_green))
            } else if (article.isVisited) {
                titleText.setTextColor(ContextCompat.getColor(context, R.color.light))
//                container.setBackgroundColor(ContextCompat.getColor(context, R.color.dark))
            } else {
                titleText.setTextColor(ContextCompat.getColor(context, R.color.dark))
            }
            titleText.setText(article.title)
            scoreText.setText(article.score.toString())
            authorText.setText(article.by)
            commentsText.setText(article.comments.toString())
            var date = Date()
            date.time = article.time
            if (article.time == 0L) {
                dateText.setText("")
                timeText.setText("")
            } else {
                dateText.setText(dateFormat.format(date))
                timeText.setText(timeFormat.format(date))
            }
        }

        private fun parse(article: Article, response: String) {
            val builder = GsonBuilder()
            builder.registerTypeAdapter(Article::class.java, ArticleDeserializer())
            val gson = builder.create()
            val parsedArticle = gson.fromJson(response, Article::class.java)
            parsedArticle.replicate(article)
//            article.title = parsedArticle.title
//            if (parsedArticle.url != null) {
//                article.url = parsedArticle.url
//            }
//            article.time = parsedArticle.time
//            article.isProcessed = true
        }

        private fun parseDownloaded(response: String, article: Article) {
            object : AsyncTask<String, Void, Unit>() {
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
            }.execute()
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

    fun getArticleList(): Array<Article>{
        return articles
    }

    fun getArticle(id: String): Article? {
        for (article in articles) {
            if (article.id.equals(id)) {
                return article
            }
        }
        return null
    }

    fun getArticleIndex(id: String): Int? {
        for ((index, article) in articles.withIndex()) {
            if (article.id.equals(id)) {
                return index
            }
        }
        return null
    }
}