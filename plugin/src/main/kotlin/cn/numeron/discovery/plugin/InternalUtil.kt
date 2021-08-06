package cn.numeron.discovery.plugin

import org.gradle.api.internal.file.archive.ZipCopyAction
import org.gradle.util.TextUtil
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

fun String.toClassName() = replace('/', '.')

fun File.zipTo(zipFile: File = File(parentFile, "$name.jar")) {
    zipTo(zipFile, this, walkReproducibly())
}

fun File.unzipTo(outputDirectory: File = File(parent, nameWithoutExtension)): File {
    ZipFile(this).use { zip ->
        val outputDirectoryCanonicalPath = outputDirectory.canonicalPath
        for (entry in zip.entries()) {
            unzipEntryTo(outputDirectory, outputDirectoryCanonicalPath, zip, entry)
        }
    }
    return outputDirectory
}

fun File.walkReproducibly(): Sequence<File> = sequence {

    require(isDirectory)

    yield(this@walkReproducibly)

    var directories: List<File> = listOf(this@walkReproducibly)
    while (directories.isNotEmpty()) {
        val subDirectories = mutableListOf<File>()
        directories.forEach { dir ->
            dir.listFilesOrdered().partition { it.isDirectory }.let { (childDirectories, childFiles) ->
                yieldAll(childFiles)
                childDirectories.let {
                    yieldAll(it)
                    subDirectories.addAll(it)
                }
            }
        }
        directories = subDirectories
    }
}


private fun zipTo(zipFile: File, baseDir: File, files: Sequence<File>) {
    zipTo(zipFile, fileEntriesRelativeTo(baseDir, files))
}

private fun fileEntriesRelativeTo(baseDir: File, files: Sequence<File>): Sequence<Pair<String, ByteArray>> =
    files.filter { it.isFile }.map { file ->
        val path = file.normalisedPathRelativeTo(baseDir)
        val bytes = file.readBytes()
        path to bytes
    }

private fun File.normalisedPathRelativeTo(baseDir: File) =
    TextUtil.normaliseFileSeparators(relativeTo(baseDir).path)


private fun zipTo(zipFile: File, entries: Sequence<Pair<String, ByteArray>>) {
    zipTo(zipFile.outputStream(), entries)
}

private fun zipTo(outputStream: OutputStream, entries: Sequence<Pair<String, ByteArray>>) {
    ZipOutputStream(outputStream).use { zos ->
        entries.forEach { entry ->
            val (path, bytes) = entry
            zos.putNextEntry(
                ZipEntry(path).apply {
                    time = ZipCopyAction.CONSTANT_TIME_FOR_ZIP_ENTRIES
                    size = bytes.size.toLong()
                }
            )
            zos.write(bytes)
            zos.closeEntry()
        }
    }
}

private fun unzipEntryTo(
    outputDirectory: File,
    outputDirectoryCanonicalPath: String,
    zip: ZipFile,
    entry: ZipEntry
) {
    val output = outputDirectory.resolve(entry.name)
    if (!output.canonicalPath.startsWith(outputDirectoryCanonicalPath)) {
        throw ZipException("Zip entry '${entry.name}' is outside of the output directory")
    }
    if (entry.isDirectory) {
        output.mkdirs()
    } else {
        output.parentFile.mkdirs()
        zip.getInputStream(entry).use { it.copyTo(output) }
    }
}

private fun InputStream.copyTo(file: File): Long = file.outputStream().use { copyTo(it) }


private fun File.listFilesOrdered(filter: ((File) -> Boolean)? = null): List<File> =
    listFiles()
        ?.let { if (filter != null) it.filter(filter) else it.asList() }
        ?.sortedBy { it.name }
        ?: emptyList()