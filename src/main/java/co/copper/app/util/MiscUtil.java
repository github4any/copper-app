package co.copper.app.util;

public final class MiscUtil {

	private MiscUtil() {
	}

	public static void await(final Object monitor, final long timeoutInMilles) {
		synchronized (monitor) {
			try {
				monitor.wait(timeoutInMilles);
			} catch (final InterruptedException ok) {
			}
		}
	}
}
