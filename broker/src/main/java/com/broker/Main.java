package com.broker;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		try {
			Broker broker = new Broker();
			if (broker.start() == -1)
				return;

			System.out.println("Type 'help' for a list of commands.");
			Scanner scanner = new Scanner(System.in);
			String command;
			while (true) {
				System.out.print("> ");
				command = scanner.nextLine();
				if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit")) {
					break;
				} else if (command.equalsIgnoreCase("help")) {
					printHelp();
					continue;
				}

				String[] parts = command.split(" ");
				if (parts.length < 5) {
					System.out.println("Invalid command. Type 'help' for a list of commands.");
					continue;
				}

				String action = parts[0];
				int marketID = Integer.parseInt(parts[1]);
				int instrumentID = Integer.parseInt(parts[2]);
				int quantity = Integer.parseInt(parts[3]);
				double price = Double.parseDouble(parts[4]);

				switch (action.toLowerCase()) {
					case "buy":
						broker.sendOrder(true, marketID, instrumentID, quantity, price);
						break;
					case "sell":
						broker.sendOrder(false, marketID, instrumentID, quantity, price);
						break;
					default:
						System.out.println("Invalid command. Type 'help' for a list of commands.");
						break;
				}
			}

			broker.stop();
			scanner.close();
		} catch (Exception e) {
			System.out.println("Error starting broker: " + e.getMessage());
		}
	}

	public static void printHelp() {
		System.out.println("Commands:");
		System.out.println("buy {marketID} {instrumentID} {quantity} {price}");
		System.out.println("sell {marketID} {instrumentID} {quantity} {price}");
		System.out.println("exit, quit");
	}
}
