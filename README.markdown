Test repository for SPR-14932
-----------------------------

**Link**: [SPR-14932](https://jira.spring.io/browse/SPR-14932)  

**Title**: Class loading issues due to thread context classloader hierarchy (Spring Web Reactive + Tomcat)

**Tag**: Use tag `SPR-14932`

## Scenario

The scenario tested in this repository is the following:

   * A web application uses a library.
   * That library needs to dynamically load a class that implements a specific interface, for example, in 
     order to select the best implementation for the specific environment in which it is being run.

## Repository Contents

This repository consists of:

   * A library (`-lib`) including two classes called `SomeIntefaceClassClassLoaderUtils` and
     `SomeInterfaceThreadContextClassLoaderUtils`, which upon initialization (static block) try to
     dynamically load and instance an implementation of an interface called `SomeInterface`. The former
     utility class uses `SomeInterface.class.getClassLoader()` to obtain the class loader to be used 
     for this, and the latter uses `Thread.currentThread().getContextClassLoader()`.
   * Four example web applications that provide two URLs: `/class` and `/threadcontext`, which respectively
     call the two `*Utils` classes in the library.
       * **[142mvc]** A Spring Boot `1.4.2.RELEASE` webapp using Spring MVC `4.3.3.RELEASE` on Tomcat.
       * **[200mvc]** A Spring Boot `2.0.0.BUILD-SNAPSHOT` webapp using Spring Web MVC `5.0.0.BUILD-SNAPSHOT` on Tomcat.
       * **[200reactive-tomcat]** A Spring Boot `2.0.0.BUILD-SNAPSHOT` webapp using Spring Web Reactive `5.0.0.BUILD-SNAPSHOT` on Tomcat.
       * **[200reactive-netty]** A Spring Boot `2.0.0.BUILD-SNAPSHOT` webapp using Spring Web Reactive `5.0.0.BUILD-SNAPSHOT` on Netty.

All Spring Boot applications were generated using [http://start.spring.io](http://start.spring.io), with minor changes to
their `pom.xml` to add the library dependency and/or enable or disable Tomcat/Netty.

## Observed Results

The observed results are:

   * Web applications [142mvc], [200mvc] and [200reactive-netty] work OK, and are able to call the library and
     let it load the required interface implementation without issues.
   * **Web application [200reactive-tomcat] fails** when using the *thread context* class loader, throwing a
     `ClassNotFoundException` on the desired interface implementation class. When using the *class* class loader it works OK.

This is the detail of what is happening at the [200reactive-tomcat] application:

   * The *thread-context* class loader is an `org.apache.catalina.loader.ParallelWebappClassLoader` instance 
     with no class path.
   * The *class* class loader is an `org.springframework.boot.loader.LaunchedURLClassLoader` instance with a
     class path including all the inner `.jar` files inside the Spring Boot *über jar*.
   * The `ParallelWebappClassLoader` **does not delegate** to the `LaunchedURLClassLoader`, but instead
     directly delegates to the `sun.misc.Launcher$AppClassLoader` that the `LaunchedURLClassLoader` delegates too.

The above situation is what causes the `ClassNotFoundException` when executing [200reactive-tomcat] and the
*thread-context* class loader is used to load the desired class at the library.

## Class Loader Hierarchy Comparison

This is the *thread-context* class loader hierarchy for [200mvc] — **OK**:

```
+-> sun.misc.Launcher$ExtClassLoader - [sun.misc.Launcher$ExtClassLoader@198ff037]
    +-> sun.misc.Launcher$AppClassLoader - [sun.misc.Launcher$AppClassLoader@330bedb4]
        +-> org.springframework.boot.loader.LaunchedURLClassLoader - [org.springframework.boot.loader.LaunchedURLClassLoader@65ab7765]
            +-> org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedWebappClassLoader - [TomcatEmbeddedWebappClassLoader\n\n  context: ROOT\n\n  delegate: true\n\n----------> Parent Classloader:\n\norg.springframework.boot.loader.LaunchedURLClassLoader@65ab7765\n\n]
```

This is the *thread-context* class loader hierarchy for [200reactive-netty] — **OK**:

```
+-> sun.misc.Launcher$ExtClassLoader - [sun.misc.Launcher$ExtClassLoader@67449f2]
    +-> sun.misc.Launcher$AppClassLoader - [sun.misc.Launcher$AppClassLoader@330bedb4]
        +-> org.springframework.boot.loader.LaunchedURLClassLoader - [org.springframework.boot.loader.LaunchedURLClassLoader@65ab7765]
```

This is the *thread-context* class loader hierarchy for [200reactive-tomcat] — **FAIL**:

```
+-> sun.misc.Launcher$ExtClassLoader - [sun.misc.Launcher$ExtClassLoader@32e33c96]
    +-> sun.misc.Launcher$AppClassLoader - [sun.misc.Launcher$AppClassLoader@330bedb4]
        +-> org.apache.catalina.loader.ParallelWebappClassLoader - [ParallelWebappClassLoader\n\n  context: ROOT\n\n  delegate: false\n\n----------> Parent Classloader:\n\nsun.misc.Launcher$AppClassLoader@330bedb4\n\n]
```


## Possible Diagnosis

The `org.apache.catalina.loader.ParallelWebappClassLoader` being used as thread context class loader in [reactive200-tomcat]
**should delegate to `org.springframework.boot.loader.LaunchedURLClassLoader`, but it doesn't**.


## How to test

First, the library has to be compiled, packaged and put into the local maven repository:

```
cd test-spring-boot-tomcat-lib
mvn clean compile package install
```

Then, in order to run any of the web applications, the Spring Boot applications have to be packaged and then
executed. **Running from an IDE such as IntelliJ or Eclipse does not correctly test the scenario** given the
different class path organizations applied by the IDEs.

```
mvn -U clean compile package
cd target/
java -jar test-spring-boot-[app].jar
```

Then a browser can be used to access `/threadcontext` or `/class` and see the results. The web applications
will output debug information about the class loaders being used through the log.

By default, when accessing `/threadcontext` or `/class` only the hierarchy of class loaders will be output through
the log. In order to see the full class path for each class loader in the hierarchy (very verbose), use:

```
mvn -U clean compile package
cd target/
java -Dlogging.level.classloaderlog=TRACE -jar test-spring-boot-[app].jar
```
