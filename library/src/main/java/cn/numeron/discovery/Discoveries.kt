package cn.numeron.discovery

import java.lang.ref.WeakReference

/**
 * Discovery的单例实现
 *
 * 通过此类中的[getInstance]和[getAllInstances]方法获取指定接口所有的实现类。
 */
class Discoveries private constructor() {

    /** 记录已创建的实例 */
    private val instanceOfMap = mutableMapOf<String, WeakReference<Any>>()

    /** 记录已发现的服务 */
    private val discovered = mutableMapOf<String, MutableSet<String>>()

    fun <T> getInstance(clazz: Class<T>): T {
        if (!clazz.isInterface) {
            throw DiscoveryException("Parameter must be a interface: $clazz.")
        }
        val implClassName = discovered[clazz.name]?.firstOrNull()
            ?: throw DiscoveryException("The implementation was not found: $clazz.")
        return getOrPutInstance(implClassName).let(clazz::cast)
    }

    fun <T> getAllInstances(clazz: Class<T>): List<T> {
        if (!clazz.isInterface) {
            throw DiscoveryException("Parameter must be a interface: $clazz.")
        }
        val implementationList = discovered[clazz.name]
            ?: throw DiscoveryException("The implementation was not found: $clazz.")
        return implementationList
            .map(::getOrPutInstance)
            .map(clazz::cast)
    }

    /** 从缓存中获取实例，如果没有，则创建一条保存到缓存中并返回 */
    private fun getOrPutInstance(className: String): Any {
        var instance = instanceOfMap[className]?.get()
        if (instance != null) {
            return instance
        }
        instance = getInstance(className)
        instanceOfMap[className] = WeakReference(instance)
        return instance
    }

    @Suppress("DEPRECATION")
    private fun getInstance(className: String): Any {
        return when (className) {
            "aa" -> Discoveries()
            "bb" -> Object()
            else -> throw DiscoveryException("The implementation was not found: $className.")
        }
    }

    private fun addImplementation(discoverable: String, implementation: String) {
        val mutableSet = discovered[discoverable] ?: mutableSetOf()
        mutableSet.add(implementation)
        discovered[discoverable] = mutableSet
    }

    companion object {

        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED, ::Discoveries)

        inline fun <reified T> getInstance() = instance.getInstance(T::class.java)

        inline fun <reified T> getAllInstances() = instance.getAllInstances(T::class.java)

    }

}

