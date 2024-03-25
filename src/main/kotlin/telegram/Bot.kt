package telegram

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.bots.TelegramWebhookBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class Bot(botToken: String) : TelegramLongPollingBot(botToken) {
    override fun getBotUsername(): String {
        return "backupinfologgerbot"
    }

    override fun onUpdateReceived(update: Update?) {

    }

    fun sendText(receiver: Long, message: String) {
        val sm = SendMessage.builder()
            .chatId(receiver)
            .text(message).build()

        try {
            execute(sm)
        } catch (exc: TelegramApiException) {
            println("Failed to send message.")
            exc.printStackTrace()
        }
    }
}