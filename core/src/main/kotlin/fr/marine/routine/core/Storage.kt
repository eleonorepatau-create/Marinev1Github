package fr.marine.routine.core

import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.security.SecureRandom

/** Encodage JSON des réglages et des déroulements (stockage local hors ligne). */
object MarineJson {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun encodeSettings(settings: Settings): String = json.encodeToString(Settings.serializer(), settings)

    fun decodeSettings(text: String): Settings = json.decodeFromString(Settings.serializer(), text)

    fun encodeDayState(state: DayState): String = json.encodeToString(DayState.serializer(), state)

    fun decodeDayState(text: String): DayState = json.decodeFromString(DayState.serializer(), text)
}

/** Code parent stocké sous forme d'empreinte salée (jamais en clair). */
object ParentPin {
    fun create(pin: String, random: SecureRandom = SecureRandom()): PinHash {
        val saltBytes = ByteArray(16).also(random::nextBytes)
        val salt = saltBytes.toHex()
        return PinHash(salt, hash(salt, pin))
    }

    fun matches(stored: PinHash, pin: String): Boolean =
        MessageDigest.isEqual(stored.hash.toByteArray(), hash(stored.salt, pin).toByteArray())

    fun isValidFormat(pin: String): Boolean = pin.length in 4..8 && pin.all(Char::isDigit)

    private fun hash(salt: String, pin: String): String {
        var bytes = (salt + pin).toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        repeat(10_000) { bytes = digest.digest(bytes) }
        return bytes.toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
