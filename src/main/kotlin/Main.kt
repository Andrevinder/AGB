import config.ConfigLoader
import log.LoggerUtil
import repo.RepoManager
import telegram.Sender
import java.io.File
import sun.security.provider.MD5
import java.security.MessageDigest
import java.time.LocalTime
import javax.xml.bind.DatatypeConverter
import kotlin.system.exitProcess


var lastCommitTime: LocalTime? = null

fun getChecksum(md5: MessageDigest, folder: File) {
    for (file: File in folder.listFiles()!!){
        if (file.isFile) {
            val inps = file.inputStream()
            md5.update(inps.readBytes())
            inps.close()
        } else {
            if (file.name != ".git")
                getChecksum(md5, file)
        }
    }
}

fun checkChecksum(): Boolean {
    val lastChecksumFile = File("last_checksum.txt")
    val md5 = MessageDigest.getInstance("MD5")
    val folder = File(ConfigLoader.config!!["folder"] as String)
    getChecksum(md5, folder)
    val checksum = DatatypeConverter.printHexBinary(md5.digest()).toUpperCase()

    if (! lastChecksumFile.exists()) {
        lastChecksumFile.createNewFile()
        val outstr = lastChecksumFile.outputStream().bufferedWriter()
        outstr.write(checksum)
        outstr.close()
        return false
    } else {
        val inpstr = lastChecksumFile.inputStream().bufferedReader()
        val lastChecksum = inpstr.readText()
        inpstr.close()
        if (lastChecksum == checksum)
            return true
        else {
            val outstr = lastChecksumFile.outputStream().bufferedWriter()
            outstr.write(checksum)
            outstr.close()
            return false
        }
    }
}

fun commitAndPushAll() {
    println()
    val remoteURL = ConfigLoader.config!!["repoLink"] as String
    val login = ConfigLoader.config!!["login"] as String
    val password = ConfigLoader.config!!["password"] as String
    val token = ConfigLoader.config!!["githubToken"] as String
    val authMethod = ConfigLoader.config!!["authMethod"] as String

    try {
        if (checkChecksum()) {
            LoggerUtil.printAndSend("Checksums are identical. Not Proceeding with committing and pushing.")
            return
        }
    } catch (exc: Exception) {
        LoggerUtil.printAndSend("Failed to check for checksums, proceeding with committing and pushing.")
        exc.printStackTrace()
    }

    try {
        RepoManager.commitAll()
        LoggerUtil.printAndSend("Committed successfully!")
    } catch (exc: Exception){
        LoggerUtil.printAndSend("Something went wrong while trying to commit repo")
        exc.printStackTrace()
    }

    try {
        when (authMethod) {
            "token" -> RepoManager.pushWithGithubToken(remoteURL, token)
            "credentials" -> RepoManager.pushWithCredentials(remoteURL, login, password)
            "none" -> RepoManager.pushWithoutAuth(remoteURL)
            else -> {
                println("Invalid authentication method $authMethod, quitting.")
                isRunning = false
                return
            }
        }
        LoggerUtil.printAndSend("Pushed successfully!")
        lastCommitTime = LocalTime.now()
    } catch (exc: Exception) {
        LoggerUtil.printAndSend("Failed to push repo")
        LoggerUtil.printAndSend(exc.stackTraceToString())
    }
}

fun commitLoop() {
    val sleepTime = (ConfigLoader.config!!["sleep"] as Int) * 60000L
    while (true){
        commitAndPushAll()

        LoggerUtil.printAndSend("Waiting for $sleepTime milliseconds")
        print("> ")
        Thread.sleep(sleepTime)
    }
}

var isRunning = true

val helpmsg  = """
Commands list:
branchlist - lists all branches
restore <branch name> - srestores all files to the state in the selected branch
snapshot - saves files snapshot in their current state
timeleft - shows time until next commit
stop - quit the program
""".trimIndent()

fun main(args: Array<String>) {
    if (! ConfigLoader.checkConfigFile()){
        println("Config file wasn't detected.")
        println("Check your working directory for agb_config.yaml file")
        println("and follow the instructions in the file to configure it.")
        return
    }
    try {
        ConfigLoader.loadConfig()
        println("Config loaded successfully")
    } catch (exc: Exception) {
        println("Config file load failed. It seems like file is corrupted or it was set up in a wrong way.")
        exc.printStackTrace()
        return
    }


    try {
        val repoFolder = File(ConfigLoader.config!!["folder"] as String)
        RepoManager.initRepo(repoFolder)
        println("Initialized repo successfully!")
    } catch (exc: Exception){
        println("Unable to initialize repo.")
        exc.printStackTrace()
        return
    }

    try {
        val botToken = ConfigLoader.config!!["token"] as String
        Sender.initialize(botToken)
        println("Initialized bot successfully.")
    } catch (exc: Exception) {
        println("Failed to initialize bot.")
        exc.printStackTrace()
    }

    var commitLoopThread: Thread? = null
    try {
        commitLoopThread = Thread{ commitLoop() }
        commitLoopThread.start()
        println("Started loop thread successfully.")
    } catch (exc: Exception) {
        println("Failed to create a thread")
        LoggerUtil.printAndSend(exc.stackTraceToString())
        return
    }

    println("Write help to get all available commands")

    while (isRunning) {
        print("> ")
        var input = readln()
        if (input.isEmpty())
            continue

        val args = input.split(" ")
        when (args[0]) {
            "stop" -> {commitLoopThread.stop()
                isRunning = false
                exitProcess(0)
            }
            "branchlist" -> {
                val branches = RepoManager.getAllBranches()
                for (ref in branches) println(ref)
            }
            "restore" -> {
                val branches = RepoManager.getAllBranches()
                if (args.size != 2) {println("Syntax:\nrestore <branch name>"); continue}
                if (!branches.contains(args.getOrNull(1))) {println("Branch ${args.getOrNull(1)} not found"); continue}
                try {
                    val checkoutCommand = RepoManager.git!!.checkout().setName(args[1]).setForced(true)
                    checkoutCommand.call()
                    LoggerUtil.printAndSend("Restored successfully.")
                } catch (exc: Exception) {
                    LoggerUtil.printAndSend("Failed to restore information")
                    LoggerUtil.printAndSend(exc.stackTraceToString())
                }
            }
            "snapshot" -> commitAndPushAll()
            "timeleft" -> {
                if (lastCommitTime != null) {
                    val timeNow = LocalTime.now()
                    val sleepTime = ConfigLoader.config!!["sleep"] as Int
                    val diff = sleepTime - ((timeNow.toSecondOfDay() - lastCommitTime!!.toSecondOfDay()) / 60)
                    println("Time until next commit: $diff")
                }
            }
            "help" -> println(helpmsg)
            else -> {println("Command ${args[0]} not found.")}
        }
    }
}