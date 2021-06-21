package co.copper.app;

import java.lang.reflect.Field;
import java.util.function.Function;

public interface Environment {

	static String getEnv(final String key, final String defaultValue) {
		final String v = System.getenv(key);
		return v != null ? v : defaultValue;
	}

	static <T> T getEnv(final String key, final Function<String, T> converter, final T defaultValue) {
		final String v = System.getenv(key);
		return v != null ? converter.apply(v) : defaultValue;
	}

	String getName();

	@SuppressWarnings("unchecked")
	default <T> T verify() {
		final Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			try {
				if (f.get(this) == null) {
					throw new IllegalStateException(
							String.format("Field %s must not be null in %s environment", f.getName(), getName()));
				}
			} catch (final IllegalArgumentException | IllegalAccessException ok) {
			}
		}
		return (T) this;
	}

}
