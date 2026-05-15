package yayauheny.by.importing.provider

import kotlinx.serialization.json.JsonObject
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.model.import.NormalizedRestroomCandidate

interface Parser<T> {
    fun parse(jsonObject: JsonObject): T

    fun toCommonModel(
        providerDto: T,
        context: ImportAdapterContext
    ): NormalizedRestroomCandidate

    fun convert(
        jsonObject: JsonObject,
        context: ImportAdapterContext
    ): NormalizedRestroomCandidate = toCommonModel(parse(jsonObject), context)
}
