package com.market;

public class Instrument {
	private String symbol;
	private int availableQuantity;
	private double price;

	public Instrument(String symbol, int availableQuantity, double price) {
		this.symbol = symbol;
		this.availableQuantity = availableQuantity;
		this.price = price;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public int getAvailableQuantity() {
		return this.availableQuantity;
	}

	public double getPrice() {
		return this.price;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setAvailableQuantity(int availableQuantity) {
		this.availableQuantity = availableQuantity;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
