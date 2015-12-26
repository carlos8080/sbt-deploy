package com.eadive.ssh

import java.io._
import com.jcraft.jsch._
import collection.JavaConverters._

/**
 * SSH singleton
 * 
 * Used to create the SSHConnection. Also implements the main helper functions. 
 * 
 * Created by carlossouza on 2/19/15.
 */
object SSH {

  /**
   * Connect the user to a remote machine using SSH
   *
   * @param user the user
   * @param host the host
   * @param privateKey the private key
   * @return a connection
   */
  def connect(user: String, host: String, privateKey: String): SSHConnection = {
    new SSHConnection(user, host, privateKey)
  }

  def connectManual(user: String, host: String, privateKey: String): SSHManualConnection = {
    new SSHManualConnection(user, host, privateKey)
  }

  /**
   * Copy a file from a remote server to local
   *  
   * @param source the source file
   * @param destination the destination file
   * @param session the implicit session. Use {{{ connection.withSession { implicit session => ... } }}}
   */
  def copyFromRemote(source: String, destination: String)(implicit session: Session): Unit = {
    val channel = session.openChannel("sftp").asInstanceOf[ChannelSftp]
    channel.connect()
    channel.get(source, destination)
    channel.disconnect()
  }

  /**
   * Copy a file from a remote server to local with callback function to monitor
   *
   * @param source the source file
   * @param destination the destination file
   * @param monitor the implementation of SftpProgressMonitor Java interface
   * @param session the implicit session. Use {{{ connection.withSession { implicit session => ... } }}}
   */
  def copyFromRemoteWithMonitor(source: String, destination: String, monitor: SftpProgressMonitor)(implicit session: Session): Unit = {
    val channel = session.openChannel("sftp").asInstanceOf[ChannelSftp]
    channel.connect()
    channel.get(source, destination, monitor)
    channel.disconnect()
  }

  /**
   * Copy a file to a remote server from local
   *
   * @param source the source file
   * @param destination the destination file
   * @param session the implicit session. Use {{{ connection.withSession { implicit session => ... } }}}
   */
  def copyToRemote(source: String, destination: String)(implicit session: Session): Unit = {
    val channel = session.openChannel("sftp").asInstanceOf[ChannelSftp]
    channel.connect()
    channel.put(source, destination)
    channel.disconnect()
  }

  /**
   * Copy a file to a remote server from local with callback function to monitor
   *
   * @param source the source file
   * @param destination the destination file
   * @param monitor the implementation of SftpProgressMonitor Java interface
   * @param session the implicit session. Use {{{ connection.withSession { implicit session => ... } }}}
   */
  def copyToRemoteWithMonitor(source: String, destination: String, monitor: SftpProgressMonitor)(implicit session: Session): Unit = {
    val channel = session.openChannel("sftp").asInstanceOf[ChannelSftp]
    channel.connect()
    channel.put(source, destination, monitor)
    channel.disconnect()
  }

  /**
   * Executes a command in the remote server and prints the result on screen \
   *  
   * @param command the command
   * @param session the implicit session. Use {{{ connection.withSession { implicit session => ... } }}}
   */
  def executeCommand(command: String, outputToFile: String = "")(implicit session: Session): Unit = {
    val channel: ChannelExec = session.openChannel("exec").asInstanceOf[ChannelExec]
    channel.setCommand(command)
    channel.setInputStream(null)
    val stdout: InputStream = channel.getInputStream
    val stderr: InputStream = channel.getErrStream
    channel.connect()
    outputToFile match {
      case "" => inputToScreen(stdout)
      case _ => inputToFile(stdout, new File(outputToFile))
    }
    channel.disconnect()
  }

  /**
   * Checks if a file exists in a remote directory
   *
   * @param file the file to be checked
   * @param session the implicit session. Use {{{ connection.withSession { implicit session => ... } }}}
   * @return true if the file exists, false if not
   */
  def fileExists(file: String)(implicit session: Session): Boolean = {
    val channel = session.openChannel("sftp").asInstanceOf[ChannelSftp]
    channel.connect()
    try {
      val list = channel.ls(file).asScala
      if (list.size > 0) true else false
    } catch {
      case e: Exception => false
    } finally {
      channel.disconnect()
    }
  }

  /**
   * Reads a remote directory
   *  
   * @param directory the directory to read
   * @param session session the implicit session. Use {{{ connection.withSession { implicit session => ... } }}}
   * @return a list of filenames in the directory                
   */
  def listFiles(directory: String)(implicit session: Session): List[String] = {
    val channel: ChannelExec = session.openChannel("exec").asInstanceOf[ChannelExec]
    channel.setCommand("ls " + directory)
    channel.setInputStream(null)
    val stdout: InputStream = channel.getInputStream
    val stderr: InputStream = channel.getErrStream
    channel.connect()
    val in = scala.io.Source.fromInputStream(stdout)
    val result = in.getLines().toList //.foreach()
    channel.disconnect()
    result
  }

  /**
   * Remove a file from a remote server
   *
   * @param file the file to be deleted
   * @param session the implicit session. Use {{{ connection.withSession { implicit session => ... } }}}
   */
  def removeFromRemote(file: String)(implicit session: Session): Unit = {
    val channel = session.openChannel("sftp").asInstanceOf[ChannelSftp]
    channel.connect()
    channel.rm(file)
    channel.disconnect()
  }

  /**
   * Prints a java.io.InputStream on screen
   *  
   * @param is the InputStream
   */
  def inputToScreen(is: java.io.InputStream) {
    val in = scala.io.Source.fromInputStream(is)
    in.getLines().foreach( println(_))
  }

  /**
   * Prints a java.io.InputStream into a file
   *  
   * @param is the InputStream
   * @param f the file
   */
  def inputToFile(is: java.io.InputStream, f: java.io.File) {
    val in = scala.io.Source.fromInputStream(is)
    val out = new java.io.PrintWriter(f)
    try { in.getLines().foreach(out.print(_)) }
    finally { out.close }
  }

  /**
   * Formats byte count in readable human string
   *
   * @param bytes the byte count
   * @param si if true SI, if not Binary
   * @return
   */
  def readableByteCount(bytes: Long, si: Boolean = true): String = {
    val unit = if (si) 1000 else 1024
    if (bytes < unit) return bytes + " B"
    val exp: Int = (Math.log(bytes) / Math.log(unit)).toInt
    val pre: String = {if (si) "kMGTPE" else "KMGTPE"}.charAt(exp-1) + {if (si) "" else "i"}
    "%.1f %sB".format(bytes / Math.pow(unit, exp), pre)
  }

}
