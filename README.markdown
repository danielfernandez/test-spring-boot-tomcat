Test repository for XXXXXX
--------------------------

  **Link**: [XXX](...)
  **Description**: Class loading issues due to thread context classloader (Spring Web Reactive + Tomcat)

## Scenario

  The scenario tested in this repository is the following:

  * A web application uses a library.
  * That library needs to dynamically load a class that implements a specific interface.

  This repository consists of:

  * A library (`-lib`) including two classes called `SomeIntefaceClassClassLoaderUtils` and
    `SomeInterfaceThreadContextClassLoaderUtils`, which upon initialization (static block) try to
    dynamically load an instance an implementation of an interface called `SomeInterface`. The former
    uses `SomeInterface.class.getClassLoader()` to obtain the class loader to be used for this, and
    the latter uses `Thread.currentThread().getContextClassLoader()`.
  * Four example web applications that provide two URLs: `/class` and `/threadcontext`, which respectively
    call the two `*Utils` classes in the library.
      * [142mvc] A Spring Boot `1.4.2.RELEASE` webapp using Spring MVC `4.3.3.RELEASE` on Tomcat.
      * [200mvc] A Spring Boot `2.0.0.BUILD-SNAPSHOT` webapp using Spring Web MVC `5.0.0.BUILD-SNAPSHOT` on Tomcat.
      * [200reactive-tomcat] A Spring Boot `2.0.0.BUILD-SNAPSHOT` webapp using Spring Web Reactive `5.0.0.BUILD-SNAPSHOT` on Tomcat.
      * [200reactive-netty] A Spring Boot `2.0.0.BUILD-SNAPSHOT` webapp using Spring Web Reactive `5.0.0.BUILD-SNAPSHOT` on Netty.

  All Spring Boot applications were generated using http://start.spring.io, with minor changes to
  their `pom.xml`s to add the library dependency and/or enable or disable Tomcat/Netty.

  The observed results are:

  * Web applications [142mvc], [200mvc] and [200reactive-netty] work OK, and are able to call the library and
    let it load the required interface implementation without issues.
  * **Web application [200reactive-tomcat] fails** when using the thread context class loader, throwing a
    `ClassNotFoundException` on the desired interface implementation class. When using the class class loader it works OK.

  This is the detail of what is happening at the [200reactive-tomcat] application:

  * The thread context class loader is not set to the same
    `org.springframework.boot.loader.LaunchedURLClassLoader` it is when using Netty in [200reactive-netty].
  * Instead, an `org.apache.catalina.loader.ParallelWebappClassLoader` class loader is set as the thread context
    class loader.
  * The `LaunchedURLClassLoader` class loader is the one obtained by `SomeInterface.class.getClassLoader()`.
  * The `LaunchedURLClassLoader` class loader is the one which class path contains all the inner `.jar` files inside
    the Spring Boot über jar so it is able to find the requested implementation class.
  * The `ParallelWebappClassLoader` class loader has no class path.
  * The `ParallelWebappClassLoader` class loader does not delegate on the `LaunchedURLClassLoader`, but instead
    directly delegates on the `sun.misc.Launcher$AppClassLoader` that the `LaunchedURLClassLoader` delegates too.

  The above situation is what causes the `ClassNotFoundException` when executing [200reactive-tomcat].

  **Possible bug diagnostic**: The `ParallelWebappClassLoader` being used as thread context class loader in [reactive200-tomcat]
  should delegate on `org.springframework.boot.loader.LaunchedURLClassLoader`, but it doesn't.

## How to test

  First, the library has to be compiled, packaged and put into the local maven repository:

  ```
  cd test-spring-boot-tomcat-lib
  mvn clean compile install
  ```

  Then, in order to run any of the web applications, the Spring Boot applications have to be packaged and then
  executed. **Running from an IDE such as IntelliJ or Eclipse does not correctly test the scenario** given the
  different class path organizations applied by the IDEs.

  ```
  mvn -U clean compile package
  cd target/
  java -jar test-spring-boot-[app].jar
  ```

  By default, when accessing `/threadcontext` or `/class` only the hierarchy of class loaders will be output through
  the log. In order to see the full class path for each class loader in the hierarchy (very verbose), use:

  ```
  mvn -U clean compile package
  cd target/
  java -Dlogging.level.classloaderlog=TRACE -jar test-spring-boot-[app].jar
  ```
