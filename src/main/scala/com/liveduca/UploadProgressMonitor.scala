package com.liveduca

import java.text.NumberFormat
import com.jcraft.jsch._
import com.liveduca.ssh.SSH
import org.joda.time._

/**
 * Created by carlossouza on 5/20/15.
 */
class UploadProgressMonitor extends SftpProgressMonitor  {

  var current: Long = 0
  var total: Long = 0
  var temp: Long = 0
  var lastPercentage: Int = 0
  val defaultFormat = NumberFormat.getPercentInstance()
  defaultFormat.setMinimumFractionDigits(1)
  var destinationPackage: String = ""
  var sizeReadable: String = ""
  var prevTime: Int = DateTime.now().getMillisOfDay
  var prevByte: Long = 0
  var prevRate: String = "-"
  var newRate: Float = 0

  def init(op: Int, src: String, dest: String, max: Long): Unit = {
    destinationPackage = dest.split("/").last
    sizeReadable = SSH.readableByteCount(max)
    total = max
  }

  def count(bytes: Long): Boolean = {
    var i: Long = 0
    //current += bytes
    while (i < bytes) {
      i += 1
      if (current < total) current += 1
      if (System.getProperty("user.name") == "jenkins") {
        val currPercentage = math.ceil((current.toFloat / total.toFloat) * 100).toInt
        if (currPercentage != lastPercentage) {
          print("\rUploading " + destinationPackage + " (" + sizeReadable + ") ->     Progress: " + defaultFormat.format(current.toFloat / total.toFloat) + "      ETA: " + getETA + "     Rate: " + getUploadRate)
          lastPercentage = currPercentage
        }
      } else {
        print("\rUploading " + destinationPackage + " (" + sizeReadable + ") ->     Progress: " + defaultFormat.format(current.toFloat / total.toFloat) + "      ETA: " + getETA + "     Rate: " + getUploadRate)
      }
    }
    true
  }

  def end(): Unit = {
    println("DONE")
  }

  def getUploadRate: String = {
    val currPercentage = math.round((current.toFloat / total.toFloat) * 200) //.toInt
    if (currPercentage != lastPercentage) {
      // Update rate
      val currTime: Int = DateTime.now().getMillisOfDay
      val currByte: Long = current
      newRate = (currByte - prevByte).toFloat / (currTime - prevTime).toFloat * 1000

      lastPercentage = currPercentage
      prevTime = currTime
      prevByte = currByte
      prevRate = SSH.readableByteCount(newRate.toLong) + "/s"
      prevRate
    } else {
      // Show the same
      prevRate
    }
  }

  def getETA: String = {
    if (newRate != 0) {
      val eta = ((total.toFloat - current.toFloat) / newRate * 1000).toLong

      val second: Long = (eta / 1000) % 60
      val minute: Long = (eta / (1000 * 60)) % 60
      val hour: Long = (eta / (1000 * 60 * 60)) % 24
      "%02d:%02d:%02d".format(hour, minute, second)
    } else {
      "---"
    }
  }

}

