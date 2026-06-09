package yayauheny.by.bot.keyboard

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import yayauheny.by.bot.callback.CallbackData
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.bot.service.BotStatsPeriod
import yayauheny.by.bot.util.Links
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto

@Component
class InlineKeyboardFactory(
    private val formatterService: FormatterService
) {

    fun toiletList(toilets: List<NearestRestroomSlimDto>): InlineKeyboardMarkup {
        val rows = toilets.map { createToiletButton(it) }
            .map { InlineKeyboardRow(listOf(it)) }

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build()
    }

    fun toiletDetail(toilet: RestroomResponseDto): InlineKeyboardMarkup {
        val rows = mutableListOf<InlineKeyboardRow>()

        addRow(
            rows,
            createMapButton(Messages.BUTTON_OPEN_YANDEX, yandexUrl(toilet)),
            createMapButton(Messages.BUTTON_OPEN_GOOGLE, googleUrl(toilet))
        )
        addRow(
            rows,
            createMapButton(Messages.BUTTON_OPEN_2GIS, twoGisUrl(toilet)),
            createMapButton(Messages.BUTTON_OPEN_APPLE, appleUrl(toilet))
        )
        rows.add(
            InlineKeyboardRow(
                listOf(
                    InlineKeyboardButton.builder()
                        .text(Messages.BUTTON_BACK)
                        .callbackData(CallbackData.backToList())
                        .build()
                )
            )
        )

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build()
    }

    fun analytics(): InlineKeyboardMarkup {
        val rows = mutableListOf<InlineKeyboardRow>()

        addRow(
            rows,
            createCallbackButton(Messages.BUTTON_STATS_TODAY, CallbackData.analytics(BotStatsPeriod.TODAY.key)),
            createCallbackButton(Messages.BUTTON_STATS_LAST_7_DAYS, CallbackData.analytics(BotStatsPeriod.LAST_7_DAYS.key))
        )
        addRow(
            rows,
            createCallbackButton(Messages.BUTTON_STATS_CURRENT_MONTH, CallbackData.analytics(BotStatsPeriod.CURRENT_MONTH.key))
        )
        addRow(
            rows,
            createCallbackButton(Messages.BUTTON_BACK, CallbackData.analytics("back"))
        )

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build()
    }

    private fun createToiletButton(toilet: NearestRestroomSlimDto): InlineKeyboardButton {
        val buttonText = formatterService.toiletListItem(toilet)

        return InlineKeyboardButton.builder()
            .text(buttonText)
            .callbackData(CallbackData.detail(toilet.id.toString()))
            .build()
    }

    private fun addRow(rows: MutableList<InlineKeyboardRow>, vararg buttons: InlineKeyboardButton?) {
        val rowButtons = buttons.filterNotNull()
        if (rowButtons.isNotEmpty()) {
            rows.add(InlineKeyboardRow(rowButtons))
        }
    }

    private fun createCallbackButton(text: String, callbackData: String): InlineKeyboardButton {
        return InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build()
    }

    private fun createMapButton(text: String, url: String?): InlineKeyboardButton? {
        if (url.isNullOrBlank()) return null
        return InlineKeyboardButton.builder()
            .text(text)
            .url(url)
            .build()
    }

    protected fun yandexUrl(toilet: RestroomResponseDto): String? {
        return mapUrl(toilet, Links::yandexMaps)
    }

    protected fun googleUrl(toilet: RestroomResponseDto): String? {
        return mapUrl(toilet, Links::googleMaps)
    }

    protected fun appleUrl(toilet: RestroomResponseDto): String? {
        return mapUrl(toilet, Links::appleMaps)
    }

    protected fun twoGisUrl(toilet: RestroomResponseDto): String? {
        val externalMaps = toilet.externalMaps
        val branchId = extractTwoGisBranchId(externalMaps)
        if (!branchId.isNullOrBlank()) {
            return Links.twoGisById(branchId)
        }

        return Links.twoGis(toilet.coordinates.lat, toilet.coordinates.lon)
    }

    private fun mapUrl(toilet: RestroomResponseDto, builder: (Double, Double) -> String): String {
        return builder(toilet.coordinates.lat, toilet.coordinates.lon)
    }

    private fun extractTwoGisBranchId(externalMaps: Map<String, Any>?): String? {
        if (externalMaps == null) {
            return null
        }

        val twoGis = externalMaps["2gis"]
        if (twoGis is String) {
            return twoGis
        }

        if (twoGis is Map<*, *>) {
            val branchId = twoGis["branch_id"]
            if (branchId is String) {
                return branchId
            }
        }

        return null
    }
}
