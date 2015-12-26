package com.eadive

import sbt.Keys._
import sbt._

import com.eadive.ssh.SSH
import com.eadive.ssh.SSH._

import java.io._
import scala.io.Source


/**
 * Created by carlossouza on 5/20/15.
 */
object SbtDeploy extends AutoPlugin {

  /**
   * Defines all settings/tasks that get automatically imported,
   * when the plugin is enabled
   */
  object autoImport {
    lazy val deploy = TaskKey[Unit]("deploy", "Deploy the package generated with 'sbt dist' to external server")

    object DeploySettings {
      lazy val packageFilename    = SettingKey[String]("deploymentPackageFilename")
      lazy val sourcePackageFile  = SettingKey[String]("deploymentSourcePackageFile")
      lazy val destinationFolder  = SettingKey[String]("deploymentDestinationFolder")
      lazy val user               = SettingKey[String]("deploymentUser")
      lazy val server             = SettingKey[String]("deploymentServer")
      lazy val keyFile            = SettingKey[String]("deploymentKeyFile")
      lazy val port               = SettingKey[String]("deploymentPort")
      lazy val nohupFile          = SettingKey[String]("deploymentNohupFile")
      //lazy val removeOlderVersions = SettingKey[Boolean]("removeOlderVersions")
      lazy val addToStartup       = SettingKey[Boolean]("addToStartup")
    }
  }

  import autoImport._

