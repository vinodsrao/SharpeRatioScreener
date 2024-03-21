package org.vinodsrao.screener.download;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;
import net.jacobpeterson.alpaca.openapi.trader.model.Exchange;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import static org.vinodsrao.screener.Constants.DOWNLOAD_QUEUE;
import static org.vinodsrao.screener.Constants.DOWNLOAD_TICKER_DATA_CHUNK_SIZE;

public class AlpacaTickerListDownloader extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AlpacaAPI api;

    public AlpacaTickerListDownloader(AlpacaAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        try {
            List<Assets> assets = api.trader().assets().getV2Assets("active", "us_equity", null, null);
            logger.warn("Total assets : " + assets.size());
            List<String> tickers = assets.stream().filter(a -> !Exchange.OTC.equals(a.getExchange()) && a.getTradable()).map(a -> a.getSymbol()).collect(Collectors.toList());
            logger.warn("Total assets to download after filtering OTC and non tradeable : " + tickers.size());
            int chunkSize = Integer.parseInt(System.getenv(DOWNLOAD_TICKER_DATA_CHUNK_SIZE));
            String chunkTickers = "";
            int chunkCounter = 0;
            for (int i=1; i<=tickers.size(); i++) {
                chunkCounter++;
                chunkTickers = chunkTickers + tickers.get(i-1);
                if ((chunkCounter == chunkSize) || (i == tickers.size())) {
                    DOWNLOAD_QUEUE.put(chunkTickers);
                    chunkTickers = "";
                    chunkCounter = 0;
                } else {
                    chunkTickers = chunkTickers + ",";
                }
            }
        }catch (Exception e) {
            logger.error("Error : " + ExceptionUtils.getStackTrace(e));
        }
        logger.warn("Thread {} completed running", Thread.currentThread());
    }
}
