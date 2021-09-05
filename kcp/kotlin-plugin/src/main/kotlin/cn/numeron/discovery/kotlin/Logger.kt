package cn.numeron.discovery.kotlin

import java.io.File
import javax.swing.filechooser.FileSystemView

object Logger {

    private val logFile: File

    init {
        val logFileDir = FileSystemView.getFileSystemView().homeDirectory
        logFile = File(logFileDir, "discovery.log")
    }

    fun log(message: String) {
        logFile.appendText(message + "\n")
    }

}