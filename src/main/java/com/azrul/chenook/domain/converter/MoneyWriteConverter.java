/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.converter;

import com.azrul.chenook.service.serializer.*;
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
import java.util.Locale;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.format.number.money.MonetaryAmountFormatter;

/**
 *
 * @author azrul
 */
@WritingConverter
public class MoneyWriteConverter   implements Converter<MonetaryAmount,String> { 



    @Override
    public String convert(MonetaryAmount source) {
       return source.toString();
    }
}