package com.example.data.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

/**
 * Backend JSON is intentionally flexible: some providers send numbers as strings,
 * some fields are empty, and some country cache items mix movies/series/trailers.
 * These adapters keep the TV app stable instead of failing deserialization.
 */
class TolerantJsonAdapters {
    @FromJson
    fun stringFromJson(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.NUMBER -> reader.nextString()
            JsonReader.Token.BOOLEAN -> reader.nextBoolean().toString()
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @FromJson
    fun intFromJson(reader: JsonReader): Int? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.NUMBER -> reader.nextDouble().toInt()
            JsonReader.Token.STRING -> reader.nextString().toDoubleOrNull()?.toInt()
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @FromJson
    fun doubleFromJson(reader: JsonReader): Double? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.NUMBER -> reader.nextDouble()
            JsonReader.Token.STRING -> reader.nextString().toDoubleOrNull()
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @FromJson
    fun booleanFromJson(reader: JsonReader): Boolean? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NUMBER -> reader.nextDouble() != 0.0
            JsonReader.Token.STRING -> {
                when (reader.nextString().lowercase()) {
                    "true", "1", "yes", "active" -> true
                    "false", "0", "no", "inactive" -> false
                    else -> null
                }
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
