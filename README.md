## Discovery

通过`KSP`及`AGP`实现的在Android工程多模块之间获取接口的实例对象的辅助工具。

通过在接口上添加`@Discoverable`注解后，在工程中的任意模块中通过`Discoveries`类获取该接口的实例，辅助开发者在模块之间访问数据。



### 原理

`Discovery`由3个模块构成，分别是基于`Kotlin Symbol Processor`的注解处理器模块、基于`Android Gradle Plugin`的插件模块以及一个`kotlin`库模块。



- `kotlin`模块
  - 包含一个`Discovrable`注解，及一个`Discovries`类。

- `KSP`模块

  - 在编译期间，获取所有被`Discoverable`注解标记的接口的信息，生成一个列表并记录下来。

- `AGP`模块

  - 在编译过程中，扫描每个模块中的类文件，并将上述列表中接口的实现类通过`ASM`注册到`kotlin`模块的`Discoveries`类中。

  

### 安装

当前最新版本：[![](https://jitpack.io/v/cn.numeron/discovery.svg)](https://jitpack.io/#cn.numeron/discovery)

1. 在根模块的`build.gradle`的适当位置添加以下代码：

   ```kotlin
buildscript {
       repositories {
           google()
           mavenCentral()
           gradlePluginPortal()
           maven("https://jitpack.io")
       }
       dependencies {
           //添加Discovery插件
           classpath("cn.numeron.discovery:plugin:latest_version")
           //添加KSP插件
           classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.5.21-1.0.0-beta06")
       }
}
   ```

2. 在需要使用`@Discoverable`注解的模块中的`build.gradle`文件中添加以下代码：

   ```kotlin
   plugins {
       id("com.android.library")
       ...
       //应用KSP插件
       id("com.google.devtools.ksp")
   }
   
   ...
   
   dependencies {
       ...
       //应用Discovery的KSP插件
       ksp("cn.numeron.discovery:ksp:latest_version")
       //添加Discovery library库
       implementation("cn.numeron.discovery:library:latest_version")
   }
   
   ...
   
   ksp {
   	//设置此模块的唯一标识和根项目的编译目录
       arg("projectName", "module-name")
       arg("rootProjectBuildDir", rootProject.buildDir.absolutePath)
   }

3. 在主模块的`build.gradle`文件中添加以下代码：

   ```
   plugins {
		id("com.android.application")
		...
		//应用Discovery插件
		id("discovery")
   }
   ```

### 使用

- 获取其它模块的业务服务

  1. 在接口上使用`@Discovrable`注解

    ```kotlin
    @Discoverable
    interface ISignInService {
    
    	/** 判断当前是否已登录 */
    	suspend fun isSignIn(context: Context): Boolean
    
    	/** 通过用户名和密码进行登录 */
    	suspend fun signInByPassword(username: String, password: String)
  
  }
    ```

  2. 在任意模块中实现`ISignInService`接口

    ```kotlin
    class SignInServiceImpl: ISignInService {
    
    	override suspend fun isSignIn(context: Context): Boolean {
    		TODO()
    	}
    
    	override suspend fun signInByPassword(username: String, password: String) {
    		TODO()
    	}
    
    }
    ```

  3. 在任意模块的代码中通过`Discoveries`获取接口实例
    ```kotlin
    lifecycleScope.launch {
        val signInService = Discoveries.getInstance<ISignInService>()
        if (!signInService.isSignIn(requireContext())) {
            //未登录， do something...
        }
    }
    ```

- 获取所有模块中的所有实例

    1. 在基础模块中声明初始化接口

    ```
    @Discoverable
    interface IInitiator {
    
        fun init(application: Application)
    
    }
    ```
    
    2. 在其它模块中实现该接口
    
    ```
    //需要初始化的A模块
    class AModuleInitiator: IInitiator {
    	override fun init(application: Application) {
    		//init a module
    	}
    }
    
    //需要初始化的B模块
    class BModuleInitiator: IInitiator {
    	override fun init(application: Application) {
    		//init b module
    	}
    }
    ```
    
    3. 在`Application`中获取所有实例并初始化
    ```
    class MyApplication: Application() {
    
    	override fun onCreate() {
    		//获取所有IInitiator的实现，并执行init方法
    		List<IInitiator> initiatorList = Discoveries.getAllInstances<IInitiator>()
    		initiatorList.forEach {
    			it.init(this)
    		}
    	}
    
    }
    ```