### [中文](README.md) | English

## Discovery

An auxiliary tool implemented through `AGP` for obtaining instances of interfaces or abstract classes between multiple modules in an Android project.  
By adding the `@Discoverable` annotation to an interface or abstract class and the `@Implementation` annotation to an implementation class, you can use the `Discoveries` class to obtain instances of that interface or abstract class from any module in the project, assisting developers in accessing data between modules.

Compared to routing frameworks like `ARouter`, `Discovery` primarily works during compile-time, offering better performance. It provides only service discovery functionality, allowing developers to implement richer functionalities.  
Compared to `ServiceLoader`, `Discovery` supports abstract classes and allows obtaining the `class` object of the implementation class, making it compatible with a wider range of other frameworks.

Demo project: https://github.com/xiazunyang/DiscoveryDemo.git

### Principles

`Discovery` scans each class file at compile time and registers information about all marked classes through `ASM` to the `Discoveries` class.

### Installation

Current latest version: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.numeron/discovery.library/badge.svg)](https://mvnrepository.com/artifact/cn.numeron/discovery.library)

1. Add the following code to the appropriate location in the `build.gradle` file of the `app` module in the Android project:
    ```kotlin
    plugins {
        ...
        id("cn.numeron.discovery") version "2.0.0"        
    }
    ```

2. Add the following code to the `build.gradle` file of the base module in the Android project:
    ```kotlin
    dependencies {
        ...
        api("cn.numeron:discovery.library:2.0.0")
    }
    ```    

### Usage

- Obtain business services from other modules

    1. Mark the interface with the `@Discoverable` annotation
    ```kotlin
    @Discoverable
    interface ISignInService {
    
        /** Check if the user is signed in */
        suspend fun isSignIn(context: Context): Boolean
    
        /** Sign in with username and password */
        suspend fun signInByPassword(username: String, password: String)
    }
    ```

    2. Implement the interface in any module, requiring a no-argument constructor, and mark it with the `@Implementation` annotation
    ```kotlin
    @Implementation
    class SignInServiceImpl: ISignInService {
    
        override suspend fun isSignIn(context: Context): Boolean {
            TODO("Check if the user is signed in")
        }
    
        override suspend fun signInByPassword(username: String, password: String) {
            TODO("Sign in with the provided credentials")
        }
    }
    ```

    3. Obtain an instance of the interface using `Discoveries` in any module
    ```kotlin
    lifecycleScope.launch {
        val signInService = Discoveries.getInstance<ISignInService>()
        if (!signInService.isSignIn(requireContext())) {
            // Not signed in, do something...
        }
    }
    ```

- Obtain all instances from all modules

    1. Declare the initialization interface in the base module
    ```kotlin
    @Discoverable
    interface IInitializer {
    
        fun init(application: Application)
    }
    ```

    2. Implement the interface in other modules
    ```kotlin
    // Initialization for Module A
    @Implementation(order = 0)
    class AModuleInitializer: IInitializer {
        override fun init(application: Application) {
            // Initialize module A
        }
    }
    
    // Initialization for Module B
    @Implementation(order = 10)
    class BModuleInitializer: IInitializer {
        override fun init(application: Application) {
            // Initialize module B
        }
    }
    ```

    3. Obtain all instances and initialize them in the `Application` class
    ```kotlin
    class MyApplication: Application() {
    
        override fun onCreate() {
            // Obtain all instances of IInitializer and call the init method
            val initializerList = Discoveries.getAllInstances<IInitializer>()
            initializerList.forEach {
                // Initialize modules with lower order values first
                it.init(this)
            }
        }
    }
    ```

### Version History
- **2.0.0**
    * Update the Gradle version to 8.0.

- 1.4.2
    * Fix the issue where it fails to get the implementation class that inherits from an abstract class.
    * [View old documentation](README_1.4.2.md)

- 1.4.1
    * Add the `order` property to the `Implementation` annotation for sorting implementation classes.

- 1.4.0
    * Add support for abstract classes, no longer requiring parameters to be interfaces.
    * No longer enforce implementation classes to have a no-argument constructor, but `Discovery` no longer participates in creating instances of such implementation classes.
    * Add two methods to `Discoveries` for obtaining the `Class` of implementation classes, facilitating users to create their instances.

- 1.3.3
    * When `getAllInstances` doesn't retrieve any instances, it no longer throws an exception but instead returns an empty list.

- 1.3.2
    * Downgrade `ASM`'s `Option` to `ASM7`.

- 1.3.1
    * Fix the issue where incorrect code was woven by `ASM`.

- ~~1.3.0~~
    * **Critical error exists, please use version `1.3.1`**
    * Remove the annotation processor module, simplifying the configuration. [View the previous configuration process](README_1.2.2.md)
    * Fix some issues with incremental compilation.

- 1.2.2
    * Use strings as configuration names, eliminating the need for verbose imports.
    * Compile-time check of classes marked with `Implementation`, requiring a no-argument constructor.

- 1.2.1
    * When a class marked with `Implementation` implements multiple interfaces, it now ignores interfaces not marked with `Discoverable`.

- 1.2.0
    * Add configuration options for `Discovery` to configure how implementation classes are processed.
    * Defaults to `Scan` mode, scanning globally. Can be configured to `Mark` mode, requiring the use of the `Implementation` annotation to mark implementation classes, skipping the scanning process to save compilation time.

- 1.1.0
    * Add an `APT` implementation to the annotation processor, making it compatible with Java projects, along with `KSP`.

- 1.0.0
    * Official release, implemented primarily with `KSP` and `AGP`.
-------
