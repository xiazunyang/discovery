## Discovery

通过`KSP/APT`及`AGP`实现的在Android工程多模块之间获取接口的实例对象的辅助工具。

通过在接口上添加`@Discoverable`注解后，在工程中的任意模块中通过`Discoveries`类获取该接口的实例，辅助开发者在模块之间访问数据。

### 原理

`Discovery`由3个功能模块构成，分别是注解处理器模块、`Gradle`插件模块以及一个`kotlin`库模块。

- `kotlin`模块
    - 包含两个注解，及一个`Discoveries`类。

- 注解处理器模块
    - 在编译前，获取所有被`Discoverable`注解标记的接口的信息，生成一个列表并记录下来。

- `Gradle`插件模块
    - 在编译中，扫描每个模块中的类文件，并将上述列表中接口的实现类通过`ASM`注册到`Discoveries`类中。

### 安装

当前最新版本：[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.numeron/discovery.core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.numeron/discovery.core)

1. 在根模块的`build.gradle`的适当位置添加以下代码：
    ```kotlin
    buildscript {
       repositories {
           ...
           mavenCentral()
           //如果使用KSP，则必需配置以下仓库
           google()
           gradlePluginPortal()
       }
       dependencies {
           ...
           //添加Discovery插件
           classpath("cn.numeron:discovery.plugin:latest_version")
           //添加KSP插件，如果使用APT，则不需要添加
           classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.5.21-1.0.0-beta06")
       }
    }
   ```

2. 在需要使用注解的模块中启用注解处理器，选择`apt`或`ksp`其中的一种方式配置即可。
    - `KSP`方式(`Kotlin`工程推荐使用)
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
       ksp("cn.numeron:discovery.ksp:latest_version")
       //添加Discovery library库
       implementation("cn.numeron:discovery.library:latest_version")
   }
   
   ...
   
   ksp {
       //设置此模块的唯一标识和编译根目录
       arg("projectName", "module-name")
       arg("rootProjectBuildDir", rootProject.buildDir.absolutePath)
   }
   ```
    * `APT`方式(`Java`请使用此方式)
    ```kotlin
     plugins {
         id("com.android.library")
         ...
         //应用kapt插件
         id("kotlin-kapt")
     }
     
     ...
     
     dependencies {
         ...
         //应用Discovery的APT插件
         kapt("cn.numeron:discovery.apt:latest_version")
         //添加Discovery library库
         implementation("cn.numeron:discovery.library:latest_version")
     }
     
     ...
     
     kapt {
        arguments {
            //设置此模块的唯一标识和编译根目录
            arg("projectName", "asset-api")
            arg("rootProjectBuildDir", rootProject.buildDir.absolutePath)
        }
     }
     ```

3. 在主模块的`build.gradle`文件中添加以下代码：
    ```kotlin
    plugins {
        id("com.android.application")
        ...
        //应用Discovery插件
        id("discovery")
   }
    ```

### 配置
- `Discovery`默认情况下，通过扫描所有的`class`文件来获取接口的实现类，在大型项目中可能比较慢。为了节省编译时间，在`1.2.0`版本开始加入了可配置功能，配置方式如下：
  
    ```kotlin
    //在应用了Discovery插件的模块的build.gradle文件中添加以下代码
    discovery {
        //Discovery有Scan和Mark两种工作模式
        //Scan为默认工作模式，会扫描除依赖以外所有的类文件
        //Mark为可选的工作模式，此模式下只会处理被Implementation注解标记的类文件
        //Mark模式需要在每一个实现类上添加Implementation注解，并配置注解处理器方可工作
        mode = cn.numeron.discovery.core.Modes.Mark
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
    //如果工作模式配置为Mark，需要在此类上添加Implementation注解
    class SignInServiceImpl: ISignInService {
    
        override suspend fun isSignIn(context: Context): Boolean {
            TODO("判断是否已经登录")
        }
    
        override suspend fun signInByPassword(username: String, password: String) {
            TODO("根据提供的账号密码进行登录")
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

    ```kotlin
    @Discoverable
    interface IInitializer {
    
        fun init(application: Application)
    
    }
    ```

    2. 在其它模块中实现该接口

    ```kotlin
    //需要初始化的A模块
    //如果工作模式配置为Mark，需要在此类上添加Implementation注解
    class AModuleInitializer: IInitializer {
        override fun init(application: Application) {
            //init a module
        }
    }
    
    //需要初始化的B模块
    //如果工作模式配置为Mark，需要在此类上添加Implementation注解
    class BModuleInitializer: IInitializer {
        override fun init(application: Application) {
            //init b module
        }
    }
    ```

    3. 在`Application`中获取所有实例并初始化
    ```kotlin
    class MyApplication: Application() {
    
        override fun onCreate() {
            //获取所有IInitiator的实现，并执行init方法
            val initializerList = Discoveries.getAllInstances<IInitializer>()
            initializerList.forEach {
                it.init(this)
            }
        }
    
    }
    ```

### 版本更新记录

- 1.2.0
    1. 新增`Discovery`的配置选项，可配置实现类的处理方式。
    2. 默认为`Scan`模式，即全局扫描，可配置为`Mark`模式，需要使用`Implementation`注解标记实现类，可免去扫描过程，以节省编译时间。

- 1.1.0
    1. 注解处理器新增`APT`的实现，兼容`java`项目，与`KSP`任选其一即可。

- 1.0.0
    1. 正式发布，由`KSP`和`AGP`实现主要功能。