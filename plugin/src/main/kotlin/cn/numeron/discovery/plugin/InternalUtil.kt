package cn.numeron.discovery.plugin

import java.io.File
import java.io.OutputStream
import java.util.function.Function
import java.util.zip.*

fun String.toClassName() = replace('/', '.')

class NamedConverter(
    val name: String,
    val converter: Function<ByteArray, ByteArray>? = null
)

fun File.copyTo(outputStream: OutputStream, vararg ignores: String) {
    return copyTo(outputStream, *ignores.map { NamedConverter(it) }.toTypedArray())
}

fun File.copyTo(outputStream: OutputStream, vararg targets: NamedConverter) {
    if (!isFile) throw IllegalArgumentException("input source must be file.")
    val zipInputStream = ZipInputStream(inputStream().buffered())
    val zipOutputStream = ZipOutputStream(outputStream.buffered())
    var zipEntry: ZipEntry? = zipInputStream.nextEntry
    while (zipEntry != null) {
        val name = zipEntry.name
        //判断class文件是否是需要处理的文件
        val namedConverter = targets.find {
            it.name == name
        }
        //获取转换器
        val converter = namedConverter?.converter
        if (zipEntry.isDirectory) {
            //如果是目录，则直接添加Entry，并结束
            zipOutputStream.putNextEntry(zipEntry)
            zipOutputStream.closeEntry()
        } else if (namedConverter == null) {
            //如果不是指定要处理的文件，则复制
            zipOutputStream.putNextEntry(zipEntry)
            zipInputStream.copyTo(zipOutputStream)
            zipOutputStream.closeEntry()
        } else if (converter != null) {
            //如果指定了转换器，则转换输入流，并写入新的数据
            zipOutputStream.putNextEntry(ZipEntry(name))
            converter.apply(zipInputStream.readBytes()).let(zipOutputStream::write)
            zipOutputStream.closeEntry()
        } else {
            //其它情况则忽略掉该条目
        }
        zipOutputStream.flush()
        //继续下一个
        zipEntry = zipInputStream.nextEntry
    }
    zipInputStream.close()
    zipOutputStream.close()
}

fun File.zipTo(outputStream: OutputStream) {
    if (!isDirectory) throw IllegalArgumentException("input source must be directory.")
    val directoryPath = absolutePath
    ZipOutputStream(outputStream.buffered()).use { zipOutputStream ->
        walkTopDown()
            .onEnter {
                it.absolutePath.startsWith(directoryPath)
            }
            .filter {
                it.absolutePath != directoryPath
            }
            .forEach { file ->
                var entryName = file.path.removePrefix(directoryPath + "\\").replace('\\', '/')
                if (file.isDirectory) {
                    entryName += "/"
                }
                val zipEntry = ZipEntry(entryName)
                zipEntry.time = file.lastModified()
                zipOutputStream.putNextEntry(zipEntry)
                if (!file.isDirectory) {
                    file.inputStream().buffered().use { fileInputStream ->
                        fileInputStream.copyTo(zipOutputStream)
                    }
                }
                zipOutputStream.closeEntry()
                zipOutputStream.flush()
            }
    }
}

fun File.unzipTo(outputDir: File = File(parentFile, nameWithoutExtension)) {
    if (!isFile) throw IllegalArgumentException("input source must be file.")
    val zipInputStream = ZipInputStream(inputStream().buffered())
    var zipEntry: ZipEntry? = zipInputStream.nextEntry
    while (zipEntry != null) {
        val name = zipEntry.name
        val entryFile = File(outputDir, name)
        if (zipEntry.isDirectory) {
            entryFile.mkdirs()
        } else {
            entryFile.outputStream().buffered().use {
                zipInputStream.copyTo(it)
            }
        }
        zipInputStream.closeEntry()
        zipEntry = zipInputStream.nextEntry
    }
    zipInputStream.close()
}

const val DISCOVERIES_CLASS = "cn/numeron/discovery/Discoveries.class"

const val DISCOVERABLE_ANNOTATION = "Lcn/numeron/discovery/Discoverable;"

const val IMPLEMENTATION_ANNOTATION = "Lcn/numeron/discovery/Implementation;"


fun ZipFile.hasEntry(className: String): Boolean {
    val zipEntry = getEntry(className)
    return zipEntry != null
}