package furhatos.app.openaichat.flow.chatbot

import furhatos.app.openaichat.flow.*

import furhatos.app.openaichat.setting.hostPersona
import furhatos.app.openaichat.setting.personas
import furhatos.flow.kotlin.*
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes
import furhatos.flow.kotlin.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import furhatos.app.openaichat.audiofeed.FurhatAudioFeedRecorder
import furhatos.app.openaichat.audiofeed.FurhatAudioFeedStreamer
import java.io.File

// Definisci la classe per i log della conversazione
@Serializable
data class ConversationLog(val speaker: String, val text: String, val timestamp: Long)

val conversationHistory = mutableListOf<ConversationLog>()
val streamer = FurhatAudioFeedStreamer()
val MainChat = state(Parent) {

    streamer.start("192.168.1.75")
    println("registra")
    fun recordAudio(streamer: FurhatAudioFeedStreamer) {
        val recorder = FurhatAudioFeedRecorder(streamer)
        recorder.startRecordAll(File("recording.wav"))
    }
    recordAudio(streamer)

    onEntry {
        Furhat.dialogHistory.clear()
        furhat.say("Adesso ti farò qualche domanda per conoscerti meglio")
        logConversation("Furhat", "Adesso ti farò qualche domanda per conoscerti meglio")

        reentry()
    }

    onReentry {
        furhat.listen()
    }

    onResponse("Possiamo fermarci?", "Arrivederci") {
        logConversation("User", it.text)  // Logga la risposta dell'utente

        furhat.say("Okay, Ciao")
        logConversation("Furhat", "Okay, ciao")

        furhat.say("Spero tu ti sia sentito a tuo agio")
        logConversation("Furhat", "Spero tu ti sia sentito a tuo agio")

        goto(AfterChat)
    }

    onResponse {
        logConversation("User", it.text)  // Logga la risposta dell'utente
        furhat.gesture(GazeAversion(2.0))
        val response = call {
            currentPersona.chatbot.getNextResponse()
        } as String
        furhat.say(response)
        logConversation("Furhat", response)
        reentry()
    }

    onNoResponse {
        reentry()
    }

}

// Funzione per loggare la conversazione con timestamp
fun logConversation(speaker: String, text: String) {
    val timestamp = System.currentTimeMillis()
    conversationHistory.add(ConversationLog(speaker, text, timestamp))
}

// Stato finale per esportare il JSON
val AfterChat = state(Parent) {
    onEntry {
        furhat.say("Conversazione conclusa.")
        exportConversationToJson()
        streamer.stop()

        goto(Idle)
    }
}

// Funzione per esportare la conversazione in JSON
fun exportConversationToJson() {
    val jsonString = Json.encodeToString(conversationHistory)
    val file = File("conversation_log.json")
    file.writeText(jsonString)
}
