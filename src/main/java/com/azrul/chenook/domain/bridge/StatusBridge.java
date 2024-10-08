/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.bridge;

import com.azrul.chenook.domain.Status;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 *
 * @author azrul
 */
public class StatusBridge implements ValueBridge<Status, String> {
    @Override
    public String toIndexedValue(Status value, ValueBridgeToIndexedValueContext context) {
       return value.name();
    }
 
}