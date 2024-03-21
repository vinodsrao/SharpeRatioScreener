package org.vinodsrao.screener.model;

public final class Performance implements Comparable {
    private String symbol;
    private double allocation;
    private double qty;
    private double amt;

    private double sharpeRatio;
    private double currPrice;
    private double totalReturn;
    private double std;
    private double cmPer;
    private double cm1Per;
    private double cm2Per;
    private double cm3Per;
    private double cm4Per;
    private double cm5Per;
    private double cm6Per;

    public double getAmt() {
        return amt;
    }

    public Performance setAmt(double amt) {
        this.amt = amt;
        return this;
    }

    public double getAllocation() {
        return allocation;
    }

    public Performance setAllocation(double allocation) {
        this.allocation = allocation;
        return this;
    }

    public double getQty() {
        return qty;
    }

    public Performance setQty(double qty) {
        this.qty = qty;
        return this;
    }

    public double getSharpeRatio() {
        return sharpeRatio;
    }

    public String getSymbol() {
        return symbol;
    }

    public Performance setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public Performance setSharpeRatio(double sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
        return this;
    }

    public double getCurrPrice() {
        return currPrice;
    }

    public Performance setCurrPrice(double currPrice) {
        this.currPrice = currPrice;
        return this;
    }

    public double getTotalReturn() {
        return totalReturn;
    }

    public Performance setTotalReturn(double totalReturn) {
        this.totalReturn = totalReturn;
        return this;
    }

    public double getStd() {
        return std;
    }

    public Performance setStd(double std) {
        this.std = std;
        return this;
    }

    public double getCmPer() {
        return cmPer;
    }

    public Performance setCmPer(double cmPer) {
        this.cmPer = cmPer;
        return this;
    }

    public double getCm1Per() {
        return cm1Per;
    }

    public Performance setCm1Per(double cm1Per) {
        this.cm1Per = cm1Per;
        return this;
    }

    public double getCm2Per() {
        return cm2Per;
    }

    public Performance setCm2Per(double cm2Per) {
        this.cm2Per = cm2Per;
        return this;
    }

    public double getCm3Per() {
        return cm3Per;
    }

    public Performance setCm3Per(double cm3Per) {
        this.cm3Per = cm3Per;
        return this;
    }

    public double getCm4Per() {
        return cm4Per;
    }

    public Performance setCm4Per(double cm4Per) {
        this.cm4Per = cm4Per;
        return this;
    }

    public double getCm5Per() {
        return cm5Per;
    }

    public Performance setCm5Per(double cm5Per) {
        this.cm5Per = cm5Per;
        return this;
    }

    public double getCm6Per() {
        return cm6Per;
    }

    public Performance setCm6Per(double cm6Per) {
        this.cm6Per = cm6Per;
        return this;
    }

    @Override
    public int compareTo(Object o) {
        Performance other = (Performance) o;
        return Math.negateExact(Double.compare(sharpeRatio,other.sharpeRatio));
    }
}
