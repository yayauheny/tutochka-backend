package yayauheny.by.util

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.Polygon
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.io.WKTWriter
import java.time.Instant
import java.util.UUID

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: UUID
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Instant
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

object PointSerializer : KSerializer<Point> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Point", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Point
    ) {
        val wktWriter = WKTWriter()
        encoder.encodeString(wktWriter.write(value))
    }

    override fun deserialize(decoder: Decoder): Point {
        val wktReader = WKTReader()
        return wktReader.read(decoder.decodeString()) as Point
    }
}

object PolygonSerializer : KSerializer<Polygon> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Polygon", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Polygon
    ) {
        val wktWriter = WKTWriter()
        encoder.encodeString(wktWriter.write(value))
    }

    override fun deserialize(decoder: Decoder): Polygon {
        val wktReader = WKTReader()
        return wktReader.read(decoder.decodeString()) as Polygon
    }
}
