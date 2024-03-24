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

		System.out.println("Instruments available in the market:");
		for (Instrument instrument : instruments) {
			System.out.println("Symbol: " + instrument.getSymbol() + ", Quantity: " + instrument.getAvailableQuantity());
		}
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
		String[] parts = message.split("\u0001");
		int brokerID = Integer.parseInt(parts[1].split("=")[1]);
		String instrumentID = parts[3].split("=")[1];
		int quantity = Integer.parseInt(parts[5].split("=")[1]);
		boolean isBuy = parts[4].split("=")[1].equals("1");
		double price = Double.parseDouble(parts[6].split("=")[1]);

		if (isBuy) {
			processBuyOrder(brokerID, instrumentID, quantity, price);
		} else {
			processSellOrder(brokerID, instrumentID, quantity, price);
		}
	}

	public void processBuyOrder(int brokerID, String instrumentID, int quantity, double price) {
		Instrument instrument = instruments.stream().filter(i -> i.getSymbol().equals(instrumentID)).findFirst().orElse(null);
		if (instrument == null) {
			System.out.println("Instrument not found: " + instrumentID);
			sendRejection(brokerID, instrumentID, quantity, price, "Instrument not found");
			return;
		}
		if (instrument.getAvailableQuantity() < quantity) {
			System.out.println("Insufficient quantity for instrument: " + instrumentID);
			sendRejection(brokerID, instrumentID, quantity, price, "Insufficient quantity");
			return;
		}
		instrument.setAvailableQuantity(instrument.getAvailableQuantity() - quantity);
		System.out.println("Buy order executed for instrument: " + instrumentID + ", quantity: " + quantity + ", price: " + price);
		sendExecutionConfirmation(true, brokerID, instrumentID, quantity, price);
	}

	public void processSellOrder(int brokerID, String instrumentID, int quantity, double price) {
		Instrument instrument = instruments.stream().filter(i -> i.getSymbol().equals(instrumentID)).findFirst().orElse(null);
		if (instrument == null) {
			System.out.println("Instrument not found: " + instrumentID);
			sendRejection(brokerID, instrumentID, quantity, price, "Instrument not found");
			return;
		}
		instrument.setAvailableQuantity(instrument.getAvailableQuantity() + quantity);
		System.out.println("Sell order executed for instrument: " + instrumentID + ", quantity: " + quantity + ", price: " + price);
		sendExecutionConfirmation(false, brokerID, instrumentID, quantity, price);
	}

	public void sendExecutionConfirmation(boolean isBuy, int brokerID, String instrumentID, int quantity, double price) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			String body = "35=8\u0001" + // MsgType = ExecutionReport
						  "49=" + marketID + "\u0001" + // SenderCompID
						  "56=" + brokerID + "\u0001" + // TargetCompID
						  "55=" + instrumentID + "\u0001" + // Symbol
						  "54=" + (isBuy ? "1" : "2") + "\u0001" + // Side
						  "38=" + quantity + "\u0001" + // OrderQty
						  "44=" + price + "\u0001" + // Price
						  "39=2\u0001" + // OrdStatus (2 = Filled)
						  "150=2\u0001" + // ExecType = Trade
						  "151=0\u0001"; // LeavesQty = 0

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

	public void sendRejection(int brokerID, String instrumentID, int quantity, double price, String reason) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			String body = "35=8\u0001" + // MsgType = ExecutionReport
						  "49=" + marketID + "\u0001" + // SenderCompID
						  "56=" + brokerID + "\u0001" + // TargetCompID
						  "55=" + instrumentID + "\u0001" + // Symbol
						  "54=1\u0001" + // Side (1 = Buy)
						  "38=" + quantity + "\u0001" + // OrderQty
						  "44=" + price + "\u0001" + // Price
						  "39=8\u0001" + // OrdStatus (8 = Rejected)
						  "150=8\u0001" + // ExecType = Rejected
						  "151=" + quantity + "\u0001" + // LeavesQty = OrderQty
						  "58=" + reason + "\u0001"; // Text = Reason for rejection

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
			System.out.println("Error sending rejection: " + e.getMessage());
		}
	}
}
