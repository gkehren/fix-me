package com.market;

public class Instrument {
	private String symbol;
	private int availableQuantity;

	public Instrument(String symbol, int availableQuantity) {
		this.symbol = symbol;
		this.availableQuantity = availableQuantity;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public int getAvailableQuantity() {
		return this.availableQuantity;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setAvailableQuantity(int availableQuantity) {
		this.availableQuantity = availableQuantity;
	}
}
