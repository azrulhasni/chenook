package com.azrul.chenook.views.common.validator;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;

public class NotEmptyValidator<T>  extends AbstractValidator<T> {


    public NotEmptyValidator(String message) {
        super(message);
    }

    @Override
    public ValidationResult apply(T t, ValueContext vc) {
        if (t == null) {
            return ValidationResult.error(this.getMessage(t));
        } else {
            if (t instanceof Collection c) {
                if (c.isEmpty()) {
                    return ValidationResult.error(this.getMessage(t));
                } else {
                    return ValidationResult.ok();
                }
            }else if (t instanceof Map m) {
                if (m.isEmpty()) {
                    return ValidationResult.error(this.getMessage(t));
                } else {
                    return ValidationResult.ok();
                }
            }
            return ValidationResult.ok();
        }
        
    }
}
