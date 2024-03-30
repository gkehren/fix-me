package com.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Broker {
	private static final int ROUTER_PORT = 5000;
	private int brokerID;
	private Socket socket;
	private int uniqueOrderID = 1;

	public Broker(int id) {
		this.brokerID = id;
		this.socket = null;
	}

	public int start() {
		try {
			this.socket = new Socket("localhost", ROUTER_PORT);

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(brokerID);

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.brokerID = Integer.parseInt(in.readLine());

			System.out.println("Connected to the router. Broker ID: " + brokerID);

			new Thread(() -> {
				while (true) {
					try {
						String message = in.readLine();
						if (message != null) {
							handleMessage(message);
						}
					} catch (IOException e) {
						System.out.println("Error reading message: " + e.getMessage());
						break;
					}
				}
			}).start();
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


	public void sendOrder(boolean isBuy, int marketID, String instrumentID, int quantity, double price) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			String body = "35=D" + "\u0001" + // MsgType = NewOrderSingle
						  "11=" + uniqueOrderID  + "\u0001" + // ClOrdID
						  "49=" + brokerID + "\u0001" + // SenderCompID
						  "56=" + marketID + "\u0001" + // TargetCompID
						  "55=" + instrumentID + "\u0001" + // Symbol
						  "54=" + (isBuy ? "1" : "2") + "\u0001" + // Side
						  "38=" + quantity + "\u0001" + // OrderQty
						  "44=" + price + "\u0001" + // Price
						  "40=1" + "\u0001"; // OrdType = Market

			String fixMessage = "8=FIX.4.4" + "\u0001" + // BeginString
								"9=" + body.length() + "\u0001" + // BodyLength
								body;

			int checksum = 0;
			for (char ch : fixMessage.toCharArray())
				checksum += ch;
			checksum %= 256;
			String checkSumStr = String.format("%03d", checksum);
			fixMessage += "10=" + checkSumStr + "\u0001";

			out.println(fixMessage);
			uniqueOrderID++;
		} catch (IOException e) {
			System.out.println("Error sending buy order: " + e.getMessage());
		}
	}

	public void handleMessage(String message) {
		String[] parts = message.split("\u0001");
		Map<String, String> fields = new HashMap<>();
		for (String part : parts) {
			String[] keyValue = part.split("=");
			if (keyValue.length == 2)
				fields.put(keyValue[0], keyValue[1]);
		}

		String msgType = fields.get("35");
		if ("8".equals(msgType)) {
			String status = fields.get("39");
			if ("2".equals(status))
				System.out.println("Order executed successfully at the market(" + fields.get("49") + "): " + fields.get("55") + " " + fields.get("38") + " @ " + fields.get("44"));
			else if ("8".equals(status))
				System.out.println("Order rejected by the market(" + fields.get("49") + "): " + fields.get("55") + " " + fields.get("38") + " @ " + fields.get("44") + " (" + fields.get("58") + ")");
			else
				System.out.println("Unknown status: " + status);
		} else if ("3".equals(msgType)) {
			System.out.println("Order rejected by the router: " + fields.get("58"));
		} else {
			System.out.println("Unknown message type: " + msgType);
		}
	}
}
