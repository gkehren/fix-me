package com.market;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class DatabaseHandler {
	private static final String DB_URL = "jdbc:postgresql://localhost:5003/fix-me";
	private static final String DB_USER = "admin";
	private static final String DB_PASSWORD = "admin";

	public static void insertBuyTransaction(int brokerID, int marketID, String symbol, int quantity, double price) {
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			String query = "INSERT INTO buy_transactions (broker_id, market_id, instrument_id, quantity, price) VALUES (?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, brokerID);
			stmt.setInt(2, marketID);
			stmt.setString(3, symbol);
			stmt.setInt(4, quantity);
			stmt.setDouble(5, price);
			stmt.executeUpdate();
			System.out.println("Inserted buy transaction: Broker ID: " + brokerID + ", Market ID: " + marketID + ", Symbol: " + symbol + ", Quantity: " + quantity + ", Price: " + price);
		} catch (SQLException e) {
			System.out.println("Error inserting buy transaction: " + e.getMessage());
		}
	}

	public static void insertSellTransaction(int brokerID, int marketID, String symbol, int quantity, double price) {
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			String query = "INSERT INTO sell_transactions (broker_id, market_id, instrument_id, quantity, price) VALUES (?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, brokerID);
			stmt.setInt(2, marketID);
			stmt.setString(3, symbol);
			stmt.setInt(4, quantity);
			stmt.setDouble(5, price);
			stmt.executeUpdate();
			System.out.println("Inserted sell transaction: Broker ID: " + brokerID + ", Market ID: " + marketID + ", Symbol: " + symbol + ", Quantity: " + quantity + ", Price: " + price);
		} catch (SQLException e) {
			System.out.println("Error inserting sell transaction: " + e.getMessage());
		}
	}
}
