package com.bugenmode.abakycharov.mediahackaton.utils

import com.google.gson.*
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type

class DateSerializer : JsonSerializer<LocalDateTime> {
    private val zoneId = ZoneId.systemDefault()
    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_INSTANT
    }
    override fun serialize(src: LocalDateTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(FORMATTER.format(src.atZone(zoneId).toInstant()))
    }
}

class DateDeserializer : JsonDeserializer<LocalDateTime> {
    private val zoneId = ZoneId.systemDefault()
    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_INSTANT
    }
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime {
        return FORMATTER.parse(json.asString, Instant.FROM).atZone(zoneId).toLocalDateTime()
    }
}