package org.vinodsrao.screener.calculate;

import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vinodsrao.screener.model.AlpacaTickerData;
import org.vinodsrao.screener.model.Performance;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.vinodsrao.screener.Constants.*;

public class PerformanceCalculator extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final double riskFreeReturnPerMonth;
    private final double minimumReqdSharpeRatio;
    private final SortedSet<Performance> assetPerformanceSortedBySharpeRatio;
    private final Timer statusTimer;

    public PerformanceCalculator() {
        statusTimer = new Timer();
        assetPerformanceSortedBySharpeRatio = new TreeSet<>();
        riskFreeReturnPerMonth = Double.parseDouble(System.getenv(RISK_FREE_RETURN_PER_MONTH));
        minimumReqdSharpeRatio = Double.parseDouble(System.getenv(MINIMUM_REQUIRED_SHARPE_RATIO));
        statusTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logger.warn("Total tickers in download queue {}",DOWNLOAD_QUEUE.size());
                logger.warn("Total tickers in filter queue {}",FILTER_QUEUE.size());
                logger.warn("Total tickers in calculation queue {}",CALCULATION_QUEUE.size());
                logger.warn("Total tickers in output set {}", assetPerformanceSortedBySharpeRatio.size());
            }
        },0, Duration.ofMinutes(1).toMillis());
    }

    public void run() {
        long start = System.currentTimeMillis();
        while(true) {
            try {
                AlpacaTickerData dto = CALCULATION_QUEUE.poll(30, TimeUnit.SECONDS);
                if(dto == null) {
                    calculateAllocation();
                    print();
                    long end = System.currentTimeMillis();
                    logger.warn("Completed screening in {} minutes", Duration.ofMillis(end-start).toMinutes());
                    break;
                } else {
                    Performance performance = calculatePerformance(dto);
                    if(performance.getSharpeRatio() >= minimumReqdSharpeRatio) {
                        assetPerformanceSortedBySharpeRatio.add(performance);
                    }
                }
            }catch (InterruptedException e) {
                logger.error("Thread {} was interrupted", Thread.currentThread());
            }
        }
        statusTimer.cancel();
        logger.warn("Thread {} completed running", Thread.currentThread());
    }

    private void calculateAllocation() {
        int capital = Integer.parseInt(System.getenv(CAPITAL));
        int positions = Integer.parseInt(System.getenv(POSITIONS));
        final List<Performance> symbolsInScope = assetPerformanceSortedBySharpeRatio.stream().limit(positions).collect(Collectors.toList());
        final double cumulativeSharpeRatio = symbolsInScope.stream().map(Performance::getSharpeRatio).reduce(0.0,(p1,p2) -> p1+p2);
        assetPerformanceSortedBySharpeRatio.stream().limit(positions).forEach(performance -> {
            performance.setAllocation(Math.floor(performance.getSharpeRatio() / cumulativeSharpeRatio * 100));
            performance.setQty(Math.floor((capital * (performance.getAllocation() / 100)) / performance.getCurrPrice()));
            performance.setAmt(Math.floor(performance.getQty() * performance.getCurrPrice()));
        });
    }

    private Performance calculatePerformance(AlpacaTickerData dto) {
        List<StockBar> dtos = dto.getBars();
        double std = std(dtos);
        return new Performance()
                .setSymbol(dto.getSymbol())
                .setTotalReturn(percReturn(dtos.get(0).getO(), dtos.get(6).getC()))
                .setCurrPrice(dtos.get(6).getC())
                .setCmPer(percReturn(dtos.get(6).getO(), dtos.get(6).getC()))
                .setCm1Per(percReturn(dtos.get(5).getO(), dtos.get(5).getC()))
                .setCm2Per(percReturn(dtos.get(4).getO(), dtos.get(4).getC()))
                .setCm3Per(percReturn(dtos.get(3).getO(), dtos.get(3).getC()))
                .setCm4Per(percReturn(dtos.get(2).getO(), dtos.get(2).getC()))
                .setCm5Per(percReturn(dtos.get(1).getO(), dtos.get(1).getC()))
                .setCm6Per(percReturn(dtos.get(0).getO(), dtos.get(0).getC()))
                .setStd(std)
                .setSharpeRatio(sharpeRatio(dtos, std));
    }

    private double sharpeRatio(List<StockBar> dtos, double std) {
        List<Double> percChanges = dtos.stream().map(dto -> percReturn(dto.getO(), dto.getC())).collect(Collectors.toList());
        double sum = percChanges.stream().reduce((p1,p2) -> (p1+p2)).get();
        return ((sum/dtos.size()) - riskFreeReturnPerMonth) / std;
    }

    private double percReturn(double open, double close) {
        return ((close / open) - 1.0) * 100.0;
    }

    private double std(List<StockBar> dtos) {
        List<Double> percChanges = dtos.stream().map(dto -> percReturn(dto.getO(), dto.getC())).collect(Collectors.toList());
        double sum = percChanges.stream().reduce((p1,p2) -> (p1+p2)).get();
        final double mean = sum / dtos.size();
        double variance = percChanges.stream().map(pc -> Math.pow(pc - mean, 2)).reduce(0.0,(p1,p2) -> p1+p2);
        return Math.sqrt(variance / dtos.size());
    }

    private void print() {
        int positions = Integer.parseInt(System.getenv(POSITIONS));
        System.out.println("Total Screened Universe = " + assetPerformanceSortedBySharpeRatio.size() + ". Displaying top " + positions);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%3s %6ss %12s %12s %12s %15s %15s %15s %15s", "#", "SYMBOL", "Allocation", "Qty", "Amount", "Sharpe Ratio", "Curr Price", "Total Return", "STD");
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");

        AtomicInteger counter = new AtomicInteger(0);
        assetPerformanceSortedBySharpeRatio.stream()
                .limit(positions)
                .forEachOrdered(performanceMetrics -> {
                    System.out.format("%3s %6s %12.0f %12.0f %12.0f %15.2f %15.2f %15.2f %15.2f",
                            counter.incrementAndGet(),
                            performanceMetrics.getSymbol(),
                            performanceMetrics.getAllocation(),
                            performanceMetrics.getQty(),
                            performanceMetrics.getAmt(),
                            performanceMetrics.getSharpeRatio(),
                            performanceMetrics.getCurrPrice(),
                            performanceMetrics.getTotalReturn(),
                            performanceMetrics.getStd());
                    System.out.println();
                });

        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
    }
}
