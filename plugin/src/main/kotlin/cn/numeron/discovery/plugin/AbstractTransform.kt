@file:Suppress("DEPRECATION")

package cn.numeron.discovery.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import java.io.File

abstract class AbstractTransform(protected val project: Project) : Transform() {

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun transform(transformInvocation: TransformInvocation) {
        //存放输出文件的目录
        val outputProvider = transformInvocation.outputProvider

        if (!isIncremental) {
            //如果未开启增量编译，则清除缓存
            transformInvocation.outputProvider.deleteAll()
        }

        for (transformInput in transformInvocation.inputs) {

            for (jarInput in transformInput.jarInputs) {
                //获取输出目录
                val outputJarFile = outputProvider.getContentLocation(
                    jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
                )
                if (isIncremental && jarInput.status == Status.REMOVED) {
                    //如果开启了增量编译并且当该jar被移除，则从输出目录中移除掉
                    FileUtils.deleteDirectory(outputJarFile)
                } else {
                    //如果没有开启增量编译，或者jar是其它状态，则复制到输出目录
                    FileUtils.copyFile(jarInput.file, outputJarFile)
                }
                if (!isIncremental || jarInput.status != Status.REMOVED) {
                    //处理jar
                    processJar(jarInput.file, outputJarFile)
                }
            }

            for (dirInput in transformInput.directoryInputs) {
                //复制到输出目录
                val outputDir = outputProvider.getContentLocation(
                    dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY
                )
                FileUtils.copyDirectory(dirInput.file, outputDir)
                //处理本地class
                processDirectory(dirInput.file, outputDir)
            }
        }
        onTransformed()
    }

    protected fun iLog(message: String, vararg args: Any) {
        project.logger.log(LogLevel.INFO, message, *args)
    }

    protected fun eLog(message: String, vararg args: Any) {
        project.logger.log(LogLevel.ERROR, message, *args)
    }

    protected fun wLog(message: String, vararg args: Any) {
        project.logger.log(LogLevel.WARN, message, *args)
    }

    protected open fun processJar(inputJarFile: File, outputJarFile: File) = Unit

    protected open fun processDirectory(inputDirFile: File, outputDirFile: File) = Unit

    protected open fun onTransformed() = Unit

}