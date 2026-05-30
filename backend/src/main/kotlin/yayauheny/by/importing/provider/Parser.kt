package yayauheny.by.importing.provider

import com.fasterxml.jackson.databind.JsonNode
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.model.imports.NormalizedRestroomCandidate

interface Parser<T> {
    fun parse(jsonObject: JsonNode): T

    fun toCommonModel(
        providerDto: T,
        context: ImportAdapterContext
    ): NormalizedRestroomCandidate

    fun convert(
        jsonObject: JsonNode,
        context: ImportAdapterContext
    ): NormalizedRestroomCandidate = toCommonModel(parse(jsonObject), context)
}
