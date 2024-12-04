/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.script;

/**
 *
 * @author azrul
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


//import com.azrul.langkuik.framework.dao.query.DataQuery;
//import com.azrul.langkuik.framework.dao.DataAccessObject;
//import com.azrul.langkuik.framework.entity.Element;
//import com.azrul.langkuik.framework.entity.EntityUtils;
//import com.azrul.langkuik.framework.entity.WebEntityType;
//import com.azrul.langkuik.framework.factory.SpringBeanFactory;
//import com.azrul.langkuik.framework.user.UserProfile;
//import com.azrul.langkuik.framework.workflow.Workflow;
import com.azrul.chenook.domain.BizUser;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.workflow.model.BizProcess;
import java.util.Set;
//import javax.persistence.EntityManagerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author azrul
 */

public class Expression<R, T> {

//    @Lazy
//    @Autowired
//    Workflow workflow;
//
//    @Autowired
//    private EntityManagerFactory emf;

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public R evaluate(String exp, T workItem, BizUser user,BizProcess bizProcess) {
        Container<T> container = new Container();
        container.currentClass = (Class<T>) workItem.getClass();
        return eval(container, workItem,user,bizProcess, exp);
    }

 

    private <R, T> R eval(
            Container container, 
            T data, 
            BizUser user, 
            BizProcess bizProcess,
            String exp) throws ParseException, EvaluationException {
       
        container.current = data;
        if (data != null) {
            container.currentClass = data.getClass();
        }
        container.user = user;
        container.bizProcess = bizProcess;

        org.springframework.expression.Expression expression = expressionParser.parseExpression(exp);
        EvaluationContext context = new StandardEvaluationContext(container);
        return (R) expression.getValue(context);
    }

    class Container<T> { //capture data to be passed to expression

        public Class<T> currentClass;
        public T current;
        public BizUser user;
        public Set<String> roles;
        public BizProcess bizProcess;
    }
}

