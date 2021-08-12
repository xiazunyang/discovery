package cn.numeron.discovery.kcp

import cn.numeron.discovery.core.DiscoveryCore
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal const val NUMERON_GROUP = "cn.numeron"

internal const val NUMERON_ARTIFACT = "discovery"

internal val KEY_PROJECT_NAME = CompilerConfigurationKey<String>(DiscoveryCore.PROJECT_NAME)

internal val KEY_ROOT_PROJECT_BUILD_DIR = CompilerConfigurationKey<String>(DiscoveryCore.ROOT_PROJECT_BUILD_DIR)