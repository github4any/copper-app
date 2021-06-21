package co.copper.app;

import static co.copper.app.util.UnsafeUtil.registerSignalHandler;
import static co.copper.app.mon.ManagementHttpServer.HTTP_RESPONSE_ERR;
import static co.copper.app.mon.ManagementHttpServer.HTTP_RESPONSE_OK;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.copper.app.entity.address.Transaction;

import co.copper.app.mon.ManagementHttpServer;
import co.copper.app.mon.ManagementHttpServer.HandlerDef;
import co.copper.app.mon.ManagementHttpServer.RequestMethod;

public class App {

	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	private static final AppEnvironment ENV;
	private static final CountDownLatch LATCH = new CountDownLatch(1);
	private static volatile int exitStatus;

	static {
		LogManager.getLogManager().reset();
		ENV = new AppEnvironment().verify();
		LOG.debug("Using {}", ENV);
	}

	private static final BlockExplorer blockExplorer = new BlockExplorer();

	private static String balancePostHandler(final Map<String, Deque<String>> queryParams, final Optional<String> arg) {
		try {
			if (!arg.isPresent()) {
				return "ERR";
			}

			final String address = arg.get();
			LOG.info("Address: {}", address);

			Long balance = blockExplorer.getAddress(address).getFinalBalance();
			return balance.toString();
		} catch (final Exception ex) {
			LOG.error("Unexpected balance error", ex);
			return "ERR";
		}

	}

	private static String transactionPostHandler(final Map<String, Deque<String>> queryParams,
			final Optional<String> arg) {
		try {
			if (!arg.isPresent()) {
				return HTTP_RESPONSE_ERR;
			}

			final String address = arg.get();
			LOG.info("Address: {}", address);
			List<Transaction> transactions = new ArrayList<Transaction>();
			transactions = blockExplorer.getAddress(address).getTransactions();
			String result = transactions.stream().map(n -> String.valueOf(n))
					.collect(Collectors.joining("||", "{", "}"));

			return result;

		} catch (

		final Exception ex) {
			LOG.error("Unexpected transaction error", ex);
			return HTTP_RESPONSE_ERR;
		}

	}

	private static String paymentGetHandler(final Map<String, Deque<String>> queryParams, final Optional<String> arg) {
		try {
			String api_code = "";
			String identifier = "";
			String password = "";
			String toAddress = "";
			String amount = "";
			String fee = "";

			Deque<String> parameter1 = queryParams.get("api_code");
			if (parameter1 != null) {
				api_code = parameter1.getFirst();
			}

			Deque<String> parameter2 = queryParams.get("identifier");
			if (parameter2 != null) {
				identifier = parameter2.getFirst();
			}

			Deque<String> parameter3 = queryParams.get("password");
			if (parameter2 != null) {
				password = parameter3.getFirst();
			}

			Deque<String> parameter4 = queryParams.get("toAddress");
			if (parameter4 != null) {
				toAddress = parameter4.getFirst();
			}

			Deque<String> parameter5 = queryParams.get("amount");
			if (parameter5 != null) {
				amount = parameter5.getFirst();
			}

			Deque<String> parameter6 = queryParams.get("fee");
			if (parameter6 != null) {
				fee = parameter6.getFirst();
			}

			Wallet wallet = new Wallet("http://localhost:3000/", api_code, identifier, password);
			wallet.send(toAddress, Long.parseLong(amount), null, Long.parseLong(fee));

			return HTTP_RESPONSE_OK;
		} catch (final Exception ex) {
			LOG.error("Unexpected payment error", ex);
			return HTTP_RESPONSE_ERR;
		}
	}

	private static String healthGetHandler(final Map<String, Deque<String>> queryParams, final Optional<String> arg) {
		return HTTP_RESPONSE_OK;
	}

	private static String shutdownPostHandler(final Map<String, Deque<String>> queryParams,
			final Optional<String> arg) {
		try {
			final int retCode = Integer.parseInt(arg.orElse("0"));
			shutdown(retCode);
			return HTTP_RESPONSE_OK;
		} catch (final NumberFormatException ex) {
			return HTTP_RESPONSE_ERR;
		}
	}

	public static void main(final String[] args) {
		LOG.info("Starting...");

		final App app = new App();
		try (ManagementHttpServer server = new ManagementHttpServer(ENV.getManagementHttpPort(),
				managementHandlers(app));) {
			registerSignalHandlers();

			LOG.info("Started");
			LATCH.await();
			LOG.warn("Stopping...");
		} catch (final Throwable ex) {
			LOG.error("Initialization error", ex);
			exitStatus = 1;
		}
		LOG.warn("Stopped");
		System.exit(exitStatus);
	}

	private static void registerSignalHandlers() {
		registerSignalHandler("TERM", () -> {
			LOG.warn("SIGTERM");
			shutdown(0);
		});

		registerSignalHandler("ABRT", () -> {
			LOG.warn("SIGABRT");
			shutdown(1);
		});
	}

	private static void shutdown(final int status) {
		LOG.warn("Shutdown {}", status);
		exitStatus = status;
		LATCH.countDown();
	}

	private static HandlerDef[] managementHandlers(final App app) {
		return new HandlerDef[] { new HandlerDef(RequestMethod.GET, "/health", App::healthGetHandler),
				new HandlerDef(RequestMethod.POST, "/transaction", App::transactionPostHandler),
				new HandlerDef(RequestMethod.POST, "/balance", App::balancePostHandler),
				new HandlerDef(RequestMethod.GET, "/payment", App::paymentGetHandler),
				new HandlerDef(RequestMethod.POST, "/shutdown", App::shutdownPostHandler) };
	}
}