  /**
   * Provide default settings
   */
  override lazy val projectSettings = Seq(
    DeploySettings.packageFilename     := name.value + "-" + version.value + ".zip",
    DeploySettings.sourcePackageFile   := new File("").getAbsolutePath() + "/target/universal/" + name.value + "-" + version.value + ".zip",
    DeploySettings.destinationFolder   := "/home/ubuntu/apps/",
    DeploySettings.user                := "ubuntu",
    DeploySettings.server              := "",
    DeploySettings.keyFile             := "",
    DeploySettings.port                := "9000",
    DeploySettings.nohupFile           := "nohup.out",
    //DeploySettings.removeOlderVersions := true,
    DeploySettings.addToStartup        := true,

    deploy := {

      val conn = SSH.connect(DeploySettings.user.value, DeploySettings.server.value, DeploySettings.keyFile.value)
      conn.withSession { implicit session =>
        println(scala.Console.GREEN + "Deploying " + DeploySettings.packageFilename.value + " -> " + DeploySettings.user.value + "@" + DeploySettings.server.value + "\n")
        println(scala.Console.BLUE + "Creating directory " + DeploySettings.destinationFolder.value + "...")
        executeCommand("mkdir -p " + DeploySettings.destinationFolder.value)
        println(scala.Console.BLUE + "mkdir -p " + DeploySettings.destinationFolder.value)
        println(scala.Console.GREEN + "DONE\n")

        val destinationPackage = DeploySettings.destinationFolder.value + DeploySettings.packageFilename.value
        println(scala.Console.BLUE + "Uploading " + destinationPackage + "..." + scala.Console.CYAN)
        copyToRemoteWithMonitor(DeploySettings.sourcePackageFile.value, destinationPackage, new UploadProgressMonitor())

        //var appFolder = DeploySettings.destinationFolder.value + name.value + "-" + version.value + "/"
        val appFolder = DeploySettings.destinationFolder.value + name.value + "/"
        val PIDFile = appFolder + "RUNNING_PID"
        var lastMsg = "Starting "
        if (fileExists(PIDFile)) {
          println(scala.Console.BLUE + "Server is running, shutting server down...")
          copyFromRemote(PIDFile, "RUNNING_PID_TEMP")
          val source = scala.io.Source.fromFile("RUNNING_PID_TEMP")
          val PID = try source.mkString finally source.close()
          val file = new File("RUNNING_PID_TEMP")
          file.delete()
          executeCommand("sudo kill -9 " + PID)
          println(scala.Console.BLUE + "sudo kill -9 " + PID)
          println(scala.Console.GREEN + "DONE\n")

          println(scala.Console.BLUE + "Removing old application...")
          executeCommand("sudo rm -rf " + appFolder)
          println(scala.Console.BLUE + "sudo rm -rf " + appFolder)
          println(scala.Console.GREEN + "DONE\n")
          lastMsg = "Restarting "
        }

        /*if (DeploySettings.removeOlderVersions.value == true) {
          println(scala.Console.BLUE + "Removing deprecated versions...")
          executeCommand("sudo rm -rf " + DeploySettings.destinationFolder.value + name.value + "*")
          println(scala.Console.BLUE + "sudo rm -rf " + DeploySettings.destinationFolder.value + name.value + "*")
          println(scala.Console.GREEN + "DONE")
        }*/

        println(scala.Console.BLUE + "Unzipping [" + DeploySettings.destinationFolder.value + "]..." + scala.Console.CYAN)
        executeCommand("unzip -o " + destinationPackage + " -d " + DeploySettings.destinationFolder.value) // + " -qq")
        removeFromRemote(destinationPackage)
        executeCommand("mv " + DeploySettings.destinationFolder.value + name.value + "-" + version.value + " " + DeploySettings.destinationFolder.value + name.value)
        println(scala.Console.BLUE + "mv " + DeploySettings.destinationFolder.value + name.value + "-" + version.value + " " + DeploySettings.destinationFolder.value + name.value)
        println(scala.Console.GREEN + "DONE")

        println(scala.Console.BLUE + "\n" + lastMsg + "server at " + DeploySettings.server.value + "...")
        val appFile = DeploySettings.destinationFolder.value + name.value + "/bin/" + name.value  //appFolder + "bin/" + name.value
        println(scala.Console.BLUE + "sudo nohup " + appFile + " -Dhttp.port=" + DeploySettings.port.value + " > " + appFolder + DeploySettings.nohupFile.value + " 2>&1 &")
        executeCommand("sudo nohup " + appFile + " -Dhttp.port=" + DeploySettings.port.value + " > " + appFolder + DeploySettings.nohupFile.value + " 2>&1 &")
        println(scala.Console.GREEN + "DONE")

        if (DeploySettings.addToStartup.value == true) {
          println(scala.Console.BLUE + "\nAdding app to startup scripts @" + DeploySettings.server.value)
          val startupScriptFilePath = new File("").getAbsolutePath() + "/" + name.value
          createInitScript(
            name.value,
            PIDFile,
            "sudo nohup " + appFile + " -Dhttp.port=" + DeploySettings.port.value + " > " + appFolder + DeploySettings.nohupFile.value + " 2>&1 &",
            startupScriptFilePath
          )
          copyToRemote(startupScriptFilePath, "/home/" + DeploySettings.user.value + "/" + name.value)

          executeCommand("sudo mv /home/" + DeploySettings.user.value + "/" + name.value +  " /etc/init.d/" + name.value)
          println(scala.Console.BLUE + "sudo mv /home/" + DeploySettings.user.value + "/" + name.value +  " /etc/init.d/" + name.value)

          executeCommand("sudo chmod ugo+x /etc/init.d/" + name.value)
          println(scala.Console.BLUE + "sudo chmod ugo+x /etc/init.d/" + name.value)

          executeCommand("sudo update-rc.d " + name.value + " defaults")
          println(scala.Console.BLUE + "sudo update-rc.d " + name.value + " defaults")

          println(scala.Console.GREEN + "DONE" + scala.Console.RESET)
        }

      }
    }
  )

  /**
   * Creates init script, with start|stop|restart|status commands, enabling app to be used as service in linux/ubuntu
   *
   * @param serviceName
   * @param pidFile
   * @param startCommand
   * @param startupScriptFilePath
   */
  def createInitScript(serviceName: String, pidFile: String, startCommand: String, startupScriptFilePath: String): Unit = {
    val stream : InputStream = getClass.getResourceAsStream("/initscript_template.sh")
    val lines = scala.io.Source.fromInputStream( stream ).getLines()
    val pw = new PrintWriter(new File(startupScriptFilePath))

    lines.foreach { line =>
      var newLine = line
      if (line.contains("{serviceName}"))   newLine = newLine.replace("{serviceName}", serviceName)
      if (line.contains("{pidFile}"))       newLine = newLine.replace("{pidFile}", pidFile)
      if (line.contains("{startCommand}"))  newLine = newLine.replace("{startCommand}", startCommand)
      pw.write(newLine + "\n")
    }
    pw.close()
  }

}
