package org.deking.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public final class PropertiesUtilsTest {

	private static final String TEST_CONFIG_PATH = "config/application.properties";

	@Test
	public void shouldLoadPropertiesFromClasspathWhenClassLoaderIsNull() {
		PropertiesUtils properties =
				new PropertiesUtils(null, TEST_CONFIG_PATH);

		Assert.assertEquals("tom", properties.getProperty("name"));
	}

	@Test
	public void shouldLoadPropertiesFromExplicitClassLoader() {
		PropertiesUtils properties =
				new PropertiesUtils(
						PropertiesUtilsTest.class.getClassLoader(),
						TEST_CONFIG_PATH);

		Assert.assertEquals("tom", properties.getProperty("name"));
	}

	@Test
	public void shouldReturnEmptyPropertiesWhenFileDoesNotExist() {
		PropertiesUtils properties =
				new PropertiesUtils(
						null,
						"non/existent/path/config.properties");

		Assert.assertTrue(properties.isEmpty());
		Assert.assertNull(properties.getProperty("name"));
	}

	@Test
	public void shouldFallbackToUserDirWhenClasspathResourceNotFound()
			throws IOException {

		String originalUserDir = System.getProperty("user.dir");

		File tempDir = Files.createTempDirectory("props-test").toFile();
		File configFile = new File(tempDir, "userdir.properties");

		try {
			Properties props = new Properties();
			props.setProperty("key", "userdir-value");

			try (FileOutputStream out =
					new FileOutputStream(configFile)) {
				props.store(out, null);
			}

			System.setProperty("user.dir", tempDir.getAbsolutePath());

			PropertiesUtils properties =
					new PropertiesUtils(null, "userdir.properties");

			Assert.assertEquals(
					"userdir-value",
					properties.getProperty("key"));

		} finally {
			System.setProperty("user.dir", originalUserDir);
			configFile.delete();
			tempDir.delete();
		}
	}

	@Test
	public void shouldFallbackToUserHomeWhenUserDirResourceNotFound()
			throws IOException {

		String originalUserHome = System.getProperty("user.home");

		File tempDir = Files.createTempDirectory("props-home-test").toFile();
		File configFile = new File(tempDir, "userhome.properties");

		try {
			Properties props = new Properties();
			props.setProperty("key", "userhome-value");

			try (FileOutputStream out =
					new FileOutputStream(configFile)) {
				props.store(out, null);
			}

			System.setProperty("user.home", tempDir.getAbsolutePath());

			PropertiesUtils properties =
					new PropertiesUtils(null, "userhome.properties");

			Assert.assertEquals(
					"userhome-value",
					properties.getProperty("key"));

		} finally {
			System.setProperty("user.home", originalUserHome);
			configFile.delete();
			tempDir.delete();
		}
	}

	@Test
	public void shouldPreferExplicitClassLoaderOverFallbackLocations()
			throws IOException {

		String originalUserDir = System.getProperty("user.dir");

		File tempDir = Files.createTempDirectory("props-priority").toFile();
		File configFile = new File(tempDir, TEST_CONFIG_PATH);

		try {
			configFile.getParentFile().mkdirs();

			Files.write(
					configFile.toPath(),
					"name=wrong-value".getBytes(StandardCharsets.UTF_8));

			System.setProperty("user.dir", tempDir.getAbsolutePath());

			PropertiesUtils properties =
					new PropertiesUtils(
							PropertiesUtilsTest.class.getClassLoader(),
							TEST_CONFIG_PATH);

			Assert.assertEquals("tom", properties.getProperty("name"));

		} finally {
			System.setProperty("user.dir", originalUserDir);
			configFile.delete();
			configFile.getParentFile().delete();
			tempDir.delete();
		}
	}

	@Test
	public void shouldLoadEmptyPropertiesFile()
			throws IOException {

		String originalUserDir = System.getProperty("user.dir");

		File tempDir = Files.createTempDirectory("props-empty").toFile();
		File configFile = new File(tempDir, "empty.properties");

		try {
			configFile.createNewFile();

			System.setProperty("user.dir", tempDir.getAbsolutePath());

			PropertiesUtils properties =
					new PropertiesUtils(null, "empty.properties");

			Assert.assertTrue(properties.isEmpty());

		} finally {
			System.setProperty("user.dir", originalUserDir);
			configFile.delete();
			tempDir.delete();
		}
	}

	@Test
	public void shouldCloseInputStreamAfterLoading()
			throws IOException {

		String originalUserDir = System.getProperty("user.dir");

		File tempDir = Files.createTempDirectory("props-close").toFile();
		File configFile = new File(tempDir, "close-test.properties");

		try {
			Files.write(
					configFile.toPath(),
					"name=test".getBytes(StandardCharsets.UTF_8));

			System.setProperty("user.dir", tempDir.getAbsolutePath());

			new PropertiesUtils(null, "close-test.properties");

			// Windows 下如果流没关闭，这里可能删除失败
			Assert.assertTrue(configFile.delete());

		} finally {
			System.setProperty("user.dir", originalUserDir);

			if (configFile.exists()) {
				configFile.delete();
			}

			tempDir.delete();
		}
	}
}