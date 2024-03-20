package com.router;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

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
	// Similar to MessageValidationHandler, but with routing logic

	@Override
	public void setNext(Handler handler) {
		this.next = handler;
	}

	@Override
	public void handle(Socket socket, String message) {
		// Implement routing logic here
		System.out.println("Received message from broker(" + socket.getPort() + "): " + message);

		next.handle(socket, message);
	}
}

class MessageForwardingHandler implements Handler {
	private Handler next;
	// Similar to MessageValidationHandler, but with message forwarding logic

	@Override
	public void setNext(Handler handler) {
		this.next = handler;
	}

	@Override
	public void handle(Socket socket, String message) {
		// Implement message forwarding logic here

		System.out.println("Forwarding message to market: " + message);
	}
}

public class Router {
	private static final int BROKER_PORT = 5000;
	private static final int MARKET_PORT = 5001;
	private static HashMap<Integer, Socket> routingTable = new HashMap<>();

	public void start() {
		startBrokerListener();
		//startMarketListener();
	}

	private void startBrokerListener() {
		try (ServerSocket serverSocket = new ServerSocket(BROKER_PORT)) {
			System.out.println("waiting brokers...");

			Handler handler = new MessageValidationHandler();
			Handler routingHandler = new RoutingHandler();
			Handler forwardingHandler = new MessageForwardingHandler();

			handler.setNext(routingHandler);
			routingHandler.setNext(forwardingHandler);

			while (true) {
				Socket socket = serverSocket.accept();

				CompletableFuture.runAsync(() -> {
					try {
						int uniqueId = socket.getPort();
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						routingTable.put(uniqueId, socket);
						out.println(uniqueId);

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
		// Listen for connections from markets on port 5001
		// Assign unique IDs and communicate them back to markets
		// Handle incoming messages from markets
		// Validate messages and forward them to the appropriate destination

		System.out.println("ROUTER: waiting markets...");
	}

	// Other methods for managing routing table, message validation, and message forwarding
}
