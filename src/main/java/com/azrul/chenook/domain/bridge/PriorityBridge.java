/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.bridge;

import com.azrul.chenook.domain.Priority;
import com.azrul.chenook.domain.Status;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 *
 * @author azrul
 */
public class PriorityBridge implements ValueBridge<Priority, String> {
    @Override
    public String toIndexedValue(Priority value, ValueBridgeToIndexedValueContext context) {
       return value.name();
    }
 
}