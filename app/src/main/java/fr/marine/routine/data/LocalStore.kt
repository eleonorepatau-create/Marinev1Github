package fr.marine.routine.data

import android.content.Context
import android.util.AtomicFile
import fr.marine.routine.core.DayState
import fr.marine.routine.core.Defaults
import fr.marine.routine.core.MarineJson
import fr.marine.routine.core.Settings
import java.io.File

/**
 * Stockage local hors ligne, dans l'espace privé de l'application.
 * Écritures atomiques : un arrêt brutal ne laisse pas de fichier à moitié écrit.
 * Aucune donnée n'est envoyée hors de l'appareil.
 */
class LocalStore(context: Context) {
    private val settingsFile = AtomicFile(File(context.filesDir, "settings.json"))
    private val dayStateFile = AtomicFile(File(context.filesDir, "day_state.json"))

    fun loadSettings(): Settings =
        read(settingsFile)?.let { runCatching { MarineJson.decodeSettings(it) }.getOrNull() }
            ?: Defaults.settings()

    fun saveSettings(settings: Settings) = write(settingsFile, MarineJson.encodeSettings(settings))

    fun loadDayState(): DayState =
        read(dayStateFile)?.let { runCatching { MarineJson.decodeDayState(it) }.getOrNull() }
            ?: DayState()

    fun saveDayState(state: DayState) = write(dayStateFile, MarineJson.encodeDayState(state))

    private fun read(file: AtomicFile): String? =
        if (!file.baseFile.exists()) null else runCatching { file.readFully().decodeToString() }.getOrNull()

    private fun write(file: AtomicFile, text: String) {
        val out = file.startWrite()
        try {
            out.write(text.encodeToByteArray())
            file.finishWrite(out)
        } catch (e: Exception) {
            file.failWrite(out)
            throw e
        }
    }
}
