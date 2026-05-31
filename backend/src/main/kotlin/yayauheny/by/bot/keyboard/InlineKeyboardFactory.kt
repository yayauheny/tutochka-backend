package yayauheny.by.bot.keyboard

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import yayauheny.by.bot.callback.CallbackData
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.bot.util.Links
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

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

    fun radiusSelection(): InlineKeyboardMarkup {
        val radius500 = InlineKeyboardButton.builder()
            .text(Messages.RADIUS_500M)
            .callbackData(CallbackData.radius(500))
            .build()

        val radius1km = InlineKeyboardButton.builder()
            .text(Messages.RADIUS_1KM)
            .callbackData(CallbackData.radius(1000))
            .build()

        val radius2km = InlineKeyboardButton.builder()
            .text(Messages.RADIUS_2KM)
            .callbackData(CallbackData.radius(2000))
            .build()

        val radius5km = InlineKeyboardButton.builder()
            .text(Messages.RADIUS_5KM)
            .callbackData(CallbackData.radius(5000))
            .build()

        return InlineKeyboardMarkup.builder()
            .keyboard(
                listOf(
                    InlineKeyboardRow(listOf(radius500, radius1km)),
                    InlineKeyboardRow(listOf(radius2km, radius5km))
                )
            )
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

        if (toilet.coordinates == null) {
            return null
        }

        return Links.twoGis(toilet.coordinates!!.lat, toilet.coordinates!!.lon)
    }

    private fun mapUrl(toilet: RestroomResponseDto, builder: (Double, Double) -> String): String? {
        if (toilet.coordinates == null) {
            return null
        }
        return builder(toilet.coordinates!!.lat, toilet.coordinates!!.lon)
    }

    private fun extractTwoGisBranchId(externalMaps: JsonObject?): String? {
        if (externalMaps == null) {
            return null
        }

        val twoGis = externalMaps["2gis"]
        if (twoGis is JsonPrimitive) {
            return twoGis.content
        }

        if (twoGis is JsonObject) {
            val branchId = twoGis["branch_id"]
            if (branchId is JsonPrimitive) {
                return branchId.content
            }
        }

        return null
    }
}
