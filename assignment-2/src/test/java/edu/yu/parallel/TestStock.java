package edu.yu.parallel;

/** A stock for test setup, independent of your StockImpl. */
record TestStock(String getSymbol, String getName, double getPrice, int getShares) implements Stock {
}
