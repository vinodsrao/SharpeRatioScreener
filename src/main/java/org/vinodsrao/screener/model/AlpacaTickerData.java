package org.vinodsrao.screener.model;

import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;

import java.util.List;

public final class AlpacaTickerData {

    private String symbol;
    private List<StockBar> bars;

    public String getSymbol() {
        return symbol;
    }

    public AlpacaTickerData setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public List<StockBar> getBars() {
        return bars;
    }

    public AlpacaTickerData setBars(List<StockBar> bars) {
        this.bars = bars;
        return this;
    }
}
