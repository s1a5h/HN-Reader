package net.gorceag.hnreader.list

import net.gorceag.hnreader.MainActivity
import net.gorceag.hnreader.model.Article
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by slash on 2/9/17.
 */

class ArticleListAdapterTest {
    private val firstId = "123"
    private val secondId = "456"
    private val firstTitle = "first article"
    private val secondTitle = "second article"
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.US)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    private lateinit var article1: Article
    private lateinit var article2: Article
    private lateinit var adapter: ArticleListAdapter

    @Before
    fun setUp() {
        article1 = populateArticle1()
        article2 = populateArticle2()
        adapter = ArticleListAdapter(MainActivity(), arrayOf(article1, article2))
    }

    private fun populateArticle1(): Article {
        val article = Article(firstId)
        article.title = firstTitle
        article.hasFullData = true
        return article
    }

    private fun populateArticle2(): Article {
        val article = Article(secondId)
        article.title = secondTitle
        article.hasFullData = true
        return article
    }

    @After
    fun tearDown() {

    }

    @Test
    fun getDateFormat() {
        assertEquals("Incorrect data Format used.", adapter.dateFormat, dateFormat)
    }

    @Test
    fun getTimeFormat() {
        assertEquals("Incorrect time Format used.", adapter.timeFormat, timeFormat)
    }

    @Test
    fun getArticle() {
        assertEquals("Returned the wrong article from the list", adapter.getArticle(firstId), article1)
        assertEquals("Returned the wrong article from the list", adapter.getArticle(secondId), article2)

    }

    @Test
    fun getArticleIndex() {
        assertEquals("Articles in the list are in the wrong order", adapter.getArticleIndex(firstId), 0)
        assertEquals("Articles in the list are in the wrong order", adapter.getArticleIndex(secondId), 1)
    }

    @Test
    fun getItemCount() {
        assertEquals("Articles in the list are in the wrong order", adapter.getArticleIndex(firstId), 0)
        assertEquals("Articles in the list are in the wrong order", adapter.getArticleIndex(secondId), 1)
    }

    @Test
    fun getArticles() {
        val data = arrayOf(article1, article2)
        assertArrayEquals(data, adapter.articles)
    }
}