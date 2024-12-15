package furhatos.app.openaichat.flow

import furhatos.app.openaichat.flow.chatbot.MainChat
import furhatos.app.openaichat.setting.Persona

import furhatos.app.openaichat.setting.hostPersona
import furhatos.app.openaichat.setting.personas
import furhatos.flow.kotlin.*
import furhatos.records.Location

val saluti = listOf(
    "Ciao, benvenuto!",
    "Buongiorno, come stai?",
    "Salve, piacere di conoscerti",
    "Ciao, sono felice di incontrarti"
)

val Greeting = state(Parent) {

    onEntry {
        furhat.attend(users.userClosestToPosition(Location(0.0, 0.0, 0.5)))
        askForAnything(saluti.random())
        furhat.say("Sono Maurizio una persona che ama aiutare la gente, per farla stare meglio")
        if (furhat.askYN("Sei pronto per partire?") == true) {
                goto(ChoosePersona())
        } else {
            furhat.say("Va bene, procediamo.")
            goto(ChoosePersona())
        }
    }
}

var currentPersona: Persona = hostPersona
fun ChoosePersona() = state(Parent) {
    val persona = personas.firstOrNull { it.voice.first().isAvailable } ?: hostPersona
    onEntry {
        currentPersona = persona
        //activate(persona) // Attiva la persona selezionata
        goto(MainChat) // Vai alla conversazione principale
    }
}
