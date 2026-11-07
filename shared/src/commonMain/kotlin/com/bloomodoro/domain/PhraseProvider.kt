package com.bloomodoro.domain

import com.bloomodoro.data.LocalStore
import com.bloomodoro.model.CustomPhrase
import com.bloomodoro.model.NotifyMoment
import com.bloomodoro.model.Phase
import kotlin.random.Random

class PhraseProvider(private val localStore: LocalStore) {

    fun all(): List<CustomPhrase> = defaults + localStore.loadPhrases()

    fun userPhrases(): List<CustomPhrase> = localStore.loadPhrases()

    fun addPhrase(text: String, moment: NotifyMoment, phase: Phase) {
        val current = localStore.loadPhrases()
        val newPhrase = CustomPhrase(
            id = "u_${Random.nextLong()}",
            text = text,
            moment = moment,
            phase = phase,
        )
        localStore.savePhrases(current + newPhrase)
    }

    fun updatePhrase(id: String, newText: String) {
        val updated = localStore.loadPhrases().map { if (it.id == id) it.copy(text = newText) else it }
        localStore.savePhrases(updated)
    }

    fun deletePhrase(id: String) {
        localStore.savePhrases(localStore.loadPhrases().filterNot { it.id == id })
    }

    /** Picks one line for this exact moment — prefers the user's own words when any exist for that slot. */
    fun pickFor(phase: Phase, moment: NotifyMoment): String {
        val ownMatches = localStore.loadPhrases().filter { it.phase == phase && it.moment == moment }
        if (ownMatches.isNotEmpty()) return ownMatches.random().text
        val fallback = defaults.filter { it.phase == phase && it.moment == moment }
        return fallback.randomOrNull()?.text ?: defaultGeneric(moment)
    }

    private fun defaultGeneric(moment: NotifyMoment): String = when (moment) {
        NotifyMoment.START -> "Let's go! 🌱"
        NotifyMoment.MIDPOINT -> "Halfway there, keep going ✨"
        NotifyMoment.END -> "Done! Proud of you 🌸"
    }

    companion object {
        private val defaults = listOf(
            CustomPhrase("e1", "Time to focus, just one sip at a time 🍅", NotifyMoment.START, Phase.WORK, false),
            CustomPhrase("e2", "Put the phone down, this is your time 💪", NotifyMoment.START, Phase.WORK, false),
            CustomPhrase("e3", "Halfway through, a little more to go, breathe 🌿", NotifyMoment.MIDPOINT, Phase.WORK, false),
            CustomPhrase("e4", "You're doing great, keep going ✨", NotifyMoment.MIDPOINT, Phase.WORK, false),
            CustomPhrase("e5", "Done! Another petal added to your garden 🌸", NotifyMoment.END, Phase.WORK, false),
            CustomPhrase("e6", "Nice, you finished another pomodoro 🎉", NotifyMoment.END, Phase.WORK, false),
            CustomPhrase("e7", "Time for a little break, go pour some tea ☕", NotifyMoment.START, Phase.SHORT_BREAK, false),
            CustomPhrase("e8", "Look away from the screen, stretch a bit 🧘", NotifyMoment.MIDPOINT, Phase.SHORT_BREAK, false),
            CustomPhrase("e9", "Break's over, ready for the next round? 🌷", NotifyMoment.END, Phase.SHORT_BREAK, false),
            CustomPhrase("e10", "A long break is yours, really relax 🛋️", NotifyMoment.START, Phase.LONG_BREAK, false),
            CustomPhrase("e11", "Still got time, take a walk outside 🚶", NotifyMoment.MIDPOINT, Phase.LONG_BREAK, false),
            CustomPhrase("e12", "Fully recharged! Onward to the next thing 🌻", NotifyMoment.END, Phase.LONG_BREAK, false),
        )
    }
}
