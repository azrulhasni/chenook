/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service.serializer;

import com.azrul.chenook.value.MoneyValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nimbusds.jwt.util.DateUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;

/**
 *
 * @author azrul
 */
public class MoneyJsonSerializer  extends JsonSerializer<MonetaryAmount> {

    @Override
    public void serialize(MonetaryAmount value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
        try {
            MoneyValue moneyValue = new MoneyValue();
            moneyValue.setCurrencyCode(value.getCurrency().getCurrencyCode());
            moneyValue.setMonetaryValue(value.getNumber().numberValue(BigDecimal.class));
            gen.writePOJO(moneyValue);
        } catch (DateTimeParseException e) {
            System.err.println(e);
            gen.writeString("");
        }
    }
}