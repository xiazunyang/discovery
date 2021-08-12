package cn.numeron.discovery.kcp

import cn.numeron.discovery.core.DiscoveryCore
import cn.numeron.discovery.core.Modes
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class DiscoveryCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "${NUMERON_GROUP}.${NUMERON_ARTIFACT}"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(DiscoveryCore.PROJECT_NAME, "<project name>", "Applied plugin project identity."),
        CliOption(DiscoveryCore.ROOT_PROJECT_BUILD_DIR, "<root project build dir>", "The root project build directory absolute path."),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            DiscoveryCore.PROJECT_NAME -> {
                configuration.put(KEY_PROJECT_NAME, value)
            }
            DiscoveryCore.ROOT_PROJECT_BUILD_DIR -> {
                configuration.put(KEY_ROOT_PROJECT_BUILD_DIR, value)
            }
        }
    }

}