/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @author azrul
 */
public class LocalDateTimeJsonDeSerializer  extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
        DateTimeFormatter format = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            
        String str = p.getText();
        try {
            return LocalDateTime.parse(str, format);
        } catch (DateTimeParseException e) {
            System.err.println(e);
            return null;
        }
    }
}