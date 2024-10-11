/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.converter;

import jakarta.persistence.PersistenceContext;
import java.util.Locale;
import javax.money.MonetaryAmount;
import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;
import org.springframework.format.number.money.MonetaryAmountFormatter;

/**
 *
 * @author azrul
 */
public class MoneyConverter implements PropertyValueConverter<MonetaryAmount, String,ValueConversionContext<? extends PersistenceContext>> {


    @Override
    public MonetaryAmount read(String value, ValueConversionContext context) {
       MonetaryAmountFormatter formatter =  new MonetaryAmountFormatter();
       return formatter.parse(value, Locale.getDefault());
    }

    @Override
    public String write(MonetaryAmount value, ValueConversionContext context) {
        return value.toString();
    }

}
