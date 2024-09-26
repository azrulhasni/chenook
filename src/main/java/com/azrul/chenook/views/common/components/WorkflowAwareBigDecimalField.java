/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import com.azrul.chenook.annotation.NumberRange;

/**
 *
 * @author azrul
 */
public class WorkflowAwareBigDecimalField<T> extends BigDecimalField {

    private WorkflowAwareBigDecimalField() {

    }

    public static <T> WorkflowAwareBigDecimalField create(String fieldName, Binder<T> binder) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareBigDecimalField();
        field.setId(fieldName);

        List<Validator> validators = new ArrayList<>();
        var annoFieldDisplayMap = WorkflowUtils.getAnnotations(
                workItem.getClass(), 
                fieldName);
        
        var workfieldMap = WorkflowUtils.applyWorkField(
                annoFieldDisplayMap, 
                field
        );
        
        validators.addAll(
                WorkflowUtils.applyNotNull(
                    annoFieldDisplayMap, 
                    field, 
                    workfieldMap, 
                    fieldName
                )
        );
        
        validators.addAll(
                WorkflowUtils.applyNumberRange(
                    annoFieldDisplayMap, 
                    workfieldMap, 
                    fieldName
                )
        );

        var bindingBuilder = binder.forField(field);
        
        bindingBuilder.withNullRepresentation(0);
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }

    
}
