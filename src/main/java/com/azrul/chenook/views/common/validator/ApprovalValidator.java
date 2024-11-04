package com.azrul.chenook.views.common.validator;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.azrul.chenook.domain.Approval;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;

public class ApprovalValidator<T extends WorkItem> extends AbstractValidator<Set<Approval>> {
    private Binder<T> binder;
    private OidcUser user;

    public ApprovalValidator(Binder<T> binder, OidcUser user, String message) {
        super(message);
        this.binder = binder;
        this.user = user;
    }

    @Override
    public ValidationResult apply(Set<Approval> approvals, ValueContext vc) {
        
        if (WorkflowUtils.isWaitingApproval(binder.getBean(), user)) {
            Optional<Approval> oapproval = approvals
                .stream()
                .filter(a -> StringUtils.equals(user.getPreferredUsername(), a.getUsername()))
                .findAny();
            return oapproval.map(
                (Approval a)->{
                    if (a.getApproved() == null) {
                        return ValidationResult.error(this.getMessage(approvals));
                    } else {
                        return ValidationResult.ok();
                    }
                }
            ).orElseGet(()->ValidationResult.error(this.getMessage(approvals)));

            
        } else {
            return ValidationResult.ok();
        }
       
        
    }

   

   
}