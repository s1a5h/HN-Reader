package net.gorceag.hnreader

import org.junit.Test
import org.junit.Assert.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.gorceag.hnreader.model.Article
import org.junit.Before

/**
 * Created by slash on 2/13/17.
 */
class ArticleDeserializerTest {
    val errorMessage = "Article is not deserialized correctly."
    val rawJson = """{
    "by" : "fergeson",
    "descendants" : 18,
    "id" : 13631049,
    "kids" : [ 13631437, 13631775, 13631973, 13631276, 13631827, 13631299, 13631423, 13631300 ],
    "score" : 193,
    "time" : 1486935470,
    "title" : "Still True: The Public Must Fight for Its Right to Privacy",
    "type" : "story",
    "url" : "http://www.spiegel.de/international/world/public-must-fight-against-prism-and-tempora-surveillance-a-907495.html"
    }"""

    lateinit var article: Article

    @Before
    fun setUp() {
        article = Article("13631049")
        article.title = "Still True: The Public Must Fight for Its Right to Privacy"
        article.score = 193
        article.by = "fergeson"
        article.comments = 8
        article.time = 1486935470000
        article.url = "http://www.spiegel.de/international/world/public-must-fight-against-prism-and-tempora-surveillance-a-907495.html"
    }

    @Test
    fun deserialize() {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(Article::class.java, ArticleDeserializer())
        val gson = builder.create()
        val parsedArticle = gson.fromJson(rawJson, Article::class.java)
        assertEquals(errorMessage, article.id, parsedArticle.id)
        assertEquals(errorMessage, article.title, parsedArticle.title)
        assertEquals(errorMessage, article.score, parsedArticle.score)
        assertEquals(errorMessage, article.by, parsedArticle.by)
        assertEquals(errorMessage, article.comments, parsedArticle.comments)
        assertEquals(errorMessage, article.time, parsedArticle.time)
        assertEquals(errorMessage, article.url, parsedArticle.url)
    }
}

