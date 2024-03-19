package com.router;

public class Main {
	public static void main(String[] args) {
		Router router = new Router();

		try {
			router.start();
		} catch (Exception e) {
			System.out.println("Error in the router: " + e.getMessage());
		}
	}
}
