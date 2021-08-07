package cn.numeron.discovery.plugin

import cn.numeron.discovery.core.DiscoverableImpl
import cn.numeron.discovery.core.DiscoveryCore
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.util.jar.JarFile

class DiscoveryTransform(project: Project) : AbstractTransform(project) {

    private val discoverableSet by lazy(DiscoveryCore::loadDiscoverable)
    private val implementationSet by lazy(DiscoveryCore::loadImplementation)

    private lateinit var discoveryLibraryJarFilePath: String

    init {
        DiscoveryCore.init(project.name, project.rootProject.buildDir.absolutePath)
    }

    override fun getName(): String = "Discovery"

    override fun isIncremental(): Boolean = true

    override fun processDirectory(inputDirFile: File, outputDirFile: File) {
        scanClasses(outputDirFile)
    }

    override fun processJar(inputJarFile: File, outputJarFile: File) {
        //通过MD5确定要修改的jar包。
        if (!::discoveryLibraryJarFilePath.isInitialized) {
            if (JarFile(outputJarFile).hasEntry(DISCOVERIES_CLASS)) {
                discoveryLibraryJarFilePath = outputJarFile.absolutePath
            }
        }
        //只扫描工程中的源码文件
        if (inputJarFile.name.endsWith("classes.jar")) {
            val unzipPath = outputJarFile.unzipTo()
            scanClasses(unzipPath)
            FileUtils.deleteDirectory(unzipPath)
        }
    }

    private fun scanClasses(classesDir: File) {
        classesDir.walkTopDown()
            .filter {
                val filename = it.name
                filename.endsWith(".class") &&
                        !filename.startsWith("BuildConfig") &&
                        !filename.startsWith("R$") &&
                        filename != "R.class"
            }
            .forEach {
                val classReader = ClassReader(it.readBytes())
                val className = classReader.className.toClassName()
                val interfaces = classReader.interfaces.map(String::toClassName)
                val discoverable = discoverableSet.find(interfaces::contains)
                wLog("discoverable = [$discoverable], className = [$className], interfaces = $interfaces")
                if (discoverable != null) {
                    implementationSet.add(DiscoverableImpl(className, discoverable))
                }
            }
    }

    override fun onTransformed() {
        wLog("implementation = $implementationSet")
        DiscoveryCore.saveImplementation(implementationSet)

        //整理为需要的格式
        val discoverableImpl = implementationSet
            .groupBy(DiscoverableImpl::discoverableName)
            .toMap()
            .mapValues {
                it.value.map(DiscoverableImpl::qualifierName)
            }

        //获取Discoveries.class所在的jar包
        val jarFile = File(discoveryLibraryJarFilePath)

        //解压，获取Discoveries.class
        val unzipPath = jarFile.unzipTo()
        val classFile = unzipPath.walkTopDown().first {
            it.name == "Discoveries.class"
        }

        //修改字节码
        val classReader = ClassReader(classFile.readBytes())
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val visitor = DiscoveryClassVisitor(classWriter, discoverableImpl)
        classReader.accept(visitor, 0)

        //重新写入到文件
        classFile.writeBytes(classWriter.toByteArray())

        //重新打包为jar文件
        unzipPath.zipTo()

        //删除解压的文件
        unzipPath.deleteRecursively()
    }

    private companion object {

        private const val DISCOVERIES_CLASS = "cn/numeron/discovery/Discoveries.class"

    }

}