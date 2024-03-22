package com.router;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

interface Handler {
	void setNext(Handler handler);
	void handle(Socket socket, String message);
}

class MessageValidationHandler implements Handler {
	private Handler next;

	@Override
	public void setNext(Handler handler) {
		this.next = handler;
	}

	@Override
	public void handle(Socket socket, String message) {
		// Validate the message based on the checksum
		// If the message is valid and there is a next handler, pass the request to the next handler
		if (isValid(message) && next != null) {
			next.handle(socket, message);
		}
	}

	private boolean isValid(String message) {
		// Implement your message validation logic here
		return true;
	}
}

class RoutingHandler implements Handler {
	private Handler next;
	private HashMap<Integer, Socket> routingTable;

	public RoutingHandler() {
		this.routingTable = new HashMap<>();
	}

	@Override
	public void setNext(Handler handler) {
		this.next = handler;
	}

	@Override
	public void handle(Socket socket, String message) {
		System.out.println("Received message from broker(" + socket.getPort() + "): " + message);

		int destinationId = parseDestinationId(message);
		Socket destinationSocket = routingTable.get(destinationId);

		if (destinationSocket != null) {
			if (next != null) {
				next.handle(destinationSocket, message);
			}
		} else {
			System.out.println("Destination not found for message: " + message);
		}
	}

	private int parseDestinationId(String message) {
		// Extract the destination ID from the message
		return 0;

	}
}

class MessageForwardingHandler implements Handler {

	@Override
	public void setNext(Handler handler) {
		// No next handler
	}

	@Override
	public void handle(Socket socket, String message) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
			System.out.println("Forwarding message to market: " + message);
		} catch (IOException e) {
			System.out.println("Error forwarding message: " + e.getMessage());
		}
	}
}

public class Router {
	private static final int BROKER_PORT = 5000;
	private static final int MARKET_PORT = 5001;
	private volatile boolean stopRequested = false;
	private static final AtomicInteger idGenerator = new AtomicInteger(100000);

	public void start() {
		Thread brokerThread = new Thread(this::startBrokerListener);
		Thread marketThread = new Thread(this::startMarketListener);

		brokerThread.start();
		marketThread.start();
	}

	public void stop() {
		stopRequested = true;
	}

	private int generateUniqueId() {
		return idGenerator.incrementAndGet();
	}

	private void startBrokerListener() {
		try (ServerSocket serverSocket = new ServerSocket(BROKER_PORT)) {
			System.out.println("waiting brokers...");

			Handler handler = new MessageValidationHandler();
			Handler routingHandler = new RoutingHandler();
			Handler forwardingHandler = new MessageForwardingHandler();

			handler.setNext(routingHandler);
			routingHandler.setNext(forwardingHandler);

			while (!stopRequested) {
				Socket socket = serverSocket.accept();

				CompletableFuture.runAsync(() -> {
					try {
						int uniqueId = generateUniqueId();
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						out.println(uniqueId);
						System.out.println("New broker connected. Assigned ID: " + uniqueId);

						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String message;
						while ((message = in.readLine()) != null) {
							if (message.length() > 0) {
								handler.handle(socket, message);
							}
						}
					} catch (IOException e) {
						System.out.println("Error in the router: " + e.getMessage());
					} finally {
						try {
							socket.close();
						} catch (IOException e) {
							System.out.println("Error closing socket: " + e.getMessage());
						}
					}
				});
			}
		} catch (IOException e) {
			System.out.println("Error in the router: " + e.getMessage());
		}
	}

	private void startMarketListener() {
		try (ServerSocket serverSocket = new ServerSocket(MARKET_PORT)) {
			System.out.println("waiting markets...");

			Handler handler = new MessageValidationHandler();
			Handler routingHandler = new RoutingHandler();
			Handler forwardingHandler = new MessageForwardingHandler();

			handler.setNext(forwardingHandler);
			routingHandler.setNext(forwardingHandler);

			while (!stopRequested) {
				Socket socket = serverSocket.accept();

				CompletableFuture.runAsync(() -> {
					try {
						int uniqueId = generateUniqueId();
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						out.println(uniqueId);
						System.out.println("New market connected. Assigned ID: " + uniqueId);

						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String message;
						while ((message = in.readLine()) != null) {
							if (message.length() > 0) {
								handler.handle(socket, message);
							}
						}
					} catch (IOException e) {
						System.out.println("Error in the router: " + e.getMessage());
					} finally {
						try {
							socket.close();
						} catch (IOException e) {
							System.out.println("Error closing socket: " + e.getMessage());
						}
					}
				});
			}
		} catch (IOException e) {
			System.out.println("Error in the router: " + e.getMessage());
		}
	}
}
