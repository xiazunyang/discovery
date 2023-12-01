package cn.numeron.discovery

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

abstract class DiscoveryTask : DefaultTask() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun taskAction() {
        // 记录@Discoverable注解标记的类全名
        val discoverableSet = mutableSetOf<String>()
        // 记录@Implementation注解标记的类信息
        val implementationSet = mutableSetOf<Implementation>()
        // class输出到的jar文件
        val outputFile = output.get().asFile
        // 记录discovery.library文件的信息
        var discoveryLibraryJarFile: File? = null
        // 记录已添加到输出jar文件中的条目
        val jarEntries = sortedSetOf<String>()
        // 输出的jar文件流
        val jarOutput = JarOutputStream(outputFile.outputStream())

        // 1.处理所有的jar文件
        allJars.get().asSequence().map(RegularFile::getAsFile).forEach { file ->
            // 扫描每一个Jar文件
            val jarFile = JarFile(file)
            // 在扫描过程中查找discovery.library文件
            if (isDiscoveryLibraryJarFile(jarFile)) {
                if (discoveryLibraryJarFile == null) {
                    discoveryLibraryJarFile = file
                    logger.warn("discovery library jar file path: $file.")
                }
            } else {
                jarFile.entries().asSequence().forEach { jarEntry ->
                    // 读取jarEntry的数据
                    val classData = jarFile.getInputStream(jarEntry).use(InputStream::readBytes)
                    // 把输入的jar文件中的entry复制到output文件中
                    val jarEntryName = jarEntry.name
                    if (jarEntryName !in jarEntries) {
                        jarOutput.putNextEntry(JarEntry(jarEntryName))
                        jarEntries.add(jarEntryName)
                        jarOutput.write(classData)
                        jarOutput.closeEntry()
                    }
                    // 判断是否是需要扫描的文件，并执行扫描操作
                    val name = jarEntry.name.substringAfterLast('/')
                    if (!jarEntry.isDirectory && isNeededClassFile(name)) {
                        scanClasses(classData, discoverableSet, implementationSet)
                    }
                }
            }
            jarFile.close()
        }

        // 2.处理文件夹中的class文件
        allDirectories.get().asSequence().map(Directory::getAsFile).forEach { directory ->
            directory.walkTopDown().forEach { file ->
                // 把输入的jar文件中的entry复制到output文件中
                val jarEntryName = directory.toURI().relativize(file.toURI()).getPath().replace(File.separatorChar, '/')
                if (jarEntryName !in jarEntries) {
                    jarOutput.putNextEntry(JarEntry(jarEntryName))
                    jarEntries.add(jarEntryName)
                    if (file.isFile) {
                        // 读取class文件的数据
                        val classData = file.readBytes()
                        // 写入到jar文件中
                        jarOutput.write(classData)
                        jarOutput.closeEntry()
                        // 判断是否是需要扫描的文件，并执行扫描操作
                        if (isNeededClassFile(file)) {
                            scanClasses(classData, discoverableSet, implementationSet)
                        }
                    }
                }
            }
        }

        // 3.把扫描到的文件记录到Discoveries.class文件中，并写入到jarOutput中
        JarFile(discoveryLibraryJarFile).use { jarFile ->
            jarFile.entries().asSequence().forEach { jarEntry ->
                // 获取class数据
                var classData = jarFile.getInputStream(jarEntry).use(InputStream::readBytes)
                // 如果是Discoveries类，则注入扫描到的类信息
                val jarEntryName = jarEntry.name
                if (jarEntryName == DISCOVERIES_CLASS) {
                    //整理为需要的格式
                    val discoveries = composingDiscoveries(discoverableSet, implementationSet)
                    // 重写Discoveries类
                    classData = injectToDiscoveryClass(classData, discoveries)
                }
                // 保存到jarOutput中
                if (jarEntryName !in jarEntries) {
                    jarOutput.putNextEntry(JarEntry(jarEntryName))
                    jarEntries.add(jarEntryName)
                    jarOutput.write(classData)
                    jarOutput.closeEntry()
                }
            }
        }

        // 4.最后关闭打开的输出流
        jarOutput.close()
    }

    private fun composingDiscoveries(
        discoverableSet: MutableSet<String>,
        implementationSet: MutableSet<Implementation>
    ): Map<String, List<String>> {
        //整理为需要的格式
        return implementationSet
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
    }

    private fun injectToDiscoveryClass(classData: ByteArray, discoveries: Map<String, List<String>>): ByteArray {
        val classReader = ClassReader(classData)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val visitor = DiscoveriesClassModifyVisitor(classWriter, discoveries)
        classReader.accept(visitor, 0)
        //获取修改后的class
        return classWriter.toByteArray()
    }

    /** 解析class文件，记录需要处理的class文件的路径 */
    private fun scanClasses(
        classBytes: ByteArray,
        discoverableSet: MutableSet<String>,
        implementationSet: MutableSet<Implementation>
    ) {
        val classReader = ClassReader(classBytes)
        val discoveryVisitor = DiscoveryVisitor()
        classReader.accept(discoveryVisitor, 0)
        val className = classReader.className.let(::toClassName)
        if (discoveryVisitor.hasAnnotation(DISCOVERABLE_ANNOTATION)) {
            if (discoveryVisitor.isAbstract()) {
                discoverableSet.add(className)
            }
        }
        if (discoveryVisitor.hasAnnotation(IMPLEMENTATION_ANNOTATION)) {
            val interfaces = discoveryVisitor.interfaces.map(::toClassName)
            val superTypes = interfaces + discoveryVisitor.superName.let(::toClassName)
            implementationSet.add(
                Implementation(
                    classpath = className,
                    superTypes = superTypes,
                    order = discoveryVisitor.order
                )
            )
        }
    }

    private fun isNeededClassFile(file: File): Boolean {
        return file.isFile && isNeededClassFile(file.name)
    }

    private fun isNeededClassFile(fileName: String): Boolean {
        return fileName.endsWith(".class")
                && fileName != "R.class"
                && fileName != "BuildConfig.class"
                && !fileName.startsWith("R$")
    }

    private fun isDiscoveryLibraryJarFile(jarFile: JarFile): Boolean {
        return jarFile.getEntry(DISCOVERIES_CLASS) != null
    }

    private fun toClassName(className: String) = className.replace('/', '.')

    companion object {

        internal const val DISCOVERIES_CLASS = "cn/numeron/discovery/Discoveries.class"

        internal const val DISCOVERABLE_ANNOTATION = "Lcn/numeron/discovery/Discoverable;"

        internal const val IMPLEMENTATION_ANNOTATION = "Lcn/numeron/discovery/Implementation;"

    }

}