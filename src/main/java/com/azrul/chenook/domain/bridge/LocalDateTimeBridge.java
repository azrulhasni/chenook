/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.bridge;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 *
 * @author azrul
 */
public class LocalDateTimeBridge  implements ValueBridge<LocalDateTime, String> {
    @Override
    public String toIndexedValue(LocalDateTime value, ValueBridgeToIndexedValueContext context) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm");
        return value.format(format);
    }
 
}
