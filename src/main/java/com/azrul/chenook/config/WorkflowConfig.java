/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.chenook.config;

import com.azrul.chenook.workflow.model.Activity;
import com.azrul.chenook.workflow.model.BizProcess;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
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

    @Value("${chenook.lgWorkflowAbsLocation}")
    String workflowLocation;

    @Value("${chenook.lgWorkflowFile}")
    String workflowFile;
    
    @Value("${chenook.lgAdminWorkflowAbsLocation}")
    String adminWorkflowLocation;

    @Value("${chenook.lgAdminWorkflowFile}")
    String adminWorkflowFile;
    
    @Value("${chenook.lgWorkflowXsdUrl}")
    String workflowXsdUrl;

//    @Bean
//    @Primary
//    @Qualifier("RootActivities")
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


//    @Bean
//    @Qualifier("RootBizProcess")
    public BizProcess rootBizProcess() {
        String wfLocation = workflowLocation;
        String wfFile = workflowFile;
        return getBizProcess(wfLocation, wfFile);
    }
    
     public BizProcess adminBizProcess() {
        String wfLocation = adminWorkflowLocation;
        String wfFile = adminWorkflowFile;
        return getBizProcess(wfLocation, wfFile);
    }

    private BizProcess getBizProcess(String wfLocation, String wfFile) {
        try {
            File file = loadFile(wfLocation, wfFile);
            File xsdfile = loadFile("", "workflow.xsd");

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
