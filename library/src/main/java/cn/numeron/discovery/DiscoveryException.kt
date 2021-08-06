package cn.numeron.discovery

class DiscoveryException(override val message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)