package net.gorceag.hnreader

import com.google.gson.*
import net.gorceag.hnreader.model.Article
import java.lang.reflect.Type

/**
 * Created by slash on 2/6/17.
 */

class ArticleDeserializer : JsonDeserializer<Article> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Article {
        val decodeObj = json.getAsJsonObject();
        val gson = Gson()
        val parsedArticle = gson.fromJson(decodeObj, Article::class.java)
        val kidsString = decodeObj.get("kids")
        if (kidsString != null) {
            val kids = gson.fromJson(kidsString.asJsonArray, Array<String>::class.java)
            parsedArticle.comments = kids.size
        }
        return parsedArticle
    }
}