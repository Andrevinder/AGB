package log

import config.ConfigLoader
import telegram.Sender

object LoggerUtil {
    fun printAndSend(message: String) {
        println(message)
        try {
            if (Sender.bot != null && ConfigLoader.config != null) {
                val chatId = ConfigLoader.config!!["chatId"] as Long
                Sender.bot!!.sendText(chatId, message)
            }
        } catch (exc: Exception) {
            print("Failed to send a message.")
            exc.printStackTrace()
        }
    }
}