package org.vinodsrao.screener;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vinodsrao.screener.calculate.PerformanceCalculator;
import org.vinodsrao.screener.download.AlpacaTickerDataDownloader;
import org.vinodsrao.screener.download.AlpacaTickerListDownloader;
import org.vinodsrao.screener.filter.FilterTickers;

public class Screener {
    private final static Logger logger = LoggerFactory.getLogger(Screener.class);
    public static void main(String[] args) {
        String alpacaKey = System.getenv(Constants.ALPACA_KEY);
        String alpacaSecret = System.getenv(Constants.ALPACA_SECRET);

        final TraderAPIEndpointType endpointType = TraderAPIEndpointType.PAPER; // or 'LIVE'
        final MarketDataWebsocketSourceType sourceType = MarketDataWebsocketSourceType.IEX; // or 'SIP'
        final AlpacaAPI alpacaAPI = new AlpacaAPI(alpacaKey, alpacaSecret, endpointType, sourceType);

        Thread listDownloader = new AlpacaTickerListDownloader(alpacaAPI);
        Thread dataDownloader = new AlpacaTickerDataDownloader(alpacaAPI);
        Thread filter = new FilterTickers();
        Thread perfCalculator = new PerformanceCalculator();

        listDownloader.start();
        dataDownloader.start();
        filter.start();
        perfCalculator.start();

        try {
            listDownloader.join();
            dataDownloader.join();
            filter.join();
            perfCalculator.join();
        }catch (InterruptedException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }

        logger.info("Completed Screening!!!");

    }
}
