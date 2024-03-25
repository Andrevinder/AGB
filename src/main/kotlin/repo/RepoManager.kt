package repo

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.RefNotFoundException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object RepoManager {
    var git: Git? = null

    fun initRepo(repoFolder: File) {
        try {
            git = Git.open(repoFolder)
        } catch (exc: RepositoryNotFoundException) {
            val initCommand = Git.init()
            initCommand.setDirectory(repoFolder)
            git = initCommand.call()
        }

    }

    fun commitAll() {

        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")

        var renameBranch = false

        val branchName = formatter.format(currentTime)
        try {
            val createBranchCommand = git!!.branchCreate().setName(branchName)
            createBranchCommand.call()

            val checkoutCommand = git!!.checkout().setName(branchName)
            checkoutCommand.call()
        } catch (exc: RefNotFoundException){
            renameBranch = true
        }

        val addCommand = git!!.add()
        addCommand.addFilepattern(".").call()

        val commitMessage = "Commit for $branchName"
        val commitCommand = git!!.commit()
        commitCommand.setMessage(commitMessage).call()

        if (renameBranch) {
            val renameCommand = git!!.branchRename()
            renameCommand.setNewName(branchName).call()
        }

    }

    fun pushWithCredentials(remoteURL: String, login: String, password: String) {
        val pushCommand = git!!.push()
        pushCommand.setRemote(remoteURL)
            .setCredentialsProvider(
                UsernamePasswordCredentialsProvider(login, password)
            )
            .call()
    }

    fun pushWithGithubToken(remoteURL: String, token: String){
        val pushCommand = git!!.push()
        pushCommand.setRemote(remoteURL)
            .setCredentialsProvider(
                UsernamePasswordCredentialsProvider(token, "")
            )
            .call()
    }

    fun pushWithoutAuth(remoteURL: String) {
        val pushCommand = git!!.push()
        pushCommand.setRemote(remoteURL)
            .call()
    }

    fun getAllBranches(): ArrayList<String> {
        val refs = git!!.branchList().call()
        var strings = arrayListOf<String>()
        for (ref in refs) strings.add(ref.name)
        return strings
    }

    fun checkout(branchName: String) {
        git!!.checkout().setName(branchName).call()
    }

}