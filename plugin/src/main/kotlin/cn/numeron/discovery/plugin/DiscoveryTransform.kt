@file:Suppress("DEPRECATION")

package cn.numeron.discovery.plugin

import cn.numeron.discovery.core.DiscoverableImpl
import cn.numeron.discovery.core.DiscoveryCore
import cn.numeron.discovery.core.Modes
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.InputStream
import java.util.jar.JarFile

class DiscoveryTransform(project: Project) : AbstractTransform(project) {

    private val discoverableSet by lazy(DiscoveryCore::loadDiscoverable)
    private val implementationSet by lazy(DiscoveryCore::loadImplementation)

    private val isPassiveScan by lazy {
        DiscoveryCore.loadConfig().mode == Modes.Mark
    }

    /** Discovery库的jar文件路径 */
    private var discoveryLibraryJarFilePath: String? = null

    init {
        DiscoveryCore.init(project.name, project.rootProject.buildDir.absolutePath)
    }

    override fun getName(): String = "Discovery"

    override fun isIncremental(): Boolean = true

    override fun processDirectory(dirInput: DirectoryInput, outputDirFile: File) {
        if (isPassiveScan) {
            //如果配置为消极模式，则直接返回
            return
        }
        outputDirFile.walkTopDown()
            .filter {
                it.isFile && isClassFile(it.name)
            }
            .forEach {
                scanClasses(it.readBytes())
            }
    }

    override fun processJar(jarInput: JarInput, outputJarFile: File) {
        //通过jar包中有没有指定的class文件，确定是不是要找的jar包
        val jarFile = JarFile(outputJarFile)
        if (discoveryLibraryJarFilePath == null) {
            if (jarFile.hasEntry(DISCOVERIES_CLASS)) {
                discoveryLibraryJarFilePath = outputJarFile.absolutePath
            }
        }
        if (isPassiveScan) {
            //如果配置为消极模式，则直接返回
            return
        }
        //只扫描工程中的源码文件
        if (jarInput.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS)) {
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
    }

    private fun isClassFile(fileName: String): Boolean {
        return fileName.endsWith(".class")
                && fileName != "R.class"
                && !fileName.startsWith("BuildConfig")
                && !fileName.startsWith("R$")
    }

    /** 解析class文件，记录需要处理的class文件的路径 */
    private fun scanClasses(classBytes: ByteArray) {
        val classReader = ClassReader(classBytes)
        val className = classReader.className.toClassName()
        val interfaces = classReader.interfaces.map(String::toClassName)
        val discoverable = discoverableSet.find(interfaces::contains)
        wLog("discoverable = [$discoverable], className = [$className], interfaces = $interfaces")
        if (discoverable != null) {
            implementationSet.add(DiscoverableImpl(className, discoverable))
        }
    }

    override fun onTransformed() {
        wLog("implementation = $implementationSet")
        if (!isPassiveScan) {
            //如果是主动的处理方式，则将扫描到的结果保存起来，否则无需保存
            DiscoveryCore.saveImplementation(implementationSet)
        }

        //整理为需要的格式
        val discoverableImpl = implementationSet
            .groupBy(DiscoverableImpl::discoverableName)
            .toMap()
            .mapValues {
                it.value.map(DiscoverableImpl::qualifierName)
            }

        //获取Discoveries.class所在的jar包
        val jarFile = File(discoveryLibraryJarFilePath ?: throw NullPointerException("Not found Discovery Library."))

        //解压，获取Discoveries.class
        val unzipPath = jarFile.unzipTo()
        val classFile = unzipPath.walkTopDown().first {
            it.isFile && it.name == "Discoveries.class"
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

    companion object {

        const val DISCOVERIES_CLASS = "cn/numeron/discovery/Discoveries.class"

    }

}