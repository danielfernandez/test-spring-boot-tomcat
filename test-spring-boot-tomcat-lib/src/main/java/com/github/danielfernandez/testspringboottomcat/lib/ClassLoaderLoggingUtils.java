package com.github.danielfernandez.testspringboottomcat.lib;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;

final class ClassLoaderLoggingUtils {


    static void reportClassLoaderInfo(final Logger logger, final ClassLoader classLoader) {

        ClassLoader cl;

        final List<ClassLoader> clHierarchy = new ArrayList<ClassLoader>();
        cl = classLoader;
        while (cl != null) {
            clHierarchy.add(cl);
            cl = cl.getParent();
        }

        Collections.reverse(clHierarchy);

        final StringBuilder clHierarchyReport = new StringBuilder();
        for (int level = 0; level < clHierarchy.size(); level++) {
            cl = clHierarchy.get(level);
            if (clHierarchyReport.length() > 0) {
                clHierarchyReport.append('\n');
                for (int i = 0; i < level; i++) {
                    clHierarchyReport.append("    ");
                }
            }
            // In DEBUG mode we will only output the hierarchy with no URL class paths (which are much more
            // verbose, and will only be output in TRACE mode).
            if (logger.isTraceEnabled()) {
                clHierarchyReport.append(String.format(
                        "+-> %s - [%s] {%s}", cl.getClass().getName(), normalizeClassLoaderValue(cl), getAvailableClassPathInfo(cl)));
            } else {
                clHierarchyReport.append(String.format(
                        "+-> %s - [%s]", cl.getClass().getName(), normalizeClassLoaderValue(cl)));
            }
        }

        final String logMsg =
                String.format(
                        "Class loader obtained for class resolution is %s [%s]. Full hierarchy follows:\n%s",
                        classLoader.getClass().getName(), normalizeClassLoaderValue(classLoader), clHierarchyReport);
        if (logger.isTraceEnabled()) {
            logger.trace(logMsg);
        } else {
            logger.debug(logMsg);
        }

    }


    static String normalizeClassLoaderValue(final ClassLoader classLoader) {
        final String clValue = classLoader.toString();
        return clValue.replaceAll("[\n|\r]", "\\\\n");
    }


    private static String getAvailableClassPathInfo(final ClassLoader classLoader) {

        if (!(classLoader instanceof URLClassLoader)) {
            return "-";
        }

        final URLClassLoader cl = (URLClassLoader)classLoader;
        return Arrays.toString(cl.getURLs());

    }


    // Utils class, private constructor
    private ClassLoaderLoggingUtils() {
        super();
    }

}
