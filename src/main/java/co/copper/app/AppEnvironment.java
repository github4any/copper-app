package co.copper.app;

import static co.copper.app.Environment.getEnv;

public class AppEnvironment implements Environment {

	private static final String MANAGEMENT_HTTP_PORT = "APP_MANAGEMENT_HTTP_PORT";

	private static final int DEFAULT_MANAGEMENT_HTTP_PORT = 8000;

	private final int managementHttpPort;

	public AppEnvironment() {
		try {
			managementHttpPort = getEnv(MANAGEMENT_HTTP_PORT, Integer::parseInt, DEFAULT_MANAGEMENT_HTTP_PORT);
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getName() {
		return "Application";
	}

	public int getManagementHttpPort() {
		return managementHttpPort;
	}

	@Override
	public String toString() {
		return "AppEnvironment [" + "managementHttpPort=" + managementHttpPort + "]";
	}
}
