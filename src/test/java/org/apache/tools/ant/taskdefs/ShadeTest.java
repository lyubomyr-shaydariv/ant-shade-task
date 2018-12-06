package org.apache.tools.ant.taskdefs;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ShadeTest {

	@Test
	public void testExecute() {
		final Shade unit = new Shade();
		unit.setJar(new File("file.jar"));
		unit.addConfiguredRelocation(unit.createRelocation());
		unit.setUberJar(new File("uber.jar"));
		// This is a silly test, but the shader cannot be mocked/injected,
		// so it assumes the shade() method throws an NPE if the uberJar option does not have a parent directory
		final Throwable ex = Assertions.assertThrows(NullPointerException.class, unit::execute);
		Assertions.assertNull(ex.getMessage());
	}

	@Test
	public void testExecuteMustFailIfNoJarSupplied() {
		final Shade unit = new Shade();
		final Throwable ex = Assertions.assertThrows(BuildException.class, unit::execute);
		Assertions.assertEquals("The jar option requires a non-null value", ex.getMessage());
	}

	@Test
	public void testExecuteMustFailIfNoRelocationsSupplied() {
		final Shade unit = new Shade();
		unit.setJar(new File("file.jar"));
		final Throwable ex = Assertions.assertThrows(BuildException.class, unit::execute);
		Assertions.assertEquals("The relocations option requires a non-null value", ex.getMessage());
	}

	@Test
	public void testExecuteMustFailIfNoUberJarSupplied() {
		final Shade unit = new Shade();
		unit.setJar(new File("file.jar"));
		unit.addConfiguredRelocation(unit.createRelocation());
		final Throwable ex = Assertions.assertThrows(BuildException.class, unit::execute);
		Assertions.assertEquals("The uberJar option requires a non-null value", ex.getMessage());
	}

}
