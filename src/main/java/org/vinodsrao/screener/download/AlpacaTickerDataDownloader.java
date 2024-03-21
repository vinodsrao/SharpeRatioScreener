package org.vinodsrao.screener.download;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBarsResp;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vinodsrao.screener.model.AlpacaTickerData;

import java.lang.invoke.MethodHandles;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.vinodsrao.screener.Constants.*;

public class AlpacaTickerDataDownloader extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AlpacaAPI api;

    public AlpacaTickerDataDownloader(AlpacaAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        AtomicInteger tickerDataDownloaded = new AtomicInteger(0);
        while(true) {
            try {
                String chunkTickers = DOWNLOAD_QUEUE.poll(30, TimeUnit.SECONDS);
                if(chunkTickers != null) {
                    handleChunk(chunkTickers,tickerDataDownloaded);
                } else {
                    break;
                }
            }catch (InterruptedException e) {
                logger.error("{} was interrupted!!!", Thread.currentThread().getName());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
        logger.warn("Total number of ticker data downloaded {}",tickerDataDownloaded.get());
        logger.warn("Thread {} completed running", Thread.currentThread());
    }

    private void handleChunk(String chunkTickers, AtomicInteger tickerDataDownloaded) throws ApiException {
        String nextPageToken = null;
        StockBarsResp resp = null;
        do {
            String timeframe = System.getenv(BAR_TIMEFRAME);
            int lookback = Integer.parseInt(System.getenv(LOOKBACK));
            resp = api.marketData().stock().stockBars(chunkTickers, timeframe, OffsetDateTime.now().minusMonths(lookback),
                    OffsetDateTime.now().minusDays(1), 10000L, null, null, null, null, nextPageToken, null);
            resp.getBars().entrySet().forEach(entry -> {
                AlpacaTickerData data = new AlpacaTickerData().setSymbol(entry.getKey()).setBars(entry.getValue());
                try {
                    FILTER_QUEUE.put(data);
                } catch (InterruptedException e) {
                    logger.error("Failed to put {} into filter queue", data.getSymbol());
                }
            });
            tickerDataDownloaded.addAndGet(resp.getBars().size());
            nextPageToken = resp.getNextPageToken();
        }while(resp != null && StringUtils.isNotEmpty(nextPageToken));
    }
}
