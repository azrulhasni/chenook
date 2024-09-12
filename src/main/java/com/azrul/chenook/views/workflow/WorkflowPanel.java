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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.Renderer;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 */
public class WorkflowPanel<T> extends FormLayout {


    @Autowired
    private BadgeUtils badgeUtils;

    @Autowired
    private BizUserService bizUserService;
    
    private ComboBox<String> cbApprove ;

    private static final String STATUS_LABEL = "Status";

    public WorkflowPanel(
            final WorkItem work,
            final Boolean editable,
            final Consumer<Attachment> onPostSave,
            final Consumer<Attachment> onPostRemove
    ) {
        ApplicationContextHolder.autowireBean(this);

        Select<Status> cbStatus = createSelect(STATUS_LABEL, editable);
        cbStatus.setItems(Status.values());
        cbStatus.setRenderer(badgeUtils.createStatusBadgeRenderer());
        if (work!=null){
            cbStatus.setValue(work.getStatus());
        }else{
            cbStatus.setValue(Status.NEWLY_CREATED);
        }
        this.add(cbStatus);
        
        cbApprove = new ComboBox<>("Approval needed");
        cbApprove.setItems(Set.of("","APPROVE","REJECT"));
        cbApprove.setItemLabelGenerator(a->"APPROVE".equals(a)?"Approve":"Reject");
        this.add(cbApprove);
        //cbApprove.
        //this.parentId = parentId;
    }
    
    public Boolean validate(){
        if (StringUtils.isEmpty(cbApprove.getValue())){
            return false;
        }else{
            return true;
        }
    }
    
    public Boolean getApproval(){
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
