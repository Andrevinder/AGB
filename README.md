# AGB
**A**<sub>utomatic</sub> **G**<sub>ithub</sub> **B**<sub>ackup</sub>

AGB is a tool that automatically commits and pushes repo in different branches to make sure you can restore those files at any point in time.
AGB also was designed with safety in mind that's why it uses telegram bot to automatically send log messages.

> FUN FACT: This project was created to make automatic minecraft world backups

# Usage
1. [Download](https://github.com/Andrevinder/AGB/releases) .jar file from releases or [build](#build-intellij-idea) it yourself.
2. Put the .jar file in your working directory and run
```
java -jar agb.jar
```
3. Program is going to create an `agb_config.yaml` file in your working directory.
4. Modify `agb_config.yaml` with your own tokens, sleep time, etc.
5. Run `java -jar agb.jar` again
6. Use `help` to get all available commands
# Build (Intellij Idea)
1. Clone this repo and open it using Intellij Idea
2. After synchronizing gradle file go to
`Gradle` -> `Tasks` -> `ktor` -> `buildFatJar`
3. When the task is complete go to the folder in `build/libs` and you will see `agb.jar` file