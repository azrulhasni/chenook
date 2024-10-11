/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nimbusds.jwt.util.DateUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @author azrul
 */
public class LocalDateTimeJsonSerializer  extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
        try {
            DateTimeFormatter format = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            String s = value.format(format);
            gen.writeString(s);
        } catch (DateTimeParseException e) {
            System.err.println(e);
            gen.writeString("");
        }
    }
}