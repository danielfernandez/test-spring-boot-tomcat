package com.github.danielfernandez.testspringboottomcat.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SomeInterfaceThreadContextClassLoaderUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("classloaderlog");

    private static final String IMPLEMENTATION_CLASS =
            "com.github.danielfernandez.testspringboottomcat.lib.SomeImplementation";

    private static final SomeInterface impl;


    /*
     * When this class is loaded we will initialize the 'impl' static variable to the specific
     * implementation we want to use (which in this case for testing purposes is always the same.
     */
    static {

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("*** Will perform class resolution using the THREAD CONTEXT CLASS LOADER ***");
            // This will output the entire class loader hierarchy through the log
            ClassLoaderLoggingUtils.reportClassLoaderInfo(LOGGER, classLoader);
        }

        try {

            final Class<?> implClass = Class.forName(IMPLEMENTATION_CLASS, true, classLoader);
            impl = (SomeInterface) implClass.newInstance();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format(
                        "Class %s correctly loaded using class loader: [%s]",
                        IMPLEMENTATION_CLASS, ClassLoaderLoggingUtils.normalizeClassLoaderValue(implClass.getClassLoader())));
            }

        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) { // Trace will do, as we are sending the (wrapped) exception upwards
                LOGGER.debug(String.format("An error happened trying to load class %s", IMPLEMENTATION_CLASS), e);
            }
            throw new ExceptionInInitializerError(e);
        }

    }


    public static String outputSomething() {
        return impl.outputSomething();
    }


    // Utils class, private constructor
    private SomeInterfaceThreadContextClassLoaderUtils() {
        super();
    }

}
