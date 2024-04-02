package com.broker;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		try {
			int id = -1;
			if (args.length > 0) {
				id = Integer.parseInt(args[0]);
			}
			Broker broker = new Broker(id);
			if (broker.start() == -1)
				return;

			System.out.println("Type 'help' for a list of commands.");
			Scanner scanner = new Scanner(System.in);
			String command;
			while (broker.isRunning()) {
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
				String instrumentID = parts[2];
				int quantity = Integer.parseInt(parts[3]);
				double price = Double.parseDouble(parts[4]);

				switch (action.toLowerCase()) {
					case "buy":
						broker.sendOrder(true, marketID, instrumentID, quantity, price);
						break;
					case "sell":
						broker.sendOrder(false, marketID, instrumentID, quantity, price);
						break;
					case "exit":
						break;
					case "quit":
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
