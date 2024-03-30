package com.router;

import java.io.*;
import java.net.*;

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

	@Override
	public void setNext(Handler handler) {
		this.next = handler;
	}

	@Override
	public void handle(Socket socket, String message) {
		if (message == null || message.isEmpty()) {
			System.out.println("Invalid message received");
			sendRejection(socket, message, "Invalid message");
			return;
		}
		int sourceId = parseSourceId(message);
		int destinationId = parseDestinationId(message);
		if (RoutingTable.isBrokerRoute(socket))
			System.out.println("Received message from broker(" + sourceId + ") to market(" + destinationId + "): " + message);
		else if (RoutingTable.isMarketRoute(socket))
			System.out.println("Received message from market(" + sourceId + ") to broker(" + destinationId + "): " + message);
		else
			System.out.println("Received message from unknown source: " + message);

		if (RoutingTable.isBrokerRoute(socket) && !RoutingTable.isMarketRoute(destinationId)) {
			System.out.println("Broker can send messages only to the market");
			sendRejection(socket, message, "Broker can send messages only to the market");
		} else if (RoutingTable.isMarketRoute(socket) && !RoutingTable.isBrokerRoute(destinationId)) {
			System.out.println("Market can send messages only to the broker");
			sendRejection(socket, message, "Market can send messages only to the broker");
		} else {
			Socket destinationSocket = RoutingTable.getRoute(destinationId);

			if (destinationSocket != null) {
				if (next != null) {
					next.handle(destinationSocket, message);
				}
			} else {
				System.out.println("Destination not found for message: " + message);
				sendRejection(socket, message, "Destination not found");
			}
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

	private int parseSourceId(String message) {
		// Extract the source ID from the message
		// The separator is \u0001 (ASCII SOH) character
		// The source ID is the value of the 49 tag

		String[] parts = message.split("\u0001");
		for (String part : parts) {
			if (part.startsWith("49=")) {
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
			System.out.println("Message forwarded: " + message);
		} catch (IOException e) {
			System.out.println("Error forwarding message: " + e.getMessage());
			int sourceId = 0;
			int destinationId = 0;
			String[] parts = message.split("\u0001");
			for (String part : parts) {
				if (part.startsWith("49=")) {
					sourceId = Integer.parseInt(part.substring(3));
				} else if (part.startsWith("56=")) {
					destinationId = Integer.parseInt(part.substring(3));
				}
			}
			RoutingTable.addPendingMessage(destinationId, message);
			System.out.println("Message saved for failover: " + message);
			Socket sourceSocket = RoutingTable.getRoute(sourceId);
			if (RoutingTable.isBrokerRoute(socket))
				sendRejection(sourceSocket, message, "Broker not available");
			else if (RoutingTable.isMarketRoute(socket))
				sendRejection(sourceSocket, message, "Market not available");
		}
	}
}
