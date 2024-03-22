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


	public void sendOrder(boolean isBuy, int marketID, int instrumentID, int quantity, double price) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			String fixMessage = "8=FIX.4.4\u0001" + // BeginString
								"49=" + brokerID + "\u0001" + // SenderCompID
								"56=" + marketID + "\u0001" + // TargetCompID
								"55=" + instrumentID + "\u0001" + // Symbol
								"54=" + (isBuy ? "1" : "2") + "\u0001" + // Side
								"38=" + quantity + "\u0001" + // OrderQty
								"44=" + price + "\u0001"; // Price

			int checksum = 0;
			for (char ch : fixMessage.toCharArray())
				checksum += ch;
			checksum %= 256;
			String checkSumStr = String.format("%03d", checksum);
			fixMessage += "10=" + checkSumStr + "\u0001";

			out.println(fixMessage);
		} catch (IOException e) {
			System.out.println("Error sending buy order: " + e.getMessage());
		}
	}

	// Methods to handle responses from the market
	// e.g., handleExecutionConfirmation(), handleRejection()
}
