package com.router;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

interface Handler {
	void setNext(Handler handler);
	void handle(Socket socket, String message);

	default void sendRejection(Socket socket, String message, String reason) {
		Router.sendRejection(socket, message, reason);
	}
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
		} else {
			System.out.println("Invalid checksum: " + message);
			sendRejection(socket, message, "Invalid checksum");
		}
	}

	private boolean isValid(String message) {
		String[] parts = message.split("10=");
		if (parts.length != 2) {
			return false;
		}
		String body = parts[0];
		String checksumStr = parts[1].substring(0, 3);

		int checksum = 0;
		for (char ch : body.toCharArray()) {
			checksum += ch;
		}
		checksum %= 256;

		return String.format("%03d", checksum).equals(checksumStr);
	}
}

class RoutingHandler implements Handler {
	private Handler next;
	private HashMap<Integer, Socket> routingTable;

	public RoutingHandler() {
		this.routingTable = new HashMap<>();
	}

	public void addRoute(int destinationId, Socket destinationSocket) {
		routingTable.put(destinationId, destinationSocket);
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
			sendRejection(socket, message, "Destination not found");
		}
	}

	private int parseDestinationId(String message) {
		// Extract the destination ID from the message
		// The separator is \u0001 (ASCII SOH) character
		// The destination ID is the value of the 56 tag

		String[] parts = message.split("\u0001");
		for (String part : parts) {
			if (part.startsWith("56=")) {
				return Integer.parseInt(part.substring(3));
			}
		}
		return -1;
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
			sendRejection(socket, message, "Market not available");
		}
	}
}
