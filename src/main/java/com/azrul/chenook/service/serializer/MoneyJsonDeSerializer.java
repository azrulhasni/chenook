/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service.serializer;

import com.azrul.chenook.value.MoneyValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;

/**
 *
 * @author azrul
 */
public class MoneyJsonDeSerializer  extends JsonDeserializer<MonetaryAmount> {

    @Override
    public MonetaryAmount deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
        MoneyValue value = p.readValueAs(MoneyValue.class);
        MonetaryAmount ma = Monetary.getDefaultAmountFactory().setCurrency(value.getCurrencyCode()).setNumber(value.getMonetaryValue()).create();//value.getMonetaryValue(), value.getCurrencyCode()); 
        return ma;
       
    }
}