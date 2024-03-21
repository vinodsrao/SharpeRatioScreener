package org.vinodsrao.screener;

import org.vinodsrao.screener.model.AlpacaTickerData;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Constants {

    public static final BlockingQueue<String> DOWNLOAD_QUEUE = new LinkedBlockingQueue();
    public static final BlockingQueue<AlpacaTickerData> CALCULATION_QUEUE = new LinkedBlockingQueue();
    public static final BlockingQueue<AlpacaTickerData> FILTER_QUEUE = new LinkedBlockingQueue();

    public static final String CAPITAL = "CAPITAL";
    public static final String POSITIONS = "POSITIONS";
    public static final String ALPACA_KEY="ALPACA_KEY";
    public static final String ALPACA_SECRET="ALPACA_SECRET";
    public static final String DOWNLOAD_TICKER_DATA_CHUNK_SIZE="DOWNLOAD_TICKER_DATA_CHUNK_SIZE";
    public static final String BAR_TIMEFRAME="BAR_TIMEFRAME";
    public static final String LOOKBACK="LOOKBACK";
    public static final String MIN_PRICE_LEVEL_FROM_HIGH = "MIN_PRICE_LEVEL_FROM_HIGH";
    public static final String RISK_FREE_RETURN_PER_MONTH = "RISK_FREE_RETURN_PER_MONTH";
    public static final String MINIMUM_REQUIRED_SHARPE_RATIO = "MINIMUM_REQUIRED_SHARPE_RATIO";
}
