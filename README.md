## Discovery

通过`AGP`实现的在`Android`工程多模块之间获取接口或抽象类的实现类的实例的辅助工具。

相比`ARouter`等路由框架的服务发现功能，`Discovery`主要功能在编译期间工作，不会在运行时扫描`dex`，有更好的性能。
相比`ServiceLoader`，`Discovery`支持抽象类，以及可以获取实现类的`class`对象，可以适配更丰富的其它框架。

通过在接口或抽象类上添加`@Discoverable`注解、并在实现类上添加`@Implementation`注解，就可以在工程中的任意模块中通过`Discoveries`类获取该接口或抽象类的实例，辅助开发者在模块之间访问数据。

演示工程：https://github.com/xiazunyang/DiscoveryDemo.git

### 原理

`Discovery`会在编译时扫描每个类文件，并将所有标记的类的信息通过`ASM`注册到`Discoveries`类中。

### 安装

当前最新版本：[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.numeron/discovery.plugin/badge.svg)](https://mvnrepository.com/artifact/cn.numeron/discovery.plugin)

1. 在根模块的`build.gradle`的适当位置添加以下代码：
    ```kotlin
    buildscript {
       repositories {
           ...
           mavenCentral()
       }
       dependencies {
           ...
           //添加Discovery插件
           classpath("cn.numeron:discovery.plugin:latest_version")
       }
    }
   ```

2. 在业务模块的`build.gradle`文件中添加以下代码：
    ```kotlin
    api("cn.numeron:discovery.library:latest_version")
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

### 使用

- 获取其它模块的业务服务

    1. 声明接口时标记`@Discovrable`注解

    ```kotlin
    @Discoverable
    interface ISignInService {
    
        /** 判断当前是否已登录 */
        suspend fun isSignIn(context: Context): Boolean
    
        /** 通过用户名和密码进行登录 */
        suspend fun signInByPassword(username: String, password: String)
  
  }
    ```

    2. 在任意模块中实现该接口，要求拥有无参构造方法，并标记`Implementation`注解

    ```kotlin
    @Implementation
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
    @Implementation(order = 0)
    class AModuleInitializer: IInitializer {
        override fun init(application: Application) {
            //init a module
        }
    }
    
    //需要初始化的B模块
    @Implementation(order = 10)
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
                //order数值小的实现类优先调用
                it.init(this)
            }
        }
    
    }
    ```

### 版本更新记录
- 1.4.1
    * `Implementation`注解中添加`order`属性，用于给实现类排序。

- 1.4.0
    * 添加抽象类的支持，不再强制要求参数是接口。
    * 不强制要求实现类拥有无参构造，但是`Discovery`不再参与创建这一类实现类的实例。
    * `Discoveries`中新增两个方法用于获取实现类的`Class`，方便用户自己创建它们的实例。

- 1.3.3
    * 当`getAllInstances`没有获取到任何实例的时候，不再抛出异常，改为返回一个空的列表。

- 1.3.2
    * `ASM`的`Option`降级至`ASM7`。

- 1.3.1
    * 修复`ASM`织入了错误的代码的问题。
  
- ~~1.3.0~~
    * **存在致命错误，请使用`1.3.1`版本**
    * 去除注解处理器模块，使配置简化。[查看之前的配置流程](README_1.2.2.md)
    * 修复增量编译的一些问题。 

- 1.2.2
    * 使用字符串作为配置名称，不再需要冗长的导包。
    * 编译时检查`Implementation`标记的类，要求必需拥有无参构造方法。

- 1.2.1
    * 当`Implementation`注解标记的类实现了多个接口时，会忽略掉未被`Discovrable`注解标记的接口。

- 1.2.0
    * 新增`Discovery`的配置选项，可配置实现类的处理方式。
    * 默认为`Scan`模式，即全局扫描，可配置为`Mark`模式，需要使用`Implementation`注解标记实现类，可免去扫描过程，以节省编译时间。

- 1.1.0
    * 注解处理器新增`APT`的实现，兼容`java`项目，与`KSP`任选其一即可。

- 1.0.0
    * 正式发布，由`KSP`和`AGP`实现主要功能。
