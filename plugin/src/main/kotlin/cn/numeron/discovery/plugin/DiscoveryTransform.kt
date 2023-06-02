package cn.numeron.discovery.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.jar.JarFile

class DiscoveryTransform(project: Project) : AbstractTransform(project) {

    private val discoverableSet = mutableSetOf<String>()
    private val implementationSet = mutableSetOf<Implementation>()

    /** Discovery库的jar文件路径 */
    private var discoveryLibraryJarFilePath: String? = null

    override fun getName(): String = "Discovery"

    override fun isIncremental(): Boolean = false

    override fun processDirectory(dirInput: DirectoryInput, outputDirFile: File) {
        outputDirFile.walkTopDown()
            .filter {
                it.isFile && isNeededClassFile(it.name)
            }
            .forEach {
                scanClasses(it.readBytes())
            }
    }

    private fun isNeededClassFile(fileName: String): Boolean {
        return fileName.endsWith(".class")
                && fileName != "R.class"
                && !fileName.startsWith("BuildConfig")
                && !fileName.startsWith("R$")
    }

    override fun processJar(jarInput: JarInput, outputJarFile: File) {
        //通过jar包中有没有指定的class文件，确定是不是要找的jar包
        val jarFile = JarFile(outputJarFile)
        if (discoveryLibraryJarFilePath == null) {
            if (jarFile.hasEntry(DISCOVERIES_CLASS)) {
                wLog("discovery library jar file path: $outputJarFile.")
                discoveryLibraryJarFilePath = outputJarFile.absolutePath
            }
        }
        jarFile.entries()
            .asSequence()
            .filter {
                val name = it.name.substringAfterLast('/')
                !it.isDirectory && isNeededClassFile(name)
            }
            .forEach {
                val classBytes = jarFile.getInputStream(it).use(InputStream::readBytes)
                scanClasses(classBytes)
            }
        jarFile.close()
    }

    /** 解析class文件，记录需要处理的class文件的路径 */
    private fun scanClasses(classBytes: ByteArray) {
        val classReader = ClassReader(classBytes)
        val discoveryVisitor = DiscoveryVisitor()
        classReader.accept(discoveryVisitor, 0)
        val className = classReader.className.toClassName()
        if (discoveryVisitor.hasAnnotation(DISCOVERABLE_ANNOTATION)) {
            if (discoveryVisitor.isAbstract()) {
                discoverableSet.add(className)
            }
        }
        if (discoveryVisitor.hasAnnotation(IMPLEMENTATION_ANNOTATION)) {
            val interfaces = discoveryVisitor.interfaces.map(String::toClassName)
            val superTypes = interfaces + discoveryVisitor.superName.toClassName()
            implementationSet.add(
                Implementation(
                    className,
                    superTypes,
                    discoveryVisitor.order
                )
            )
        }
    }

    override fun onTransformed() {
        //整理为需要的格式
        val discoveries = implementationSet
            .map { implementation ->
                implementation.superTypes
                    .filter(discoverableSet::contains)
                    .map {
                        it to implementation
                    }
            }
            .flatten()
            .groupBy(Pair<String, Implementation>::first)
            .mapValues {
                it.value.map(Pair<String, Implementation>::second).sorted().map(Implementation::classpath)
            }
        //获取Discoveries.class所在的jar包
        val jarFile = File(discoveryLibraryJarFilePath ?: throw NullPointerException("Not found Discovery Library."))
        //创建一个字节数组输出流，保存修改后的class数据
        val tempOutputStream = ByteArrayOutputStream()
        jarFile.copyTo(tempOutputStream, NamedConverter(DISCOVERIES_CLASS) {
            val classReader = ClassReader(it)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val visitor = DiscoveriesClassModifyVisitor(classWriter, discoveries)
            classReader.accept(visitor, 0)
            //获取修改后的class
            classWriter.toByteArray()
        })
        //将新文件的数据覆盖到原文件
        jarFile.writeBytes(tempOutputStream.toByteArray())
    }

}