package com.router;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class RoutingTable {
	private static final HashMap<Integer, Socket> marketRoutingTable = new HashMap<>();
	private static final HashMap<Integer, Socket> brokerRoutingTable = new HashMap<>();
	private static final HashMap<Integer, List<String>> pendingMessages = new HashMap<>();

	public static void addMarketRoute(int id, Socket address) {
		marketRoutingTable.put(id, address);
	}

	public static void addBrokerRoute(int id, Socket address) {
		brokerRoutingTable.put(id, address);
	}

	public static Socket getRoute(int id) {
		if (isBrokerRoute(id)) {
			return brokerRoutingTable.get(id);
		} else if (isMarketRoute(id)) {
			return marketRoutingTable.get(id);
		} else {
			return null;
		}
	}

	public static boolean isBrokerRoute(int id) {
		return brokerRoutingTable.containsKey(id);
	}

	public static boolean isMarketRoute(int id) {
		return marketRoutingTable.containsKey(id);
	}

	public static boolean isBrokerRoute(Socket address) {
		return brokerRoutingTable.containsValue(address);
	}

	public static boolean isMarketRoute(Socket address) {
		return marketRoutingTable.containsValue(address);
	}

	public static void addPendingMessage(int id, String message) {
		if (pendingMessages.containsKey(id)) {
			pendingMessages.get(id).add(message);
		} else {
			pendingMessages.put(id, List.of(message));
		}
	}

	public static List<String> getPendingMessages(int id) {
		return pendingMessages.get(id);
	}

	public static void removePendingMessages(int id) {
		pendingMessages.remove(id);
	}

	public static void close() throws IOException {
		for (Socket socket : marketRoutingTable.values()) {
			socket.close();
		}
		for (Socket socket : brokerRoutingTable.values()) {
			socket.close();
		}
	}
}
