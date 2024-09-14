/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.script;

import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.workflow.model.BizProcess;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 *
 * @author azrul
 */
@Service
public class Scripting /*implements Scripting*/ {
    private static ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("groovy");
    
  

    
    public <T> void runScript(
            T workItem,
            BizUser user, 
            String script, 
            BizProcess bizProcess) {
        if (script==null){
            return;
        }
        
         if (scriptEngine==null){
            scriptEngine = new ScriptEngineManager().getEngineByName("groovy");
        }
        
        try {
            

           
            //scriptEngine.put("tenant", tenant);
            scriptEngine.put("current", workItem);
            if (workItem!= null) {
                scriptEngine.put("currentClass", workItem.getClass());
            }
            scriptEngine.put("user", user);
            //scriptEngine.put("dao", dao);
            scriptEngine.put("bizProcess",bizProcess);
//            scriptEngine.put("dataQuery", SpringBeanFactory.create(DataQuery.class));
//            Map<String, Class> REF = new HashMap<>();
//            EntityUtils entityUtils = SpringBeanFactory.create(EntityUtils.class);
//            for (Class c : entityUtils.getAllEntities(emf)) {
//                if (entityUtils.getEntityType(c) == WebEntityType.REF) {
//                    REF.put(c.getSimpleName(), c);
//                }
//            }
//            scriptEngine.put("REF",REF);

            scriptEngine.eval(script);
        } catch (ScriptException ex) {
            Logger.getLogger(Scripting.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
