/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;

import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author azrul
 */
public class WorkflowAwareTextField<T> extends TextField {

    public WorkflowAwareTextField() {
    }

    public static <T> WorkflowAwareTextField create(String fieldName, Boolean editable, Binder<T> binder, Converter converter) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareTextField();
        List<Validator> validators = new ArrayList<>();

        var annoFieldDisplayMap = WorkflowUtils.getAnnotations(
                workItem.getClass(),
                fieldName
        );

        var workfieldMap = WorkflowUtils.applyWorkField(
                annoFieldDisplayMap,
                field
        );

        validators.addAll(
                WorkflowUtils.applyNotBlank(
                        annoFieldDisplayMap,
                        field,
                        workfieldMap,
                        fieldName
                )
        );

        validators.addAll(
                WorkflowUtils.applyMatcher(
                        annoFieldDisplayMap
                )
        );
        
        var bindingBuilder = binder.forField(field);
        bindingBuilder.withNullRepresentation("");
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        
        if (!editable) {
            if (converter != null) {
                bindingBuilder.withConverter(converter).bindReadOnly(fieldName);
            } else {
                bindingBuilder.bindReadOnly(fieldName);
            }
        } else {
            if (converter != null) {
                bindingBuilder.withConverter(converter).bind(fieldName);
            } else {
                bindingBuilder.bind(fieldName);
            }
        }

        return field;
    }

    public static <T> WorkflowAwareTextField create(String fieldName, Boolean readOnly, Binder<T> binder) {
        return create(fieldName, readOnly, binder, null);
    }

}
