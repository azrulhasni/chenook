/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;


import com.azrul.chenook.annotation.DateTimeFormat;
import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author azrul
 */
public class WorkflowAwareDateTimePicker<T> extends DateTimePicker{
    public WorkflowAwareDateTimePicker(){}
    
    public static <T> WorkflowAwareDateTimePicker create(String fieldName, Binder<T> binder) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareDateTimePicker();

        List<Validator> validators = new ArrayList<>();
        var annotations = WorkflowUtils.getAnnotations(workItem, fieldName);
        if (annotations.containsKey(WorkField.class)) {
            WorkField wf = (WorkField) annotations.get(WorkField.class);
            field.setLabel(wf.displayName());
        }
        if (annotations.containsKey(NotNullValue.class)) {
            NotNullValue nb = (NotNullValue) annotations.get(NotNullValue.class);
            field.setRequiredIndicatorVisible(true);

            if (nb.message().length > 0) {
                validators.add(new PresenceValidator(nb.message()[0]));
            } else {
                if (annotations.containsKey(WorkField.class)) {
                    WorkField wf = (WorkField) annotations.get(WorkField.class);
                    validators.add(new PresenceValidator("Field " + wf.displayName() + " is empty"));
                } else {
                    validators.add(new PresenceValidator("Field " + fieldName + " is empty"));
                }
            }
        }
        if (annotations.containsKey(DateTimeFormat.class)) {
            DateTimeFormat df = (DateTimeFormat) annotations.get(DateTimeFormat.class);
            DatePickerI18n i18n = new DatePickerI18n();
            i18n.setDateFormat(df.format());
            field.setDatePickerI18n(i18n);
        }
        var bindingBuilder = binder.forField(field);
        
        for (var validator:validators){
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }

}
