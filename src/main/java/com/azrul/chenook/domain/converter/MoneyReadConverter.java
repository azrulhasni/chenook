/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.converter;

import com.azrul.chenook.service.serializer.*;
import com.azrul.chenook.value.MoneyValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.format.number.money.MonetaryAmountFormatter;

/**
 *
 * @author azrul
 */
@ReadingConverter
public class MoneyReadConverter  implements Converter<String,MonetaryAmount> { 



    @Override
    public MonetaryAmount convert(String source) {
       MonetaryAmountFormatter formatter =  new MonetaryAmountFormatter();
       return formatter.parse(source, Locale.getDefault());
    }
}