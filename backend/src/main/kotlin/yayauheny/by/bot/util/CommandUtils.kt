package yayauheny.by.bot.util

import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity

object CommandUtils {

    fun extractCommand(message: Message?): String? {
        if (message == null) return null

        val text = extractMessageText(message) ?: return null
        val entities = extractEntities(message) ?: return null

        for (entity in entities) {
            if (entity.type == "bot_command") {
                val offset = entity.offset
                val length = entity.length
                if (offset >= 0 && offset + length <= text.length) {
                    var command = text.substring(offset, offset + length)
                    val atIndex = command.indexOf('@')
                    if (atIndex > 0) {
                        command = command.substring(0, atIndex)
                    }
                    return command
                }
            }
        }
        return null
    }

    fun extractCommandArgument(message: Message?): String? {
        if (message == null) return null

        val text = extractMessageText(message)
        val entities = extractEntities(message)
        if (text == null || entities == null) return null

        for (entity in entities) {
            if (entity.type != "bot_command") continue

            val commandEnd = entity.offset + entity.length
            if (commandEnd >= text.length) return null

            val argument = text.substring(commandEnd).trim()
            return argument.ifBlank { null }
        }
        return null
    }

    fun isCommand(message: Message?): Boolean {
        if (message == null) return false

        val entities = extractEntities(message) ?: return false
        return entities.any { it.type == "bot_command" }
    }

    private fun extractMessageText(message: Message): String? {
        return message.text ?: message.caption
    }

    private fun extractEntities(message: Message): List<MessageEntity>? {
        return message.entities ?: message.captionEntities
    }
}
