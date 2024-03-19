package com.router;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class Router {
	private static final int BROKER_PORT = 5000;
	private static final int MARKET_PORT = 5001;
	private static HashMap<Integer, Socket> routingTable = new HashMap<>();

	public void start() {
		startBrokerListener();
		//startMarketListener();
	}

	private void startBrokerListener() {
		// !!! TODO: Respect the Chain of Responsibility pattern by refactoring this method

		try (ServerSocket serverSocket = new ServerSocket(BROKER_PORT)) {
			System.out.println("waiting brokers...");

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
								System.out.println("Received message from broker(" + uniqueId + "): " + message);
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
