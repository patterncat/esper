/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.epl.expression.dot;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.core.CodegenBlock;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMember;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.codegen.model.statement.CodegenStatementTryCatch;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.epl.rettype.EPType;
import com.espertech.esper.util.JavaClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class ExprDotMethodForgeNoDuckEvalPlain implements ExprDotEval {
    private static final Logger log = LoggerFactory.getLogger(ExprDotMethodForgeNoDuckEvalPlain.class);

    protected final ExprDotMethodForgeNoDuck forge;
    private final ExprEvaluator[] parameters;

    ExprDotMethodForgeNoDuckEvalPlain(ExprDotMethodForgeNoDuck forge, ExprEvaluator[] parameters) {
        this.forge = forge;
        this.parameters = parameters;
    }

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        if (target == null) {
            return null;
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = parameters[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        }

        try {
            return forge.getMethod().invoke(target, args);
        } catch (InvocationTargetException e) {
            handleTargetException(forge.getStatementName(), forge.getMethod().getJavaMethod(), target.getClass().getName(), args, e.getTargetException());
        }
        return null;
    }

    public EPType getTypeInfo() {
        return forge.getTypeInfo();
    }

    public ExprDotForge getDotForge() {
        return forge;
    }

    public static CodegenExpression codegenPlain(ExprDotMethodForgeNoDuck forge, CodegenExpression inner, Class innerType, CodegenContext context, CodegenParamSetExprPremade params) {
        Class returnType = JavaClassHelper.getBoxedType(forge.getMethod().getReturnType());
        CodegenMember methodMember = context.makeAddMember(Method.class, forge.getMethod().getJavaMethod());
        CodegenBlock block = context.addMethod(returnType, ExprDotMethodForgeNoDuckEvalPlain.class).add(forge.getMethod().getDeclaringClass(), "target").add(params).begin();
        if (!innerType.isPrimitive() && returnType != void.class) {
            block.ifRefNullReturnNull("target");
        }
        CodegenExpression[] args = new CodegenExpression[forge.getParameters().length];
        for (int i = 0; i < forge.getParameters().length; i++) {
            String name = "p" + i;
            block.declareVar(forge.getParameters()[i].getEvaluationType(), name, forge.getParameters()[i].evaluateCodegen(params, context));
            args[i] = ref(name);
        }
        CodegenBlock tryBlock = block.tryCatch();
        CodegenExpression invocation = exprDotMethod(ref("target"), forge.getMethod().getName(), args);
        CodegenStatementTryCatch tryCatch;
        if (returnType == void.class) {
            tryCatch = tryBlock.expression(invocation).tryEnd();
        } else {
            tryCatch = tryBlock.tryReturn(invocation);
        }
        CodegenBlock catchBlock = tryCatch.addCatch(Throwable.class, "t");
        catchBlock.declareVar(Object[].class, "args", newArray(Object.class, constant(forge.getParameters().length)));
        for (int i = 0; i < forge.getParameters().length; i++) {
            catchBlock.assignArrayElement("args", constant(i), args[i]);
        }
        catchBlock.expression(staticMethod(ExprDotMethodForgeNoDuckEvalPlain.class, "handleTargetException", constant(forge.getStatementName()), ref(methodMember.getMemberName()),
                exprDotMethodChain(ref("target")).add("getClass").add("getName"), ref("args"), ref("t")));
        String method = returnType == void.class ? block.methodEnd() : block.methodReturn(constantNull());
        return localMethodBuild(method).pass(inner).passAll(params).call();
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param statementName   name
     * @param method          method
     * @param targetClassName target class name
     * @param args            args
     * @param t               throwable
     */
    public static void handleTargetException(String statementName, Method method, String targetClassName, Object[] args, Throwable t) {
        String message = JavaClassHelper.getMessageInvocationTarget(statementName, method, targetClassName, args, t);
        log.error(message, t);
    }
}
