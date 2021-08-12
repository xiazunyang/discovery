package cn.numeron.discovery.core

import java.io.Serializable

open class DiscoveryConfig : Serializable {

    var mode: Modes = Modes.Scan

    companion object {

        private const val serialVersionUID = -4940332577895232370L

    }

}