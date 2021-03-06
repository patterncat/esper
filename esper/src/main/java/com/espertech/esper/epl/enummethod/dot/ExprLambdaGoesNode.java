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
package com.espertech.esper.epl.enummethod.dot;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.*;

import java.io.StringWriter;
import java.util.List;

/**
 * Represents the case-when-then-else control flow function is an expression tree.
 */
public class ExprLambdaGoesNode extends ExprNodeBase implements ExprForge, ExprEvaluator, ExprDeclaredOrLambdaNode {
    private static final long serialVersionUID = 5551755641199945138L;
    private List<String> goesToNames;

    public ExprLambdaGoesNode(List<String> goesToNames) {
        this.goesToNames = goesToNames;
    }

    public boolean validated() {
        return true;
    }

    public List<String> getGoesToNames() {
        return goesToNames;
    }

    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public ExprNode validate(ExprValidationContext validationContext) throws ExprValidationException {
        throw new UnsupportedOperationException();
    }

    public boolean isConstantResult() {
        return false;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        throw new UnsupportedOperationException();
    }

    public CodegenExpression evaluateCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        throw new UnsupportedOperationException();
    }

    public ExprForgeComplexityEnum getComplexity() {
        return ExprForgeComplexityEnum.NOT_APPLICABLE;
    }

    public boolean equalsNode(ExprNode node, boolean ignoreStreamPrefix) {
        return false;
    }

    public void toPrecedenceFreeEPL(StringWriter writer) {
    }

    public ExprPrecedenceEnum getPrecedence() {
        return ExprPrecedenceEnum.MINIMUM;
    }

    public ExprForge getForge() {
        return this;
    }

    public Class getEvaluationType() {
        return null;
    }

    public ExprNodeRenderable getForgeRenderable() {
        return this;
    }
}


