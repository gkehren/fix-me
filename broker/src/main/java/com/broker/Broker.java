package com.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Broker {
	private static final int ROUTER_PORT = 5000;
	private int brokerID;
	private Socket socket;

	public Broker() {
		this.brokerID = -1;
		this.socket = null;
	}

	public int start() {
		try {
			this.socket = new Socket("localhost", ROUTER_PORT);

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.brokerID = Integer.parseInt(in.readLine());

			System.out.println("Connected to the router. Broker ID: " + brokerID);
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

	public void sendBuyOrder(int marketID, int instrumentID, int quantity, double price) {
		// Construct and send a buy order message to the router
		// Include broker ID in the message

		// FOR TESTING PURPOSES ONLY (need to use FIX protocol)
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("BUY " + brokerID + " " + marketID + " " + instrumentID + " " + quantity + " " + price);
		} catch (IOException e) {
			System.out.println("Error sending buy order: " + e.getMessage());
		}
	}

	public void sendSellOrder(int marketID, int instrumentID, int quantity, double price) {
		// Construct and send a sell order message to the router
		// Include broker ID in the message

		// FOR TESTING PURPOSES ONLY (need to use FIX protocol)
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("SELL " + brokerID + " " + marketID + " " + instrumentID + " " + quantity + " " + price);
		} catch (IOException e) {
			System.out.println("Error sending sell order: " + e.getMessage());
		}
	}

	// Methods to handle responses from the market
	// e.g., handleExecutionConfirmation(), handleRejection()
}
