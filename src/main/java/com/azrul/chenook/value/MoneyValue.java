/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.value;

import java.math.BigDecimal;

/**
 *
 * @author azrul
 */
public class MoneyValue {
    private String currencyCode;
    private BigDecimal monetaryValue;

    /**
     * @return the currencyCode
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * @param currencyCode the currencyCode to set
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * @return the monetaryValue
     */
    public BigDecimal getMonetaryValue() {
        return monetaryValue;
    }

    /**
     * @param monetaryValue the monetaryValue to set
     */
    public void setMonetaryValue(BigDecimal monetaryValue) {
        this.monetaryValue = monetaryValue;
    }
}
