/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.config.ApplicationContextHolder;
import com.azrul.chenook.domain.Attachment;
import com.azrul.chenook.domain.Status;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.BizUserService;
import com.azrul.chenook.service.BadgeUtils;
import com.azrul.chenook.service.WorkflowService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.Renderer;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class WorkflowPanel<T> extends FormLayout {


    @Autowired
    private BadgeUtils badgeUtils;

    
     @Autowired
    private WorkflowService workflowService;
    
    private ComboBox<String> cbApprove ;

    public WorkflowPanel(
            final WorkItem work,
            final OidcUser user,
            final Boolean editable,
            final Consumer<Attachment> onPostSave,
            final Consumer<Attachment> onPostRemove
    ) {
        ApplicationContextHolder.autowireBean(this);
        var fieldDisplayMap = WorkflowUtils.getFieldNameDisplayNameMap(work.getClass());
        Select<Status> cbStatus = createSelect(fieldDisplayMap.get("status"), editable);
        cbStatus.setItems(Status.values());
        cbStatus.setRenderer(badgeUtils.createStatusBadgeRenderer());
        if (work!=null){
            cbStatus.setValue(work.getStatus());
        }else{
            cbStatus.setValue(Status.NEWLY_CREATED);
        }
        this.add(cbStatus);
        
        HorizontalLayout apprrovalPanel = new HorizontalLayout();
        if (workflowService.isWaitingApproval(work) 
                && work.getApprovals().stream().filter(
                        a->StringUtils.equals(
                                a.getUsername(), 
                                user.getPreferredUsername()
                        )).count()>0){
            cbApprove = new ComboBox<>("Approval needed");
            cbApprove.setItems(Set.of("","APPROVE","REJECT"));
            cbApprove.setItemLabelGenerator(a->{
                if (StringUtils.equals(a, "REJECT")){
                    return "Reject";
                }else if  (StringUtils.equals(a, "APPROVE")){
                    return "Approve";
                }else{
                    return "";
                }
            });
            apprrovalPanel.add(cbApprove);
        }
        
        this.add(apprrovalPanel);
        //cbApprove.
        //this.parentId = parentId;
    }
    
    public void createApprovalInfoDialog(WorkItem work){
        
    }
    
    public Boolean validate(){
        if (cbApprove==null){
            return true;
        }
        
        if (StringUtils.isEmpty(cbApprove.getValue())){
            return false;
        }else{
            return true;
        }
    }
    
    public Boolean getApproval(){
         if (cbApprove==null){
            return null;
        }
        return "APPROVE".equals(cbApprove.getValue());
    }

    private <T> Select<T> createSelect(
            final String label,
            final boolean editable
    ) {
        Select<T> select = new Select<>();
        select.setLabel(label);
        select.setReadOnly(!editable);
        return select;
    }

//    public void moveWork(){
//         WorkItem work = workItemService.findOneByParentIdAndContext(parentId, context);
//         BizUser bizUser = bizUserService.getUser(oidcUser.getPreferredUsername());
//         workflowService.run(parent, work, bizUser, false, bizProcess);
//    }
}
