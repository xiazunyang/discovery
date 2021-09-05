package cn.numeron.discovery.kotlin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class DiscoveryCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = BuildConfig.ARTIFACT_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf()

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) = Unit

}