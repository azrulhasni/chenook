/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.script;

import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.workflow.Workflow;
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
    ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("groovy");
    
  

    
    public <T extends WorkItem> void runScript(
            T work,
            BizUser user, 
            String script, 
            Workflow workflow) {
        if (script==null){
            return;
        }
        if (work==null){
            return;
        }
        try {
            

            scriptEngine.put("current", work);
            if (work != null) {
                scriptEngine.put("currentClass", work.getClass());
            }
            //scriptEngine.put("tenant", tenant);
            scriptEngine.put("user", user);
            //scriptEngine.put("dao", dao);
            scriptEngine.put("workflow", workflow);
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
