package co.copper.app.util;

import java.lang.reflect.Field;

import sun.misc.Signal;
import sun.misc.Unsafe;

public final class UnsafeUtil {

	private UnsafeUtil() {
	}

	/**
	 * Disables "illegal reflective access" warning under Java 9, 10, 11
	 */
	public static void disableJVM11StartupWarnings() {
		try {
			final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			final Unsafe u = (Unsafe) theUnsafe.get(null);
			final Class<?> cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
			u.putObjectVolatile(cls, u.staticFieldOffset(cls.getDeclaredField("logger")), null);
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void registerSignalHandler(final String signal, final Runnable callback) {
		Signal.handle(new Signal(signal), sig -> callback.run());
	}
}
