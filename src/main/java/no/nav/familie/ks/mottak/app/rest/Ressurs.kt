package no.nav.familie.ks.mottak.app.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

data class Ressurs(
        val data: JsonNode?,
        val status: Status,
        val melding: String,
        val errorMelding: String?
) {
    enum class Status { SUKSESS, FEILET, IKKE_HENTET, IKKE_TILGANG }

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        inline fun <reified T> success(data: T): Ressurs {
            return Ressurs(
                data = objectMapper.valueToTree(data),
                status = Status.SUKSESS,
                melding = "Innhenting av data var vellykket",
                errorMelding = null
            )
        }

        fun failure(errorMessage: String? = null, error: Throwable? = null): Ressurs = Ressurs(
            data = null,
            status = Status.FEILET,
            melding = errorMessage ?: "Kunne ikke hente data: ${error?.message}",
            errorMelding = error?.message
        )

        fun ikkeTilgang(melding: String): Ressurs = Ressurs(
                data = null,
                status = Status.IKKE_TILGANG,
                melding = melding,
                errorMelding = ""
        )
    }
}
