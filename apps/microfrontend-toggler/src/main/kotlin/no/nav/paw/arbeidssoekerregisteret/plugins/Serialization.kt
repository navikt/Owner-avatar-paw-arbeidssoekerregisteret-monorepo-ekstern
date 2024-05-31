package no.nav.paw.arbeidssoekerregisteret.plugins

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            registerModule(JavaTimeModule())
            registerModule(buildExtraModule())
            registerKotlinModule {
                disable(KotlinFeature.NullIsSameAsDefault)
                disable(KotlinFeature.SingletonSupport)
                disable(KotlinFeature.StrictNullChecks)
                enable(KotlinFeature.NullToEmptyCollection)
                enable(KotlinFeature.NullToEmptyMap)
            }
        }
    }
}

private fun buildExtraModule(): Module {
    return SimpleModule("ExtraModule")
        .addSerializer(HttpStatusCode::class.java, HttpStatusCodeSerializer())
        .addDeserializer(HttpStatusCode::class.java, HttpStatusCodeDeserializer())
}

class HttpStatusCodeSerializer : JsonSerializer<HttpStatusCode>() {
    override fun serialize(value: HttpStatusCode?, generator: JsonGenerator, serializers: SerializerProvider) {
        if (value != null) {
            generator.writeNumber(value.value)
        }
    }
}

class HttpStatusCodeDeserializer : JsonDeserializer<HttpStatusCode>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): HttpStatusCode {
        val status = parser.valueAsInt
        return HttpStatusCode.fromValue(status)
    }
}
