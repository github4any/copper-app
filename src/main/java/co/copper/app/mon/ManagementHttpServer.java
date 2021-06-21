package co.copper.app.mon;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.RoutingHandler;

public class ManagementHttpServer implements Closeable {

	public enum RequestMethod {
		GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;
	}

	public static class HandlerDef {

		private final RequestMethod method;
		private final String path;
		private final BiFunction<Map<String, Deque<String>>, Optional<String>, String> callback;

		public HandlerDef(final RequestMethod method, final String path,
				final BiFunction<Map<String, Deque<String>>, Optional<String>, String> callback) {
			this.method = method;
			this.path = path;
			this.callback = callback;
		}

		public RequestMethod getMethod() {
			return method;
		}

		public String getPath() {
			return path;
		}

		public BiFunction<Map<String, Deque<String>>, Optional<String>, String> getCallback() {
			return callback;
		}

		@Override
		public String toString() {
			return "[" + "method=" + method + ", path=" + path + ", callback=" + callback + "]";
		}
	}

	public static final String HTTP_RESPONSE_OK = "ok";
	public static final String HTTP_RESPONSE_ERR = "err";
	public static final String HTTP_RESPONSE_TRUE = Boolean.TRUE.toString();
	public static final String HTTP_RESPONSE_FALSE = Boolean.FALSE.toString();

	private static final Logger LOG = LoggerFactory.getLogger(ManagementHttpServer.class);
	private static final String HOST = "0.0.0.0";

	private final Undertow server;

	public ManagementHttpServer(final int port, final HandlerDef... handlerDefs) {
		if (port == 0) {
			server = null;
			return;
		}

		Undertow s = null;
		try {
			LOG.info("Starting management server...");
			s = Undertow.builder().addHttpListener(port, HOST, buildHandler(handlerDefs)).setIoThreads(1)
					.setWorkerThreads(1).build();
			s.start();
			LOG.info("Started");
		} catch (final Exception ex) {
			LOG.warn("Could not start", ex);
		}
		server = s;
	}

	private RoutingHandler buildHandler(final HandlerDef[] defs) {
		final RoutingHandler h = new RoutingHandler();
		h.setInvalidMethodHandler(e -> {
			final Sender responseSender = e.getResponseSender();
			responseSender.send("Invalid method");
			responseSender.close();
		});

		h.setFallbackHandler(e -> {
			final Sender responseSender = e.getResponseSender();
			responseSender.send("Resource not found");
			responseSender.close();
		});

		for (HandlerDef d : defs) {
			final RequestMethod method = d.getMethod();
			if (method == RequestMethod.GET) {
				h.get(d.getPath(), e -> {
					e.setStatusCode(HTTP_OK);
					final String resp = d.getCallback().apply(e.getQueryParameters(), Optional.of(e.getRequestPath()));
					final Sender responseSender = e.getResponseSender();
					responseSender.send(resp);
					responseSender.close();
				});
			} else if (method == RequestMethod.POST) {
				h.post(d.getPath(), e -> e.dispatch(() -> {
					try {
						final Optional<String> req;
						e.startBlocking();
						e.setStatusCode(HTTP_OK);
						final InputStream is = e.getInputStream();
						final int available = is.available();
						if (available > 0) {
							final byte[] b = new byte[available];
							is.read(b);
							req = Optional.of(new String(b, StandardCharsets.UTF_8));
						} else {
							req = Optional.empty();
						}

						// the lock prevents closing before sending the reply
						synchronized (ManagementHttpServer.this) {
							final Sender responseSender = e.getResponseSender();
							responseSender.send(d.getCallback().apply(e.getQueryParameters(), req));
							responseSender.close();
						}
					} catch (final IOException ex) {
						e.setStatusCode(HTTP_INTERNAL_ERROR);
					}
				}));
			} else {
				throw new IllegalArgumentException(String.format("Method '%s' not supported", method));
			}
		}
		return h;
	}

	@Override
	public synchronized void close() {
		if (server != null) {
			server.stop();
		}
	}
}