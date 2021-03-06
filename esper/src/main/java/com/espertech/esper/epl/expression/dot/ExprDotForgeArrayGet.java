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

import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.ExprForge;
import com.espertech.esper.epl.rettype.EPType;
import com.espertech.esper.epl.rettype.EPTypeHelper;

public class ExprDotForgeArrayGet implements ExprDotForge {
    private final EPType typeInfo;
    private final ExprForge indexExpression;

    public ExprDotForgeArrayGet(ExprForge index, Class componentType) {
        this.indexExpression = index;
        this.typeInfo = EPTypeHelper.singleValue(componentType);
    }

    public EPType getTypeInfo() {
        return typeInfo;
    }

    public void visit(ExprDotEvalVisitor visitor) {
        visitor.visitArraySingleItemSource();
    }

    public ExprDotEval getDotEvaluator() {
        return new ExprDotForgeArrayGetEval(this, indexExpression.getExprEvaluator());
    }

    public CodegenExpression codegen(CodegenExpression inner, Class innerType, CodegenContext context, CodegenParamSetExprPremade params) {
        return ExprDotForgeArrayGetEval.codegen(this, inner, innerType, context, params);
    }

    public ExprForge getIndexExpression() {
        return indexExpression;
    }
}
