/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.converter;

import jakarta.persistence.PersistenceContext;
import java.util.Locale;
import javax.money.MonetaryAmount;
import org.springframework.format.number.money.MonetaryAmountFormatter;
import org.springframework.data.elasticsearch.core.mapping.PropertyValueConverter;

/**
 *
 * @author azrul
 */
public class MoneyConverter implements PropertyValueConverter{


  

    @Override
    public Object write(Object value) {
        return value.toString();
    }

    @Override
    public Object read(Object value) {
        MonetaryAmountFormatter formatter =  new MonetaryAmountFormatter();
       return formatter.parse((String) value, Locale.getDefault());
    }

}
