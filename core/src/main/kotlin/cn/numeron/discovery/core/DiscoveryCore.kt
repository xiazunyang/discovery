package cn.numeron.discovery.core

import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@Suppress("UNCHECKED_CAST")
object DiscoveryCore {

    const val PROJECT_NAME = "projectName"
    const val ROOT_PROJECT_BUILD_DIR = "rootProjectBuildDir"

    private const val DISCOVERY_CONFIG_NAME = "config"
    private const val DISCOVERABLE_DIR_NAME = "discoverable"
    private const val IMPLEMENTATION_DIR_NAME = "implementation"

    private lateinit var projectName: String
    private lateinit var rootProjectBuildDir: String

    fun init(projectName: String?, rootProjectBuildDir: String?) {
        if (projectName.isNullOrEmpty()) {
            throw IllegalArgumentException("Please add argument: `arg(\"$PROJECT_NAME\": name)`.")
        }
        if (rootProjectBuildDir.isNullOrBlank()) {
            throw IllegalArgumentException("Please add argument: `arg(\"$ROOT_PROJECT_BUILD_DIR\": rootProject.buildDir.absolutePath)`.")
        }
        this.projectName = projectName
        this.rootProjectBuildDir = rootProjectBuildDir + File.separator + "numeron" + File.separator + "discovery"
    }

    /** 获取Discovery处理的文件中最后修改的时间 */
    fun lastModifiedTime(): Long {
        val rootProjectBuildDir = File(rootProjectBuildDir)
        return rootProjectBuildDir
            .walkTopDown()
            .maxOf(File::lastModified)
    }

    /** 保存本模块中被标记的接口 */
    fun saveDiscoverable(discoverableSet: Set<String>) {
        val discoverableDir = File(rootProjectBuildDir, DISCOVERABLE_DIR_NAME)
        if (!discoverableDir.exists()) {
            discoverableDir.mkdirs()
        }
        val discoverableFile = File(discoverableDir, projectName)
        ObjectOutputStream(discoverableFile.outputStream()).use {
            it.writeObject(discoverableSet)
        }
    }

    /** 加载本模块中被标记的接口 */
    fun loadDiscoverable(): MutableSet<String> {
        val discoverableSet = mutableSetOf<String>()
        //只读取本模块中的实现类
        val file = File(rootProjectBuildDir, DISCOVERABLE_DIR_NAME + File.separator + projectName)
        if (!file.exists()) {
           return discoverableSet
        }
        ObjectInputStream(file.inputStream()).use {
            val readObject = it.readObject()
            val set = readObject as Set<String>
            discoverableSet.addAll(set)
        }
        return discoverableSet.toMutableSet()
    }

    /** 加载所有被标记的接口 */
    fun loadAllDiscoverable(): MutableSet<String> {
        val discoverableSet = mutableSetOf<String>()
        File(rootProjectBuildDir, DISCOVERABLE_DIR_NAME)
            .listFiles()
            ?.forEach { file ->
                ObjectInputStream(file.inputStream()).use {
                    val readObject = it.readObject()
                    val set = readObject as Set<String>
                    discoverableSet.addAll(set)
                }
            }
        return discoverableSet
    }

    /** 保存本模块中的实现类 */
    fun saveImplementation(implementationSet: Set<DiscoverableImpl>) {
        //保存本模块中的实现类
        val implementationDir = File(rootProjectBuildDir, IMPLEMENTATION_DIR_NAME)
        if (!implementationDir.exists()) {
            implementationDir.mkdirs()
        }
        val implementationFile = File(implementationDir, projectName)
        ObjectOutputStream(implementationFile.outputStream()).use {
            it.writeObject(implementationSet)
        }
    }

    /** 加载本模块中的实现类 */
    fun loadImplementation(): MutableSet<DiscoverableImpl> {
        val implementationSet = mutableSetOf<DiscoverableImpl>()
        //只读取本模块中的数据
        val file = File(rootProjectBuildDir, IMPLEMENTATION_DIR_NAME + File.separator + projectName)
        if (!file.exists()) {
            return implementationSet
        }
        ObjectInputStream(file.inputStream()).use {
            val readObject = it.readObject()
            val set = readObject as Set<DiscoverableImpl>
            implementationSet.addAll(set)
        }
        return implementationSet
    }

    /** 加载所有的实现类 */
    fun loadAllImplementation(): MutableSet<DiscoverableImpl> {
        val implementationSet = mutableSetOf<DiscoverableImpl>()
        //加载根模块中的实现类
        File(rootProjectBuildDir, IMPLEMENTATION_DIR_NAME)
            .listFiles()
            ?.forEach { file ->
                ObjectInputStream(file.inputStream()).use {
                    val readObject = it.readObject()
                    val set = readObject as Set<DiscoverableImpl>
                    implementationSet.addAll(set)
                }
            }
        return implementationSet
    }

    /** 保存Discovery的配置 */
    fun saveConfig(config: DiscoveryConfig) {
        val configFile = File(rootProjectBuildDir, DISCOVERY_CONFIG_NAME)
        if (!configFile.parentFile.exists()) {
            configFile.parentFile.mkdirs()
        }
        ObjectOutputStream(configFile.outputStream()).use {
            val discoveryConfig = DiscoveryConfig()
            discoveryConfig.workMode = config.workMode
            it.writeObject(discoveryConfig)
        }
    }

    /** 获取Discovery的配置 */
    fun loadConfig(): DiscoveryConfig {
        val configFile = File(rootProjectBuildDir, DISCOVERY_CONFIG_NAME)
        return try {
            ObjectInputStream(configFile.inputStream()).use {
                it.readObject() as DiscoveryConfig
            }
        } catch (e: Throwable) {
            DiscoveryConfig()
        }
    }

}