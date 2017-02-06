package net.gorceag.hnreader

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import net.gorceag.hnreader.model.Article
import java.lang.reflect.Type

/**
 * Created by slash on 2/6/17.
 */

class SimpleArticleDeserializer : JsonDeserializer<Article> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Article {

        return Article(json.asJsonPrimitive.asString)
    }
}