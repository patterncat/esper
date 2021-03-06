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
package com.espertech.esper.epl.expression.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;

public class ExprNodeUtilExprStreamNumEnumSingleForge implements ExprForge {
    private final ExprEnumerationForge enumeration;

    public ExprNodeUtilExprStreamNumEnumSingleForge(ExprEnumerationForge enumeration) {
        this.enumeration = enumeration;
    }

    public ExprEvaluator getExprEvaluator() {
        return new ExprNodeUtilExprStreamNumEnumSingleEval(enumeration.getExprEvaluatorEnumeration());
    }

    public CodegenExpression evaluateCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        return enumeration.evaluateGetEventBeanCodegen(params, context);
    }

    public ExprForgeComplexityEnum getComplexity() {
        return ExprForgeComplexityEnum.INTER;
    }

    public Class getEvaluationType() {
        return EventBean.class;
    }

    public ExprNodeRenderable getForgeRenderable() {
        return enumeration.getForgeRenderable();
    }
}
