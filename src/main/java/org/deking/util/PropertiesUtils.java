package org.deking.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public final class PropertiesUtils extends Properties {

	private static final ClassLoader[] CLASSLOADERS = { Thread.currentThread().getContextClassLoader(),
			PropertiesUtils.class.getClassLoader() };

	private PropertiesUtils() {
	}

	public PropertiesUtils(@Nullable ClassLoader classLoader, String configPath) {

		findInputStream(classLoader, configPath).ifPresent(in -> {
			try (InputStream inputStream = in) {
				load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
				throw new UncheckedIOException(e);
			}
		});
	}

	private static Optional<InputStream> findInputStream(@Nullable ClassLoader classLoader, String configPath) {

		if (classLoader != null) {
			InputStream in = classLoader.getResourceAsStream(configPath);
			if (in != null) {
				return Optional.of(in);
			}
		}

		Optional<InputStream> classpathStream = Arrays.stream(CLASSLOADERS).filter(Objects::nonNull)
				.map(loader -> loader.getResourceAsStream(configPath)).filter(Objects::nonNull).findFirst();

		if (classpathStream.isPresent()) {
			return classpathStream;
		}

		Optional<InputStream> userDirStream = openFile(System.getProperty("user.dir"), configPath);

		if (userDirStream.isPresent()) {
			return userDirStream;
		}

		return openFile(System.getProperty("user.home"), configPath);
	}

	private static Optional<InputStream> openFile(String dir, String configPath) {
		if (dir == null) {
			return Optional.empty();
		}

		File file = new File(dir, configPath);

		if (!file.isFile()) {
			return Optional.empty();
		}

		try {
			return Optional.of(new FileInputStream(file));
		} catch (IOException e) {
			return Optional.empty();
		}
	}
}