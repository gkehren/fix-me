package com.router;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Router {
	private static final int BROKER_PORT = 5000;
	private static final int MARKET_PORT = 5001;
	private volatile boolean stopRequested = false;
	private static final AtomicInteger idGenerator = new AtomicInteger(100000);
	private Handler handler;
	private RoutingHandler routingHandler;

	public void start() {
		this.handler = new MessageValidationHandler();
		this.routingHandler = new RoutingHandler();
		Handler forwardingHandler = new MessageForwardingHandler();

		handler.setNext(routingHandler);
		routingHandler.setNext(forwardingHandler);

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

			while (!stopRequested) {
				Socket socket = serverSocket.accept();

				CompletableFuture.runAsync(() -> {
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						int brokerID = Integer.parseInt(in.readLine());
						if (brokerID == -1) {
							int uniqueId = generateUniqueId();
							PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
							out.println(uniqueId);
							RoutingTable.addBrokerRoute(uniqueId, socket);
							System.out.println("New broker connected. Assigned ID: " + uniqueId);
						} else {
							if (RoutingTable.isBrokerRoute(brokerID)) {
								Socket oldSocket = RoutingTable.getRoute(brokerID);
								if (oldSocket != null) {
									sendDisconnection(oldSocket, brokerID, "A new connection has been established");
									oldSocket.close();
								}

								RoutingTable.addBrokerRoute(brokerID, socket);
								System.out.println("Broker reconnected. ID: " + brokerID);

								try {
									PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
									out.println(brokerID);


									List<String> pendingMessages = RoutingTable.getPendingMessages(brokerID);
									if (pendingMessages != null) {
										Thread.sleep(1000);
										for (String message : pendingMessages) {
											System.out.println("Sending pending message to broker(" + brokerID + "): " + message);
											out.println(message);
										}
										RoutingTable.removePendingMessages(brokerID);
									}
								} catch (InterruptedException e) {
									System.out.println("Error while sleeping: " + e.getMessage());
								}
							} else {
								int uniqueId = generateUniqueId();
								PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
								out.println(uniqueId);
								RoutingTable.addBrokerRoute(uniqueId, socket);
								System.out.println("New broker connected. Assigned ID: " + uniqueId);
							}
						}

						String message;
						while ((message = in.readLine()) != null) {
							if (message.length() > 0) {
								this.handler.handle(socket, message);
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

			while (!stopRequested) {
				Socket socket = serverSocket.accept();

				CompletableFuture.runAsync(() -> {
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						int marketID = Integer.parseInt(in.readLine());
						if (marketID == -1) {
							int uniqueId = generateUniqueId();
							PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
							out.println(uniqueId);
							RoutingTable.addMarketRoute(uniqueId, socket);
							System.out.println("New market connected. Assigned ID: " + uniqueId);
						} else {
							if (RoutingTable.isMarketRoute(marketID)) {
								Socket oldSocket = RoutingTable.getRoute(marketID);
								if (oldSocket != null) {
									sendDisconnection(oldSocket, marketID, "A new connection has been established");
									oldSocket.close();
								}
								RoutingTable.addMarketRoute(marketID, socket);
								System.out.println("Market reconnected. ID: " + marketID);

								try {
									PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
									out.println(marketID);

									List<String> pendingMessages = RoutingTable.getPendingMessages(marketID);
									if (pendingMessages != null) {
										Thread.sleep(1000);
										for (String message : pendingMessages) {
											System.out.println("Sending pending message to market(" + marketID + "): " + message);
											out.println(message);
										}
										RoutingTable.removePendingMessages(marketID);
									}
								} catch (InterruptedException e) {
									System.out.println("Error while sleeping: " + e.getMessage());
								}
							} else {
								int uniqueId = generateUniqueId();
								PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
								out.println(uniqueId);
								RoutingTable.addMarketRoute(uniqueId, socket);
								System.out.println("New market connected. Assigned ID: " + uniqueId);
							}
						}

						String message;
						while ((message = in.readLine()) != null) {
							if (message.length() > 0) {
								this.handler.handle(socket, message);
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

	public static void sendRejection(Socket socket, String message, String reason) {
		try {
			String[] parts = message.split("\u0001");
			Map<String, String> fields = new HashMap<>();
			for (String part : parts) {
				String[] keyValue = part.split("=");
				if (keyValue.length == 2)
					fields.put(keyValue[0], keyValue[1]);
			}

			String body = "35=3" + "\u0001" + // MsgType = Reject
						  "49=" + fields.get("56") + "\u0001" + // SenderCompID
						  "56=" + fields.get("49") + "\u0001" + // TargetCompID
						  "45=" + fields.get("11") + "\u0001" + // RefSeqNum
						  "58=" + reason + "\u0001"; // Text

			String fixMessage = "8=FIX.4.4" + "\u0001" + // BeginString
								"9=" + body.length() + "\u0001" + // BodyLength
								body;

			int checksum = 0;
			for (char ch : fixMessage.toCharArray())
				checksum += ch;
			checksum %= 256;
			String checkSumStr = String.format("%03d", checksum);
			fixMessage += "10=" + checkSumStr + "\u0001";

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(fixMessage);
		} catch (IOException e) {
			System.out.println("Error sending rejection: " + e.getMessage());
		}
	}

	public static void sendDisconnection(Socket socket, int id, String reason) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			String body = "35=5" + "\u0001" + // MsgType = Logout
						  "49=" + 0 + "\u0001" + // SenderCompID
						  "56=" + id + "\u0001" + // TargetCompID
						  "58=" + reason + "\u0001"; // Text

			String fixMessage = "8=FIX.4.4\u0001" + // BeginString
								"9=" + body.length() + "\u0001" + // BodyLength
								body;

			int checksum = 0;
			for (char ch : fixMessage.toCharArray())
				checksum += ch;
			checksum %= 256;
			String checkSumStr = String.format("%03d", checksum);
			fixMessage += "10=" + checkSumStr + "\u0001";

			out.println(fixMessage);
		} catch (IOException e) {
			System.out.println("Error sending execution confirmation: " + e.getMessage());
		}
	}
}
