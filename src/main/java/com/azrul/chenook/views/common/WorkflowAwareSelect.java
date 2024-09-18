/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;


import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author azrul
 */
public class WorkflowAwareSelect<T> extends Select {
    public WorkflowAwareSelect(){}
    
    
    public static <T>WorkflowAwareSelect create(String fieldName,Binder<T> binder){
        T workItem = binder.getBean();
        var field = new WorkflowAwareSelect();
         List<Validator> validators = new ArrayList<>();
       var annotations = WorkflowUtils.getAnnotations(workItem, fieldName);
        if (annotations.containsKey(WorkField.class)) {
            WorkField wf = (WorkField) annotations.get(WorkField.class);
            field.setLabel(wf.displayName());
            if (wf.prefix().length > 0) {
                field.setPrefixComponent(new NativeLabel(wf.prefix()[0]));
            }
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
        var bindingBuilder = binder.forField(field);
        //bindingBuilder.withNullRepresentation("");
        for (var validator:validators){
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }
}
