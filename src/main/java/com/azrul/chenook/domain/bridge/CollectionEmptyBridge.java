/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.domain.bridge;

import java.util.Collection;
import java.util.Set;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 *
 * @author azrul
 */
public class CollectionEmptyBridge implements ValueBridge<Set, Boolean> {
    @Override
    public Boolean toIndexedValue(Set value, ValueBridgeToIndexedValueContext context) {
        return value == null || value.isEmpty();
    }
} 
