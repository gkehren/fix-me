package com.market;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java -jar market.jar <filename>");
			System.exit(1);
		}

		String filename = args[0];
		if (filename.isEmpty()) {
			System.out.println("Please provide a filename.");
			System.exit(1);
		} else if (filename.equalsIgnoreCase("random")) {
			filename = "random.txt";
			generateRandomInstruments(filename);
		} else if (!filename.endsWith(".txt")) {
			System.out.println("Invalid file format. Please provide a .txt file.");
			System.exit(1);
		}

		List<Instrument> instruments = readInstrumentsFromFile(filename);
		if (instruments.isEmpty()) {
			System.out.println("No instruments found in file.");
			System.exit(1);
		}

		Market market = new Market(instruments);
		if (market.start() == -1)
			return;

		market.listen();
	}

	public static List<Instrument> readInstrumentsFromFile(String filename) {
		List<Instrument> instruments = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length == 3) {
					String symbol = parts[0];
					int quantity = Integer.parseInt(parts[1]);
					double price = Double.parseDouble(parts[2]);
					instruments.add(new Instrument(symbol, quantity, price));
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading instruments from file: " + e.getMessage());
		}

		return instruments;
	}

	public static void generateRandomInstruments(String filename) {
		Random random = new Random();
		String[] symbols = {"AAPL", "GOOG", "MSFT", "AMZN", "FB", "TSLA", "NVDA", "NFLX", "IBM", "AMD", "QCOM", "ORCL", "CSCO"};

		try (PrintWriter writer = new PrintWriter(filename)) {
			for (int i = 0; i < symbols.length; i++) {
				String symbol = symbols[i];
				int quantity = random.nextInt(1000) + 1;
				double price = random.nextDouble() * 1000;
				writer.println(symbol + "," + quantity + "," + price);
			}
		} catch (IOException e) {
			System.out.println("Error writing to file: " + e.getMessage());
		}
	}
}
