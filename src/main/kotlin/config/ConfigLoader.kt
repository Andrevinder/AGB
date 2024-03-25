package config

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileWriter

object ConfigLoader {

    var config: Map<String, Any>? = null

    fun checkConfigFile(): Boolean {
        val configFile = File("agb_config.yaml")
        if (configFile.exists() && !configFile.isDirectory)
            return true
        configFile.createNewFile()
        val bis = FileWriter(configFile)
        bis.write(defaultFileContent)
        bis.close()
        return false
    }

    fun loadConfig() {
        val configFile = File("agb_config.yaml")
        val yaml = Yaml()
        val inputStream = configFile.inputStream()
        config = yaml.load(inputStream)
        inputStream.close()
    }
}

val defaultFileContent = """
###Bot configuration

#Telegram bot http token
token: '<your bot token>'

#Telegram chat id
chatId: <log chat id>

###GitHub authorization

repoLink: '<remote repo link here>'

#Can be 'token' 'credentials' or 'none'
authMethod: 'token'

login: '<login here>'
password: '<password here>'

githubToken: '<your github token here>'

###Backup configuration

#Path to the repo folder
folder: '<your repo folder here>'

#Time between automatic backups (in minutes)
sleep: 180
""".trimIndent()