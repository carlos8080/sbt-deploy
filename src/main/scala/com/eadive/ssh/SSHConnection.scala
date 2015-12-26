package com.eadive.ssh

import com.jcraft.jsch._

/**
  * SSHConnection class
  *
  * Simply used to hold user, host and privateKey parameters, and initialize JSCH.
  * Created by carlossouza on 2/19/15.
  */
class SSHConnection(user: String, host: String, privateKey: String) {

  val jsch = new JSch()
  jsch.addIdentity(privateKey)

  def withSession[T](f: Session => T): T = {
    val session = jsch.getSession(user, host, 22)
    session.setConfig("StrictHostKeyChecking", "no")
    session.connect()
    var ok = false
    try {
      val res = f(session)
      ok = true
      res
    } finally {
      if(ok) session.disconnect() // Let exceptions propagate normally
      else {
        // f(s) threw an exception, so don't replace it with an Exception from close()
        try session.disconnect() catch { case _: Throwable => }
      }
    }
  }

}
