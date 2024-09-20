/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;

import com.vaadin.flow.data.converter.StringToLongConverter;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author azrul
 */
public class StringToUngroupLongConverter extends StringToLongConverter {
    public StringToUngroupLongConverter(String message){
        super(message);
    }
    
    protected java.text.NumberFormat getFormat(Locale locale) {
        NumberFormat format = super.getFormat(locale);
        format.setGroupingUsed(false);
        return format;
    };
}
