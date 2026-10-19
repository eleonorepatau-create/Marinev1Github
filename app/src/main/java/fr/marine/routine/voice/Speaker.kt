package fr.marine.routine.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Synthèse vocale Android (moteur installé sur le téléphone), en français.
 * Jalon 1 : la lecture automatique n'est garantie que lorsque l'application
 * est au premier plan et l'écran allumé.
 */
class Speaker(context: Context) : TextToSpeech.OnInitListener {

    enum class Status { STARTING, READY, NO_FRENCH_VOICE, UNAVAILABLE }

    var status: Status = Status.STARTING
        private set

    var onStatusChanged: ((Status) -> Unit)? = null

    private var rate: Float = 0.9f
    private var pending: String? = null
    private var counter = 0

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext, this)

    override fun onInit(result: Int) {
        status = if (result == TextToSpeech.SUCCESS) configureLanguage() else Status.UNAVAILABLE
        onStatusChanged?.invoke(status)
        if (status == Status.READY) {
            pending?.let { speak(it) }
        }
        pending = null
    }

    private fun configureLanguage(): Status {
        val res = tts.setLanguage(Locale.FRANCE)
        return if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            Status.NO_FRENCH_VOICE
        } else {
            tts.setSpeechRate(rate)
            Status.READY
        }
    }

    fun setRate(value: Float) {
        rate = value
        if (status == Status.READY) tts.setSpeechRate(value)
    }

    /** Lit [text] en interrompant la lecture précédente. */
    fun speak(text: String) {
        when (status) {
            Status.READY -> tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "marine-${counter++}")
            Status.STARTING -> pending = text
            else -> Unit
        }
    }

    fun stop() {
        if (status == Status.READY) tts.stop()
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
