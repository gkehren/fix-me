package com.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Broker {
	private static final int ROUTER_PORT = 5000;

	private int brokerID;

	public Broker() {
		this.brokerID = -1;
	}

	public void start() {
		try (Socket socket = new Socket("localhost", ROUTER_PORT)) {

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.brokerID = Integer.parseInt(in.readLine());

			System.out.println("Connected to the router. Broker ID: " + brokerID);
		} catch (IOException e) {
			System.out.println("Error connecting to the router: " + e.getMessage());
		}
	}

	public void sendBuyOrder() {
		// Construct and send a buy order message to the router
		// Include broker ID in the message
	}

	public void sendSellOrder() {
		// Construct and send a sell order message to the router
		// Include broker ID in the message
	}

	// Methods to handle responses from the market
	// e.g., handleExecutionConfirmation(), handleRejection()
}
