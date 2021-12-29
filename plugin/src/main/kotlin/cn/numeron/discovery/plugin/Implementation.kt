package cn.numeron.discovery.plugin

data class Implementation(

    val classpath: String,

    val superTypes: List<String>,

    val order: Int

) : Comparable<Implementation> {

    override fun compareTo(other: Implementation): Int {
        return order - other.order
    }

}