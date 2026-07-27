package com.example.appgasto.data.backup

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter

class LocalDateTimeTypeAdapter : com.google.gson.TypeAdapter<LocalDateTime>() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        out.value(value?.format(formatter))
    }

    override fun read(`in`: JsonReader): LocalDateTime? {
        return when (`in`.peek()) {
            JsonToken.STRING -> {
                val string = `in`.nextString()
                if (string.isNullOrBlank()) null else LocalDateTime.parse(string, formatter)
            }
            JsonToken.BEGIN_OBJECT -> {
                `in`.beginObject()
                var year = 0; var month = 0; var day = 0
                var hour = 0; var minute = 0; var second = 0; var nano = 0
                while (`in`.hasNext()) {
                    when (`in`.nextName()) {
                        "date" -> {
                            `in`.beginObject()
                            while (`in`.hasNext()) {
                                when (`in`.nextName()) {
                                    "year" -> year = `in`.nextInt()
                                    "month" -> {
                                        if (`in`.peek() == JsonToken.STRING) {
                                            month = Month.valueOf(`in`.nextString().uppercase()).value
                                        } else {
                                            month = `in`.nextInt()
                                        }
                                    }
                                    "day" -> day = `in`.nextInt()
                                    else -> `in`.skipValue()
                                }
                            }
                            `in`.endObject()
                        }
                        "time" -> {
                            `in`.beginObject()
                            while (`in`.hasNext()) {
                                when (`in`.nextName()) {
                                    "hour" -> hour = `in`.nextInt()
                                    "minute" -> minute = `in`.nextInt()
                                    "second" -> second = `in`.nextInt()
                                    "nano" -> nano = `in`.nextInt()
                                    else -> `in`.skipValue()
                                }
                            }
                            `in`.endObject()
                        }
                        else -> `in`.skipValue()
                    }
                }
                `in`.endObject()
                LocalDateTime.of(year, month, day, hour, minute, second, nano)
            }
            else -> {
                `in`.skipValue()
                null
            }
        }
    }
}
