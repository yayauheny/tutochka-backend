package yayauheny.by.testfixtures

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

object OsmOriginalRows {
    fun accessCustomersWheelchair(): JsonObject =
        row(
            nodeId = "4,115,239,922",
            geom = "POINT (3352438.796532056 7405810.459422194)",
            tags =
                mapOf(
                    "access" to "customers",
                    "unisex" to "yes",
                    "amenity" to "toilets",
                    "wheelchair" to "yes"
                )
        )

    fun bareAmenityOnly(): JsonObject =
        row(
            nodeId = "3,711,248,641",
            geom = "POINT (3351997.570598347 7411598.090041776)",
            tags = mapOf("amenity" to "toilets")
        )

    fun addressTaggedPaid(): JsonObject =
        row(
            nodeId = "1,343,018,832",
            geom = "POINT (3069841.8904259545 7142162.823587519)",
            tags =
                mapOf(
                    "fee" to "yes",
                    "unisex" to "yes",
                    "amenity" to "toilets",
                    "addr:city" to "Мінск",
                    "check_date" to "2023-02-09",
                    "wheelchair" to "yes",
                    "addr:street" to "праезд Чыжэўскіх",
                    "addr:country" to "BY",
                    "addr:postcode" to "220028",
                    "addr:housenumber" to "13"
                )
        )

    fun indoorWithAddress(): JsonObject =
        row(
            nodeId = "13,165,299,709",
            geom = "POINT (3063800.98241077 7154144.781827426)",
            tags =
                mapOf(
                    "level" to "1",
                    "access" to "yes",
                    "amenity" to "toilets",
                    "addr:floor" to "1",
                    "check_date" to "2025-09-23",
                    "addr:street" to "Татарская вуліца",
                    "addr:housenumber" to "2"
                )
        )

    fun indoorPaidWheelchair(): JsonObject =
        row(
            nodeId = "1,639,215,912",
            geom = "POINT (3360279.317699302 7399839.089663116)",
            tags =
                mapOf(
                    "fee" to "yes",
                    "level" to "1",
                    "access" to "yes",
                    "indoor" to "yes",
                    "unisex" to "yes",
                    "amenity" to "toilets",
                    "wheelchair" to "yes"
                )
        )

    fun pitlatrine(): JsonObject =
        row(
            nodeId = "6,793,048,772",
            geom = "POINT (3358410.875706082 7378665.789035133)",
            tags =
                mapOf(
                    "access" to "yes",
                    "unisex" to "yes",
                    "amenity" to "toilets",
                    "toilets:disposal" to "pitlatrine"
                )
        )

    fun femaleFree(): JsonObject =
        row(
            nodeId = "4,865,147,166",
            geom = "POINT (3388419.5825337986 7367931.988223511)",
            tags =
                mapOf(
                    "fee" to "no",
                    "access" to "yes",
                    "female" to "yes",
                    "amenity" to "toilets"
                )
        )

    fun accessPrivateChemical(): JsonObject =
        row(
            nodeId = "13,108,420,496",
            geom = "POINT (3029457.1049933457 7183085.391079405)",
            tags =
                mapOf(
                    "fee" to "no",
                    "access" to "private",
                    "unisex" to "yes",
                    "amenity" to "toilets",
                    "operator" to "Путевая машинная станция Радошковичи",
                    "wheelchair" to "no",
                    "drinking_water" to "no",
                    "toilets:disposal" to "chemical",
                    "toilets:position" to "urinal"
                )
        )

    fun sameBuildingLevelOne(): JsonObject =
        row(
            nodeId = "10,188,902,669",
            geom = "POINT (3377396.7376873987 7152059.568366067)",
            tags =
                mapOf(
                    "fee" to "no",
                    "level" to "1",
                    "access" to "customers",
                    "amenity" to "toilets",
                    "check_date" to "2022-11-16",
                    "wheelchair" to "no",
                    "addr:street" to "Першамайская вуліца",
                    "opening_hours" to "07:00-23:00",
                    "changing_table" to "yes",
                    "addr:housenumber" to "57",
                    "toilets:handwashing" to "yes"
                )
        )

    fun sameBuildingLevelTwo(): JsonObject =
        row(
            nodeId = "10,188,902,668",
            geom = "POINT (3377393.9324362306 7152059.984065017)",
            tags =
                mapOf(
                    "fee" to "no",
                    "level" to "2",
                    "access" to "customers",
                    "amenity" to "toilets",
                    "check_date" to "2022-11-16",
                    "wheelchair" to "yes",
                    "addr:street" to "Першамайская вуліца",
                    "opening_hours" to "07:00-23:00",
                    "changing_table" to "yes",
                    "addr:housenumber" to "57",
                    "toilets:handwashing" to "yes"
                )
        )

    fun wcNameOnly(): JsonObject =
        row(
            nodeId = "5,925,889,233",
            geom = "POINT (3062958.2493376685 7141140.324640105)",
            tags =
                mapOf(
                    "name" to "WC",
                    "entrance" to "yes"
                )
        )

    fun buildingToiletsOnly(): JsonObject =
        row(
            nodeId = "12,960,751,816",
            geom = "POINT (2933735.1112567238 6801099.099455815)",
            tags =
                mapOf(
                    "name" to "Надворный туалет (служебный)",
                    "building" to "toilets",
                    "building:levels" to "1"
                )
        )

    fun descriptionMatchedCampSite(): JsonObject =
        row(
            nodeId = "3,664,477,258",
            geom = "POINT (3042061.532637141 7479566.883078946)",
            tags =
                mapOf(
                    "fee" to "yes",
                    "name" to "Маямі (заказная турстаянка)",
                    "tourism" to "camp_site",
                    "description" to
                        "Заказная туристическая стоянка расположена на берегу озера Укля. Оснащена: беседкой, навесом для дров, туалетом, кострищем с лавками."
                )
        )

    fun withTopLevel(
        payload: JsonObject,
        values: Map<String, String>
    ): JsonObject =
        buildJsonObject {
            payload.forEach { (key, value) -> put(key, value) }
            values.forEach { (key, value) -> put(key, JsonPrimitive(value)) }
        }

    fun withTags(
        payload: JsonObject,
        values: Map<String, String>
    ): JsonObject {
        val existingTags: JsonObject =
            (payload["tags"] as? JsonPrimitive)
                ?.contentOrNull
                ?.let { Json.parseToJsonElement(it).jsonObject }
                ?: JsonObject(emptyMap())
        return buildJsonObject {
            payload.forEach { (key, value) ->
                if (key != "tags") {
                    put(key, value)
                }
            }
            put(
                "tags",
                JsonPrimitive(
                    buildJsonObject {
                        existingTags.forEach { (key, value) -> put(key, value) }
                        values.forEach { (key, value) -> put(key, JsonPrimitive(value)) }
                    }.toString()
                )
            )
        }
    }

    private fun row(
        nodeId: String,
        geom: String,
        tags: Map<String, String>
    ): JsonObject =
        buildJsonObject {
            put("node_id", nodeId)
            put("geom", geom)
            put("tags", JsonPrimitive(buildJsonObject { tags.forEach { (key, value) -> put(key, value) } }.toString()))
        }
}
