package com.eadive.ssh

import com.jcraft.jsch._

/**
  * Created by carlossouza on 5/20/15.
  */
class SSHManualConnection(user: String, host: String, privateKey: String) {

   val jsch = new JSch()
   jsch.addIdentity(privateKey)

   def openSession: Session = {
     val session = jsch.getSession(user, host, 22)
     session.setConfig("StrictHostKeyChecking", "no")
     session.connect()
     session
   }

   def closeSession(implicit session: Session): Unit = {
     session.disconnect()
   }

 }
