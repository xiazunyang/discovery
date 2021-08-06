package cn.numeron

import org.gradle.internal.impldep.org.apache.commons.codec.digest.DigestUtils
import java.io.File

fun main() {
//    val jarFile = File("C:\\Users\\Administrator\\.m2\\repository\\cn\\numeron\\discovery.library\\1.0.1\\discovery.library-1.0.1.jar")
    val jarFile = File("E:\\AndroidWork\\discovery\\library\\build\\libs\\library.jar")
    val md5Hex = DigestUtils.md5Hex(jarFile.readBytes())
    println(md5Hex)
}