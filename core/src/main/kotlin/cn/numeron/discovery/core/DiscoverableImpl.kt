package cn.numeron.discovery.core

import java.io.Serializable

data class DiscoverableImpl(

    val qualifierName: String,

    val discoverableName: String

) : Serializable {

    companion object {

        private const val serialVersionUID = -4940583367895232370L

    }

}
