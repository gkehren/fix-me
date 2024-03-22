package com.market;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Market {
	private static final int ROUTER_PORT = 5001;
	private int marketID;
	private Socket socket;
	private List<Instrument> instruments;

	public Market(List<Instrument> instruments) {
		this.marketID = -1;
		this.socket = null;
		this.instruments = instruments;
	}

	public int start() {
		try {
			this.socket = new Socket("localhost", ROUTER_PORT);

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.marketID = Integer.parseInt(in.readLine());

			System.out.println("Connected to the router. Market ID: " + marketID);
			return 0;
		} catch (IOException e) {
			System.out.println("Error connecting to the router: " + e.getMessage());
			return -1;
		}
	}

	public void stop() {
		try {
			if (socket != null && !socket.isClosed())
				socket.close();
		} catch (IOException e) {
			System.out.println("Error closing socket: " + e.getMessage());
		}
	}

	public void listen() {
		try {
			System.out.println("Waiting for messages...");
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String message;
			while (true) {
				message = in.readLine();
				if (message != null && message.length() > 0) {
					processMessage(message);
				}
				message = null;
			}
		} catch (IOException e) {
			System.out.println("Error reading from socket: " + e.getMessage());
		}
		System.out.println("Market stopped.");
	}

	public void processMessage(String message) {
		System.out.println("Received message: " + message);
		// Process incoming message from a broker
		// Check if the message is a buy or sell order
		// Extract the instrument ID, quantity, and price from the message
		// Call processOrder() to handle the order
	}

	public void processBuyOrder(int brokerID, int instrumentID, int quantity, double price) {
		// Process incoming order from a broker
		// Check if the requested instrument is available for trading
		// Check if the requested quantity can be fulfilled
		// Update internal instrument list accordingly
		// Send execution confirmation or rejection to the broker
	}

	public void processSellOrder(int brokerID, int instrumentID, int quantity, double price) {
		// Process incoming order from a broker
		// Check if the requested instrument is available for trading
		// Check if the requested quantity can be fulfilled
		// Update internal instrument list accordingly
		// Send execution confirmation or rejection to the broker
	}

	// Methods to handle order execution and sending messages to brokers
	// e.g., sendExecutionConfirmation(), sendRejection()
}
