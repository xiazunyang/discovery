package cn.numeron.discovery.core

import java.io.Serializable

open class DiscoveryConfig : Serializable {

    var workMode: Modes = Modes.Scan

    var mode: String
        get() = workMode.name
        set(value) {
            val modeName = value.first().uppercase() + value.substring(1).lowercase()
            this.workMode = Modes.valueOf(modeName)
        }

    companion object {

        private const val serialVersionUID = -4940332577895232370L

    }

}