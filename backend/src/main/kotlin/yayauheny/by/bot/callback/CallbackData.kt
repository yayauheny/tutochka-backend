package yayauheny.by.bot.callback

object CallbackData {
    fun detail(id: String): String = "detail:$id"
    
    fun backToList(): String = "back:list"
    
    fun route(provider: String, restroomId: String): String = "route:$provider:$restroomId"
    
    fun isType(data: String, type: String): Boolean = data.startsWith("$type:")
    
    fun arg(data: String): String {
        val i = data.indexOf(':')
        return if (i >= 0) data.substring(i + 1) else ""
    }
    
    fun routeProvider(data: String): String {
        val parts = data.split(":", limit = 3)
        return if (parts.size >= 2) parts[1] else ""
    }
    
    fun routeRestroomId(data: String): String {
        val parts = data.split(":", limit = 3)
        return if (parts.size == 3) parts[2] else ""
    }
}
