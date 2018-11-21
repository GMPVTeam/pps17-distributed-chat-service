package it.unibo.dcs.demo

import java.awt.Desktop
import java.net.URI

object ServiceDeploymentHelper {

  def openBrowser(url: String): Unit = Desktop.getDesktop.browse(new URI(url))

  def buildDetachedProcessCommand(command: String): String = {
    if (System.getProperty("os.name").contains("Windows")) {
      "cmd.exe /c START " + command
    } else {
      "shopt -u huponexit; " + command
    }
  }
}
