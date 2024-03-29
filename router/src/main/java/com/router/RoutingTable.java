package com.router;

import java.net.Socket;
import java.util.HashMap;

public class RoutingTable {
	private static final HashMap<Integer, Socket> marketRoutingTable = new HashMap<>();
	private static final HashMap<Integer, Socket> brokerRoutingTable = new HashMap<>();

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
}
