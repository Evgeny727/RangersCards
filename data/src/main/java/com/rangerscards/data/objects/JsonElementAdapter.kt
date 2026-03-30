package com.rangerscards.data.objects

import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlin.collections.iterator

object JsonElementAdapter : Adapter<JsonElement> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): JsonElement {
        return readJsonElement(reader)
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: JsonElement) {
        writeJsonElement(writer, value)
    }

    // Recursively reads from the JsonReader and builds a JsonElement.
    private fun readJsonElement(reader: JsonReader): JsonElement {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_OBJECT -> {
                val map = mutableMapOf<String, JsonElement>()
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    map[name] = readJsonElement(reader)
                }
                reader.endObject()
                JsonObject(map)
            }
            JsonReader.Token.BEGIN_ARRAY -> {
                val list = mutableListOf<JsonElement>()
                reader.beginArray()
                while (reader.hasNext()) {
                    list.add(readJsonElement(reader))
                }
                reader.endArray()
                JsonArray(list)
            }
            JsonReader.Token.STRING -> JsonPrimitive(reader.nextString())
            JsonReader.Token.BOOLEAN -> JsonPrimitive(reader.nextBoolean())
            JsonReader.Token.NUMBER -> {
                // Use nextDouble() to read the number.
                // (If you need more precise handling, you could use reader.nextNumber() if available.)
                JsonPrimitive(reader.nextInt())
            }
            JsonReader.Token.NULL -> {
                reader.nextNull()
                JsonNull
            }
            else -> {
                reader.skipValue()
                JsonNull
            }
        }
    }

    // Recursively writes the JsonElement using the JsonWriter streaming API.
    private fun writeJsonElement(writer: JsonWriter, element: JsonElement) {
        when (element) {
            is JsonNull -> {
                writer.beginObject()
                writer.endObject()
            }
            is JsonObject -> {
                writer.beginObject()
                for ((key, value) in element) {
                    writer.name(key)
                    writeJsonElement(writer, value)
                }
                writer.endObject()
            }
            is JsonArray -> {
                writer.beginArray()
                for (item in element) {
                    writeJsonElement(writer, item)
                }
                writer.endArray()
            }
            is JsonPrimitive -> {
                if (element.isString) {
                    writer.value(element.content)
                } else if (element.booleanOrNull != null) {
                    writer.value(element.boolean)
                } else if (element.intOrNull != null) {
                    writer.value(element.int)
                } else {
                    // Fallback: write the content as a string
                    writer.value(element.content)
                }
            }
        }
    }
}