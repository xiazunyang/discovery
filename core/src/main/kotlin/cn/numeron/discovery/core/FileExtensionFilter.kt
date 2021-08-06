package cn.numeron.discovery.core

import java.io.File
import java.io.FilenameFilter

class FileExtensionFilter(private val ext: String) : FilenameFilter {
    override fun accept(dir: File, filename: String): Boolean {
        return filename.endsWith(ext)
    }
}