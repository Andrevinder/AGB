package telegram

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

object Sender {
    var bot: Bot? = null
    var botsApi: TelegramBotsApi? = null

    fun initialize(botToken: String) {
        botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        bot = Bot(botToken)
        botsApi!!.registerBot(bot)
    }
}