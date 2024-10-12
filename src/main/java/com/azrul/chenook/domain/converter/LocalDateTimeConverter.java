/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.converter;

import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.format.number.money.MonetaryAmountFormatter;
import org.springframework.data.elasticsearch.core.mapping.PropertyValueConverter;

/**
 *
 * @author azrul
 */
public class LocalDateTimeConverter implements PropertyValueConverter{


  

    @Override
    public Object write(Object value) {
         DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return ((LocalDateTime)value).format(formatter);
    }

    @Override
    public Object read(Object value) {
       DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
       return LocalDateTime.parse((String)value,formatter);
    }

}
