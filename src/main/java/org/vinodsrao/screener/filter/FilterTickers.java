package org.vinodsrao.screener.filter;

import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vinodsrao.screener.model.AlpacaTickerData;

import java.util.concurrent.TimeUnit;

import static org.vinodsrao.screener.Constants.*;

public class FilterTickers extends Thread {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final double expectedAvgDollarVolumePerMonth;
    public final double minPriceLevelFromHigh;
    public final int lookback;

    public FilterTickers() {
        int capital = Integer.parseInt(System.getenv(CAPITAL));
        int positions = Integer.parseInt(System.getenv(POSITIONS));
        expectedAvgDollarVolumePerMonth = Math.floor(capital / positions * 1000) * 21;//Eg: 7_500_000 per day * 21 days;
        minPriceLevelFromHigh = Double.parseDouble(System.getenv(MIN_PRICE_LEVEL_FROM_HIGH));
        lookback = Integer.parseInt(System.getenv(LOOKBACK));
    }

    public void run() {
        while(true) {
            try {
                AlpacaTickerData dto = FILTER_QUEUE.poll(30, TimeUnit.SECONDS);
                if (dto == null) {
                    break;
                } else if(accepted(dto)) {
                    CALCULATION_QUEUE.put(dto);
                }
            }catch (InterruptedException e) {
                logger.error("Thread {} was interrupted", Thread.currentThread());
            }
        }
        logger.error("Thread {} completed running", Thread.currentThread());
    }

    private boolean accepted(AlpacaTickerData dto) {
        return (dto.getBars().size() == lookback) && //minimum 7 months data
                //volume expected
                ((dto.getBars().stream().map(v -> v.getV() * v.getC()).reduce(0.0,(v1,v2) -> v1 + v2)) / lookback >= expectedAvgDollarVolumePerMonth) &&
                //currPrice within acceptable distance from 7 month high
        dto.getBars().get(lookback-1).getC() >= dto.getBars().stream().map(StockBar::getH).reduce(0.0,(v1, v2) -> v1 >= v2 ? v1 : v2) * minPriceLevelFromHigh;
    }
}
