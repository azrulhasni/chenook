package com.azrul.chenook.utils;

import com.azrul.chenook.annotation.DateTimeFormat;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.WorkflowService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author azrul
 */
public class WorkflowUtils {

    public static <T, A extends Annotation> Optional<A> getAnnotation(Class<A> annoClass, T item, String fieldName) {

        List<java.lang.reflect.Field> fields = new ArrayList<>();
        Collections.addAll(fields, item.getClass().getDeclaredFields());
        Collections.addAll(fields, item.getClass().getSuperclass().getDeclaredFields());
        Optional<Field> ofield = fields.stream().filter(f -> f.getName().equals(fieldName)).findFirst();
        Optional<A> oa = ofield.flatMap(field -> {
            try {
                var anno = field.getAnnotation(annoClass);
                if (anno != null) {
                    return Optional.of(anno);
                } else {
                    BeanInfo info = Introspector.getBeanInfo(item.getClass());
                    Optional<PropertyDescriptor> property = Stream.of(info.getPropertyDescriptors()).filter(p -> StringUtils.equals(fieldName, p.getName())).findFirst();

                    return property.map(p -> p.getReadMethod().getAnnotation(annoClass));
                }
            } catch (SecurityException | IntrospectionException ex) {
                Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            return Optional.empty();
        });

        return Optional.empty();
    }

    public static <T> Map<Class,Annotation> getAnnotations(T item, String fieldName) {

        List<java.lang.reflect.Field> fields = new ArrayList<>();
        Collections.addAll(fields, item.getClass().getDeclaredFields());
        Collections.addAll(fields, item.getClass().getSuperclass().getDeclaredFields());
        Optional<Field> ofield = fields.stream().filter(f -> f.getName().equals(fieldName)).findFirst();
        Map<Class,Annotation> annotations = new HashMap<>();
        ofield.ifPresent(field -> {
            try {
                for (Annotation anno : field.getDeclaredAnnotations()) {
                    annotations.put(anno.annotationType(),anno);
                }

                BeanInfo info = Introspector.getBeanInfo(item.getClass());
                Optional<PropertyDescriptor> property = Stream.of(info.getPropertyDescriptors()).filter(p -> StringUtils.equals(fieldName, p.getName())).findFirst();
                property.ifPresent(p -> {
                    for (var anno : p.getReadMethod().getDeclaredAnnotations()) {
                       annotations.put(anno.annotationType(),anno);
                    }
                });
            } catch (SecurityException | IntrospectionException ex) {
                Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        return annotations;
    }
}
