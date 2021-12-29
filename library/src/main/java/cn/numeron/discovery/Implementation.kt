package cn.numeron.discovery

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class Implementation(

    /** 此实现类加入列表时的顺序，值越小，越靠前 */
    val order: Int = 0

)
