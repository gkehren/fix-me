package com.market;

import java.util.ArrayList;
import java.util.List;

public class Market {
	private static final int ROUTER_PORT = 5001;

	private String marketID;
	private List<Instrument> instruments;

	public Market(String marketID) {
		this.marketID = marketID;
		this.instruments = new ArrayList<>();
		// Initialize instrument list with available instruments
	}

	public void start() {
		// Establish connection with the message router
		// Communicate market ID to the router
		// Start listening for incoming orders from brokers
	}

	public void processOrder(/* parameters */) {
		// Process incoming order from a broker
		// Check if the requested instrument is available for trading
		// Check if the requested quantity can be fulfilled
		// Update internal instrument list accordingly
		// Send execution confirmation or rejection to the broker
	}

	// Methods to handle order execution and sending messages to brokers
	// e.g., sendExecutionConfirmation(), sendRejection()
}
