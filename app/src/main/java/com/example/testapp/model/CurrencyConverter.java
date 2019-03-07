package com.example.testapp.model;

import java.util.Map;

public class CurrencyConverter {

    private Map<String, Double> currencyList;

    public CurrencyConverter(Map<String, Double> currencyList) {
        this.currencyList = currencyList;
    }

    /**
     *
     * @param fromCurrency
     * @param toCurrency
     * @param inputAmount
     * @return double
     */
    public double ConvertCurrency(String fromCurrency, String toCurrency, double inputAmount) {
        double fromRate = 0.0, toRate = 0.0;

        try {
            fromRate = this.currencyList.get(fromCurrency);
            toRate = this.currencyList.get(toCurrency);

        } catch (NullPointerException ex){
            ex.printStackTrace();
        }

        return (inputAmount / fromRate) * toRate;
    }

}
