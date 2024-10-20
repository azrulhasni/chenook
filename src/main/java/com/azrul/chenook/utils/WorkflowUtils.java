package com.azrul.chenook.utils;

import com.azrul.chenook.annotation.DateTimeFormat;
import com.azrul.chenook.annotation.Matcher;
import com.azrul.chenook.annotation.MoneyRange;
import com.azrul.chenook.annotation.NotBlankValue;
import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.NumberRange;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.views.common.validator.MatcherValidator;
import com.azrul.chenook.views.common.validator.MoneyRangeValidator;
import com.azrul.chenook.views.common.validator.NumberRangeValidator;
import com.azrul.chenook.views.common.validator.PresenceValidator;
import com.azrul.chenook.views.workflow.WorkflowAwareBigDecimalField;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.RangeValidator;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author azrul
 */
public class WorkflowUtils {

//    public static <T, A extends Annotation> Optional<A> getAnnotation(Class<A> annoClass, T item, String fieldName) {
//
//        List<java.lang.reflect.Field> fields = new ArrayList<>();
//        Collections.addAll(fields, item.getClass().getDeclaredFields());
//        Collections.addAll(fields, item.getClass().getSuperclass().getDeclaredFields());
//        Optional<Field> ofield = fields.stream().filter(f -> f.getName().equals(fieldName)).findFirst();
//        Optional<A> oa = ofield.flatMap(field -> {
//            try {
//                var anno = field.getAnnotation(annoClass);
//                if (anno != null) {
//                    return Optional.of(anno);
//                } else {
//                    BeanInfo info = Introspector.getBeanInfo(item.getClass());
//                    Optional<PropertyDescriptor> property = Stream.of(info.getPropertyDescriptors()).filter(p -> StringUtils.equals(fieldName, p.getName())).findFirst();
//
//                    return property.map(p -> p.getReadMethod().getAnnotation(annoClass));
//                }
//            } catch (SecurityException | IntrospectionException ex) {
//                Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return Optional.empty();
//        });
//
//        return Optional.empty();
//    }
    public static <T> Field getField(Class<T> itemClass, String fieldName) {

        try {
            return itemClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException ex) {
            try {
                return itemClass.getSuperclass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException | SecurityException ex1) {
                Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return null;
    }

    public static <T> Map<Class<? extends Annotation>,Map<String, Object>> getAnnotations(Class<T> itemClass, String fieldName) {
        Map<Class<? extends Annotation>,Map<String, Object>> annotationValuesMap = new HashMap<>();
        try {

            //get annotations from field
            Optional<Field> ofield = Optional.ofNullable(
                    getField(itemClass, fieldName)
            );

            ofield.ifPresent(field -> {
                for (Annotation anno : field.getDeclaredAnnotations()) {
                    Map<String, Object> annotations = new HashMap<>();
                    Class annoClass = anno.annotationType();
                    for (Method annoMember : annoClass.getDeclaredMethods()) {
                        try {
                            var value = annoMember.invoke(anno, (Object[]) null);
                            annotations.put(annoMember.getName(), value);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    annotationValuesMap.put(annoClass, annotations);
                }
            });

            //get annotations from setter
            BeanInfo info = Introspector.getBeanInfo(itemClass);
            Optional<PropertyDescriptor> property = Stream.of(info.getPropertyDescriptors()).filter(p -> StringUtils.equals(fieldName, p.getName())).findFirst();
            property.ifPresent(p -> {
                for (var anno : p.getReadMethod().getDeclaredAnnotations()) {
                    Map<String, Object> annotations = new HashMap<>();
                    Class annoClass = anno.annotationType();
                    for (Method annoMember : annoClass.getDeclaredMethods()) {
                        try {
                            var value = annoMember.invoke(anno, (Object[]) null);
                            annotations.put(annoMember.getName(), value);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    annotationValuesMap.put(annoClass, annotations);
                }
            });
        } catch (SecurityException | IntrospectionException ex) {
            Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return annotationValuesMap;
    }
    
    
    public static <T> Map<Field, Map<String, Object>> getAnnotations(Class<? extends Annotation> annoClass, Class<T> itemClass) {
        return getAnnotations(annoClass, itemClass, Boolean.FALSE);
    }

    public static <T> Map<Field, Map<String, Object>> getAnnotations(Class<? extends Annotation> annoClass, Class<T> itemClass, Boolean excludeCollection) {

        List<java.lang.reflect.Field> fields = new ArrayList<>();
        Collections.addAll(fields, itemClass.getDeclaredFields());
        Collections.addAll(fields, itemClass.getSuperclass().getDeclaredFields());
        Map<Field, Map<String, Object>> result = new HashMap<>();

        for (Field field : fields) {
            if (excludeCollection && Collection.class.isAssignableFrom(field.getType())){
                continue;
            }
            try {
                Map<String, Object> annotations = new HashMap<>();
                for (Annotation anno : field.getDeclaredAnnotations()) {
                    if (anno.annotationType().equals(annoClass)) {
                        for (Method annoMember : annoClass.getDeclaredMethods()) {
                            try {
                                var value = annoMember.invoke(anno, (Object[]) null);
                                annotations.put(annoMember.getName(), value);
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }
                }

                BeanInfo info = Introspector.getBeanInfo(itemClass);
                for (PropertyDescriptor p : info.getPropertyDescriptors()) {
                    for (var anno : p.getReadMethod().getDeclaredAnnotations()) {
                        if (StringUtils.equals(p.getName(), field.getName())){
                            if (anno.annotationType().equals(annoClass)) {
                                for (Method annoMember : annoClass.getDeclaredMethods()) {
                                    try {
                                        var value = annoMember.invoke(anno, (Object[]) null);
                                        annotations.put(annoMember.getName(), value);
                                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                        Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!annotations.isEmpty()){
                    result.put(field, annotations);
                }
            } catch (IntrospectionException ex) {
                Logger.getLogger(WorkflowUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return result;
    }

//    public static <T> Map<Field, Annotation> getFieldAnnotationMap(Class annoClass, Class<T> itemClass) {
//        Map<Field, Map<Class, Annotation>> annosPerField = getAnnotations(annoClass, itemClass);
//
//        Map<Field, Annotation> results = new HashMap<>();
//        for (var fieldAnno : annosPerField.entrySet()) {
//            if (fieldAnno.getValue().containsKey(annoClass)) {
//                var anno = fieldAnno.getValue().get(annoClass);
//                results.put(fieldAnno.getKey(), anno);
//            }
//        }
//        return results;
//    }

    public static <T> Map<String, String> getSortableFields(Class<T> itemClass) {
        Map<Field,  Map<String, Object>> fieldAnnoMap = getAnnotations(WorkField.class, itemClass);
        return fieldAnnoMap.entrySet().stream()
                .filter(e -> {
                    return Boolean.TRUE.equals(e.getValue().get("sortable"));
                })
                .collect(
                        Collectors.toMap(
                                e -> {
                                    return e.getKey().getName();
                                },
                                e -> {
                                   return e.getValue().get("displayName")!=null?(String)e.getValue().get("displayName"):"";
                                })
                );
    }

    public static <T> Map<String, String> getFieldNameDisplayNameMap(Class<T> itemClass) {
       Map<Field,  Map<String, Object>> fieldAnnoMap = getAnnotations(WorkField.class, itemClass);
       
        return fieldAnnoMap.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                e -> {
                                    return e.getKey().getName();
                                },
                                e -> {
                                    return e.getValue().get("displayName")!=null?(String)e.getValue().get("displayName"):"";
                                })
                );
       
    }
    
//    public static <T> Set<String> getSearchFields(Class<T> itemClass) {
//       Set<String> fieldNames = new HashSet<>();
//       Map<Field,  Map<String, Object>> fieldAnnoMap = getAnnotations(FullTextField.class, itemClass,true);
//       fieldNames.addAll(
//               fieldAnnoMap
//                .entrySet()
//                .stream()
//                .filter(e->String.class.equals(e.getKey().getType()))
//                .map(e->e.getKey().getName())
//                .collect(Collectors.toSet()));
//       Map<Field,  Map<String, Object>> fieldAnnoMap2 = getAnnotations(KeywordField.class, itemClass,true);
//       fieldNames.addAll(
//               fieldAnnoMap2
//                .entrySet()
//                .stream()
//                .filter(e->String.class.equals(e.getKey().getType()))
//                .map(e->e.getKey().getName())
//                .collect(Collectors.toSet()));
//       Map<Field,  Map<String, Object>> fieldAnnoMap3 = getAnnotations(GenericField.class, itemClass,true);
//       fieldNames.addAll(
//               fieldAnnoMap3
//                .entrySet()
//                .stream()
//                 .filter(e->String.class.equals(e.getKey().getType()))
//                .map(e->e.getKey().getName())
//                .collect(Collectors.toSet()));
//       return fieldNames;
//    }
    
     public static List<Validator> applyMoneyRange(
            Map<Class<? extends Annotation>, Map<String, Object>> annoFieldDisplayMap, 
            Map<String, Object> workfieldMap, 
            String fieldName,
            String currencyCode
    ) {
        List<Validator> validators = new ArrayList<>();
        
        Map<String,Object> rangeMap = annoFieldDisplayMap.get(MoneyRange.class);
        if (rangeMap!=null) {
            if (((String[])rangeMap.get("message")).length>0){
                validators.add(new MoneyRangeValidator(((String[])rangeMap.get("message"))[0],currencyCode, ((long)rangeMap.get("min")), ((long)rangeMap.get("max"))));
            }else{
                if (workfieldMap!=null) {
                    validators.add(new MoneyRangeValidator("Field " + (String)workfieldMap.get("displayName") + " is out of range", currencyCode,((long)rangeMap.get("min")), ((long)rangeMap.get("max"))));
                } else {
                    validators.add(new MoneyRangeValidator("Field " + fieldName + " is out of range",currencyCode,((long)rangeMap.get("min")),((long)rangeMap.get("max"))));
                }
            }
        }
        return validators;
    }
     
     public static List<Validator> applyNumberRange(
            Map<Class<? extends Annotation>, Map<String, Object>> annoFieldDisplayMap, 
            Map<String, Object> workfieldMap, 
            String fieldName
            
    ) {
        List<Validator> validators = new ArrayList<>();
        
        Map<String,Object> rangeMap = annoFieldDisplayMap.get(NumberRange.class);
        if (rangeMap!=null) {
            if (((String[])rangeMap.get("message")).length>0){
                validators.add(new NumberRangeValidator(((String[])rangeMap.get("message"))[0], ((long)rangeMap.get("min")), ((long)rangeMap.get("max"))));
            }else{
                if (workfieldMap!=null) {
                    validators.add(new NumberRangeValidator("Field " + (String)workfieldMap.get("displayName") + " is out of range", ((long)rangeMap.get("min")), ((long)rangeMap.get("max"))));
                } else {
                    validators.add(new NumberRangeValidator("Field " + fieldName + " is out of range",((long)rangeMap.get("min")), ((long)rangeMap.get("max"))));
                }
            }
        }
        return validators;
    }
    
    public static List<Validator> applyMatcher(
            Map<Class<? extends Annotation>, Map<String, Object>> annoFieldDisplayMap
    ) {
        List<Validator> validators = new ArrayList<>();
        
        Map<String,Object> matcherMap = annoFieldDisplayMap.get(Matcher.class);
        if (matcherMap!=null) {
            if (((String[])matcherMap.get("message")).length>0){
                validators.add(new MatcherValidator(((String[])matcherMap.get("message"))[0],(String)matcherMap.get("regexp")));
            }else{
                validators.add(new MatcherValidator("Problem matching",(String)matcherMap.get("regexp")));
         
            }
        }
        return validators;
    }
    
    public static  List<Validator> applyNotBlank(
            Map<Class<? extends Annotation>, Map<String, Object>> annoFieldDisplayMap, 
            AbstractField field, 
            Map<String, Object> workfieldMap, 
            String fieldName
    ) {
         List<Validator> validators = new ArrayList<>();
       
        Map<String,Object> notblankMap = annoFieldDisplayMap.get(NotBlankValue.class);
        
        if (notblankMap!=null) {
            field.setRequiredIndicatorVisible(true);
            
            if (((String[])notblankMap.get("message")).length > 0) {
                validators.add(new PresenceValidator(((String[])notblankMap.get("message"))[0]));
            } else {
                if (workfieldMap!=null) {
                    validators.add(new PresenceValidator("Field " + (String)workfieldMap.get("displayName") + " is empty"));
                } else {
                    validators.add(new PresenceValidator("Field " + fieldName + " is empty"));
                }
            }
        }
        return validators;
    }

    public static  List<Validator> applyNotNull(
            Map<Class<? extends Annotation>, Map<String, Object>> annoFieldDisplayMap,
            Component field,  
            Map<String, Object> workfieldMap, 
            String fieldName
    ) {
         List<Validator> validators = new ArrayList<>();
       
        Map<String,Object> notnullMap = annoFieldDisplayMap.get(NotNullValue.class);
        
        if (notnullMap!=null) {
            if (HasValue.class.isAssignableFrom(field.getClass())){
                ((HasValue)field).setRequiredIndicatorVisible(true);
            }
            
            if (((String[])notnullMap.get("message")).length > 0) {
                validators.add(new PresenceValidator(((String[])notnullMap.get("message"))[0]));
            } else {
                if (workfieldMap!=null) {
                    validators.add(new PresenceValidator("Field " + (String)workfieldMap.get("displayName") + " is empty"));
                } else {
                    validators.add(new PresenceValidator("Field " + fieldName + " is empty"));
                }
            }
        }
        return validators;
    }

    public static Map<String, Object> applyWorkField(
            Map<Class<? extends Annotation>, Map<String, Object>> annoFieldDisplayMap, 
            Component field
    ) {
        Map<String,Object> workfieldMap = annoFieldDisplayMap.get(WorkField.class);
        if (workfieldMap!=null){
            if (HasLabel.class.isAssignableFrom(field.getClass())){
                ((HasLabel)field).setLabel((String)workfieldMap.get("displayName"));
            }
            
            if (HasPrefix.class.isAssignableFrom(field.getClass())){
                if (((String[])workfieldMap.get("prefix")).length > 0) {
                    ((HasPrefix)field).setPrefixComponent(new NativeLabel(((String[])workfieldMap.get("prefix"))[0]));
                }
            }
        }
        return workfieldMap;
    }
    
    public static void applyDateTimeFormat(Map<Class<? extends Annotation>, Map<String, Object>> annoFieldDisplayMap, DateTimePicker field){
        Map<String, Object> df = annoFieldDisplayMap.get(DateTimeFormat.class);
        if (df!=null){
            DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
            i18n.setDateFormat((String)df.get("format"));
            field.setDatePickerI18n(i18n);
        }
    }
}
