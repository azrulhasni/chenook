/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.chenook.workflow2.config;

import com.azrul.chenook.workflow.Workflow;
import com.azrul.chenook.workflow.model.Activity;
import com.azrul.chenook.workflow.model.BizProcess;
import com.azrul.chenook.workflow.WorkflowImpl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.xml.sax.SAXException;

/**
 *
 * @author azrul
 */
@Configuration
public class WorkflowConfig {

    @Value("${application.lgWorkflowAbsLocation}")
    String workflowLocation;

    @Value("${application.lgWorkflowFile}")
    String workflowFile;
    
    @Value("${application.lgWorkflowXsdUrl}")
    String workflowXsdUrl;

//    @Value("${application.lgRefMgmtWorkflowAbsLocation}")
//    String refWorkflowLocation;
//
//    @Value("${application.lgRefMgmtWorkflowFile}")
//    String refWorkflowFile;
    @Bean
    @Primary
    @Qualifier("RootActivities")
    public Map<String, Activity> activities(BizProcess rootBizProcess) {
        return getActivities(rootBizProcess);
    }

    private Map<String, Activity> getActivities(BizProcess bizProcess) {
        Map<String, Activity> activities = new HashMap<>();
        BizProcess.Workflow workflow = bizProcess.getWorkflow();
        for (Activity currentActivity : workflow.getStartEventOrServiceOrHuman()) {
            activities.put(currentActivity.getId(), currentActivity);
        }
        return activities;
    }

    @Bean
    @Primary
    @Qualifier("RootWorkflow")
    public Workflow workflow(BizProcess rootBizProcess) {
        return new WorkflowImpl(rootBizProcess);
    }

//    @Bean
//    @Qualifier("RefWorkflow")
//    public Workflow refWorkflow( BizProcess refBizProcess){
//        return new WorkflowImpl(refBizProcess);
//    }
    @Bean
    @Qualifier("RootBizProcess")
    public BizProcess rootBizProcess() {
        try {
            File file = loadFile(workflowLocation, workflowFile);
            File xsdfile = loadFile("", "workflow.xsd");

//            ObjectMapper objectMapper = new ObjectMapper();
//            BizProcess bizprocess = objectMapper.readValue(file, BizProcess.class);
            JAXBContext context = JAXBContext.newInstance(BizProcess.class);
            //Create Unmarshaller using JAXB context
            Unmarshaller unmarshaller = context.createUnmarshaller();

            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL xsdUrl = new URL(workflowXsdUrl);
            
            Schema workflowSchema = sf.newSchema(xsdfile);
            unmarshaller.setSchema(workflowSchema);

            BizProcess bizProcess = (BizProcess) unmarshaller.unmarshal(file);
            return bizProcess;
            //System.out.println(bizprocess.getWorkflow().get(0).getId());
        } catch (JAXBException | MalformedURLException | SAXException ex) {
            Logger.getLogger(WorkflowConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (new BizProcess()).withName("NONE");
    }

    private File loadFile(String wfLocation, String wfFile) {
        File file;
        if (!"".equals(wfLocation)) {
            file = new File(wfLocation + "/" + wfFile);
        } else {
            file = new File(this.getClass().getClassLoader().getResource(wfFile).getFile());
        }
        return file;
    }

}
