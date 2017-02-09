package net.gorceag.hnreader.model

/**
 * Created by slash on 2/6/17.
 */

class Article(val id: String) {
    var hasFullData = false
    var isVisited = false
    var isFavorite = false

    var title: String = ""
    var score: Int = 0
    var by: String = ""
    var comments: Int = 0
    var time: Long = 0
    var url: String = ""

    fun replicate(copyTo: Article) {
        copyTo.title = title
        copyTo.score = score
        copyTo.by = by
        copyTo.comments = comments
        copyTo.time = time
        copyTo.url = url
        copyTo.hasFullData = true
    }
}