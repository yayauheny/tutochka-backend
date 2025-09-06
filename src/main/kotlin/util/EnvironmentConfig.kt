package yayauheny.by.util

object EnvironmentConfig {
    
    fun getString(key: String, default: String): String = 
        System.getenv(key) ?: default
    
    fun getInt(key: String, default: Int): Int = 
        System.getenv(key)?.toIntOrNull() ?: default
    
    fun getLong(key: String, default: Long): Long = 
        System.getenv(key)?.toLongOrNull() ?: default
    
    fun getBoolean(key: String, default: Boolean): Boolean = 
        System.getenv(key)?.toBooleanStrictOrNull() ?: default
}
