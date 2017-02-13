package net.gorceag.hnreader.model

import org.junit.Test

import org.junit.Assert.*

/**
 * Created by slash on 2/13/17.
 */
class ArticleTest {
    val errorMessage = "Article is not replecating correctly."

    val title: String = "articleTitle"
    val score: Int = 16
    val by: String = "articleAuthor"
    val comments: Int = 32
    val time: Long = 64
    val url: String = "articleUrl"

    @Test
    fun replicate() {
        val article = Article("123")
        article.title = title
        article.score = score
        article.by = by
        article.comments = comments
        article.time = time
        article.url = url

        val article2 = Article("456")
        article.replicate(article2)

        assertEquals(errorMessage, article2.title, title)
        assertEquals(errorMessage, article2.score, score)
        assertEquals(errorMessage, article2.by, by)
        assertEquals(errorMessage, article2.comments, comments)
        assertEquals(errorMessage, article2.time, time)
        assertEquals(errorMessage, article2.url, url)
    }
}