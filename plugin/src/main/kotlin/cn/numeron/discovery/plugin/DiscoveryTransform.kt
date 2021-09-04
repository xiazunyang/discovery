package cn.numeron.discovery.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import java.io.File
import java.io.InputStream
import java.util.jar.JarFile

class DiscoveryTransform(project: Project) : AbstractTransform(project) {

    private val discoverableSet = mutableSetOf<String>()
    private val implementationSet = mutableMapOf<String, List<String>>()

    /** Discovery库的jar文件路径 */
    private var discoveryLibraryJarFilePath: String? = null

    override fun getName(): String = "Discovery"

    override fun isIncremental(): Boolean = false

    override fun processDirectory(dirInput: DirectoryInput, outputDirFile: File) {
        outputDirFile.walkTopDown()
            .filter {
                it.isFile && isClassFile(it.name)
            }
            .forEach {
                scanClasses(it.readBytes())
            }
    }

    private fun isClassFile(fileName: String): Boolean {
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
                !it.isDirectory && isClassFile(name)
            }
            .forEach {
                val classBytes = jarFile.getInputStream(it).use(InputStream::readBytes)
                scanClasses(classBytes)
            }
    }

    /** 解析class文件，记录需要处理的class文件的路径 */
    private fun scanClasses(classBytes: ByteArray) {
        val classReader = ClassReader(classBytes)
        val annotationVisitor = AnnotationVisitor()
        classReader.accept(annotationVisitor, 0)
        val className = classReader.className.toClassName()
        if (annotationVisitor.hasAnnotation(DISCOVERABLE_ANNOTATION)) {
            if (annotationVisitor.isInterface()) {
                discoverableSet.add(className)
            }
        }
        if (annotationVisitor.hasAnnotation(IMPLEMENTATION_ANNOTATION)) {
            if (annotationVisitor.hasNoArgConstructor()) {
                implementationSet[className] = annotationVisitor.interfaces
            }
        }
    }

    override fun onTransformed() {
        wLog("discoverableSet = $discoverableSet")
        wLog("implementation = $implementationSet")
        //整理为需要的格式
        /*val implementation = implementationSet
            .groupBy(Implementation::discoverableName)
            .toMap()
            .filter {
                //忽略没有注册过的接口
                it.key in discoverableSet
            }
            .mapValues {
                it.value.map(Implementation::qualifierName)
            }
        //获取Discoveries.class所在的jar包
        val jarFile = File(discoveryLibraryJarFilePath ?: throw NullPointerException("Not found Discovery Library."))
        //创建一个字节数组输出流，保存修改后的class数据
        val tempOutputStream = ByteArrayOutputStream()
        jarFile.copyTo(tempOutputStream, NamedConverter(DISCOVERIES_CLASS) {
            val classReader = ClassReader(it)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val visitor = DiscoveryClassVisitor(classWriter, implementation)
            classReader.accept(visitor, 0)
            //获取修改后的class，并将数据转为输入流
            classWriter.toByteArray().inputStream()
        })
        //将新文件的数据覆盖到原文件
        jarFile.writeBytes(tempOutputStream.toByteArray())*/
    }

}