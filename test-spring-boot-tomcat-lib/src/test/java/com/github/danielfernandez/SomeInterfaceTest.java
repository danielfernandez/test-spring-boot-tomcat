package com.github.danielfernandez;

import com.github.danielfernandez.testspringboottomcat.lib.SomeInterfaceClassClassLoaderUtils;
import com.github.danielfernandez.testspringboottomcat.lib.SomeInterfaceThreadContextClassLoaderUtils;
import org.junit.Test;

public class SomeInterfaceTest {

	@Test
	public void testThreadContextClassLoader() {
		System.out.printf(SomeInterfaceThreadContextClassLoaderUtils.outputSomething());
	}

	@Test
	public void testClassClassLoader() {
		System.out.printf(SomeInterfaceClassClassLoaderUtils.outputSomething());
	}

}
