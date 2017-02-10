package net.gorceag.hnreader.list

import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
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
import com.google.gson.GsonBuilder
import net.gorceag.hnreader.ArticleDeserializer
import net.gorceag.hnreader.MainActivity
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by slash on 2/6/17.
 */

class ArticleListAdapter(val context: MainActivity, val articles: Array<Article>) : RecyclerView.Adapter<ArticleListAdapter.ViewHolder>() {
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.US)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById(R.id.title_text) as TextView
        private val scoreText = itemView.findViewById(R.id.score_text) as TextView
        private val authorText = itemView.findViewById(R.id.author_text) as TextView
        private val commentsText = itemView.findViewById(R.id.comments_text) as TextView
        private val dateText = itemView.findViewById(R.id.date_text) as TextView
        private val timeText = itemView.findViewById(R.id.time_text) as TextView

        init {
            itemView.setOnClickListener {

                val article = articles[adapterPosition]
                if (article.hasFullData && article.url != "") {

//                    var dimens = IntArray(2)
//                    getLocationInWindow(dimens)
                    val height = itemView.height - context.resources.getDimension(R.dimen.list_separator_height)
                    val coords = arrayOf(itemView.y, itemView.y + height)
                        context.showDetail(article.id, article.url, coords)
                }
            }
        }

        private fun updateView(article: Article) {
            titleText.text = article.title
            scoreText.text = article.score.toString()
            authorText.text = article.by
            commentsText.text = article.comments.toString()
            showDateTime(article)
            showHistory(article)
        }

        private fun showDateTime(article: Article) {
            val date = Date()
            date.time = article.time
            if (article.time == 0L) {
                dateText.text = ""
                timeText.text = ""
            } else {
                dateText.text = dateFormat.format(date)
                timeText.text = timeFormat.format(date)
            }
        }

        private fun showHistory(article: Article) {
            if (article.isFavorite) {
                titleText.setTextColor(ContextCompat.getColor(context, R.color.green))
            } else if (article.isVisited) {
                titleText.setTextColor(ContextCompat.getColor(context, R.color.light))
            } else {
                titleText.setTextColor(ContextCompat.getColor(context, R.color.dark))
            }
        }

        private fun parseDownloadedData(response: String, article: Article) {
            object : AsyncTask<String, Void, Unit>() {
                override fun doInBackground(vararg params: String?) {
                    parseToArticle(response)
                }

                private fun parseToArticle(response: String) {
                    val builder = GsonBuilder()
                    builder.registerTypeAdapter(Article::class.java, ArticleDeserializer())
                    val gson = builder.create()
                    val parsedArticle = gson.fromJson(response, Article::class.java)
                    parsedArticle.replicate(article)
                }

                override fun onPostExecute(result: Unit?) {
                    updateView(article)
                }
            }.execute()
        }

        private fun getListItemUrl(article: Article): String {
            val head = context.resources.getString(R.string.list_item_url_head)
            val tail = context.resources.getString(R.string.list_item_url_tail)
            return head + article.id + tail
        }

        private fun downloadData(article: Article) {
            val request = StringRequest(Request.Method.GET, getListItemUrl(article), fun(response: String) {
                parseDownloadedData(response, article)
            }, fun(error: VolleyError) {})

            Volley.newRequestQueue(context).add(request)
        }

        fun updateView(index: Int) {
            val article = articles[index]
            updateView(article)
            if (!article.hasFullData) {
                downloadData(article)
            }
        }
    }

    fun getArticle(id: String): Article? = articles.firstOrNull { it.id == id }

    fun getArticleIndex(id: String): Int? {
        for ((index, article) in articles.withIndex()) if (article.id == id) return index
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.article_list_item, parent, false)
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.updateView(position)
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}