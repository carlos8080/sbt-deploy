SBT-Deploy - 1.2.4
==================

SBT plugin to deploy Play apps in AWS instances (or any other).

### Installation

First import the library to your project, in ```plugins.sbt```:

```scala
addSbtPlugin("com.eadive" % "sbt-deploy" % "1.2.4")
```

Then, in your ```build.sbt``` file:

```scala
enablePlugins(SbtDeploy)

DeploySettings.server := "staging.eadive.com"

DeploySettings.keyFile := "/Users/carlossouza/.ssh/staging.pem"
```

### Usage

Then, to deploy your app to the server, just run:

```scala
sbt clean dist deploy
```

### Customization

Here are the settings and its default values. They should be customized on ```build.sbt```:

```scala
DeploySettings.packageFilename     := name.value + "-" + version.value + ".zip"
DeploySettings.sourcePackageFile   := new File("").getAbsolutePath() + "/target/universal/" + name.value + "-" + version.value + ".zip"
DeploySettings.destinationFolder   := "/home/ubuntu/apps/"
DeploySettings.user                := "ubuntu"
DeploySettings.server              := ""
DeploySettings.keyFile             := ""
DeploySettings.port                := "9000"
DeploySettings.nohupFile           := "nohup.out"
//DeploySettings.removeOlderVersions := true
DeploySettings.addToStartup        := true
```

### Dependencies

This library works with **"com.jcraft" % "jsch" % "0.1.52"**. Please refer to [library's website](http://www.jcraft.com/jsch/) for more details.

### Revision history

Version | Changes
--------|--------
1.2.4 | Changing package name and organization to Eadive
1.2.3 | Bugfix: adding log on screen, and fixing add as service
1.2.2 | Bugfix: removing the removeOlderVersions setting
1.2.1 | Bugfix: moving folder after unzip to right place, and removing older versions default to true
1.2.0 | Now, by default, sbt-deploy creates a service in linux/ubuntu, that starts on boot, and responds to {start|stop|restart|status}
1.1.7 | Bugfix: application restart was not working - enforcing sudo kill and removing RUNNING_PID in case of restart
1.1.6 | Bugfix: permission denied / copying startup script first to public folder, then moving to /etc/init.d/
1.1.5 | Adding addToStartup setting, to create startup script [linux/ubuntu](http://askubuntu.com/questions/228304/how-do-i-run-a-script-at-start-up)
1.1.0 | Changing destination folder to exclude app's version
1.0.0 | Changing name to SbtDeploy and adding DeploySettings, with key removeOlderVersions
0.2.0 | Development: preparing release
0.1.0 | Initial development