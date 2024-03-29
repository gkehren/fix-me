CREATE TABLE buy_transactions (
	id SERIAL PRIMARY KEY,
	broker_id INT NOT NULL,
	market_id INT NOT NULL,
	instrument_id VARCHAR(255) NOT NULL,
	quantity INT NOT NULL,
	price NUMERIC NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sell_transactions (
	id SERIAL PRIMARY KEY,
	broker_id INT NOT NULL,
	market_id INT NOT NULL,
	instrument_id VARCHAR(255) NOT NULL,
	quantity INT NOT NULL,
	price NUMERIC NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
