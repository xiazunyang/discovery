package cn.numeron.discovery

import java.lang.ref.WeakReference
import java.lang.reflect.Modifier

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

    /** 获取该项目中该class的实现类的实例 */
    fun <T> getInstance(clazz: Class<T>): T {
        if (!Modifier.isAbstract(clazz.modifiers)) {
            throw DiscoveryException("Parameter must be a abstract class: $clazz.")
        }
        val implClassName = discovered[clazz.name]?.firstOrNull()
            ?: throw DiscoveryException("The implementation was not found: $clazz.")
        return getOrPutInstance(implClassName).let(clazz::cast)
    }

    /** 获取该项目中该class所有的实现类的实例 */
    fun <T> getAllInstances(clazz: Class<T>): List<T> {
        if (!Modifier.isAbstract(clazz.modifiers)) {
            throw DiscoveryException("Parameter must be a abstract class: $clazz.")
        }
        val implementationList = discovered[clazz.name] ?: return emptyList()
        return implementationList
            .map(::getOrPutInstance)
            .map(clazz::cast)
    }

    /** 获取该项目中该class的实现类 */
    fun <T> getImplementClass(clazz: Class<T>): Class<out T> {
        if (!Modifier.isAbstract(clazz.modifiers)) {
            throw DiscoveryException("Parameter must be a abstract class: $clazz.")
        }
        val implClassName = discovered[clazz.name]?.firstOrNull()
            ?: throw DiscoveryException("The implementation class was not found: $clazz.")
        return getImplementClass(implClassName).asSubclass(clazz)
    }

    /** 获取该项目中该class所有的实现类 */
    fun <T> getAllImplementClasses(clazz: Class<T>): List<Class<out T>> {
        if (!Modifier.isAbstract(clazz.modifiers)) {
            throw DiscoveryException("Parameter must be a abstract class: $clazz.")
        }
        val implementationList = discovered[clazz.name] ?: return emptyList()
        return implementationList.map {
            getImplementClass(it).asSubclass(clazz)
        }
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

    private fun getInstance(className: String): Any {
        return getImplementClass(className).newInstance()
    }

    private fun getImplementClass(className: String): Class<*> {
        return Class.forName(className)
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

        inline fun <reified T> getImplementClass() = instance.getImplementClass(T::class.java)

        inline fun <reified T> getAllImplementClasses() = instance.getAllImplementClasses(T::class.java)

    }

}

