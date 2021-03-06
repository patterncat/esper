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
package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventType;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.*;
import com.espertech.esper.event.EventBeanManufacturer;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.TypeWidener;

import java.io.StringWriter;

public class SelectExprProcessorTypableSingleForge implements ExprForge, ExprNodeRenderable {
    protected final ExprTypableReturnForge typable;
    protected final boolean hasWideners;
    protected final TypeWidener[] wideners;
    protected final EventBeanManufacturer factory;
    protected final EventType targetType;
    protected final boolean singleRowOnly;

    public SelectExprProcessorTypableSingleForge(ExprTypableReturnForge typable, boolean hasWideners, TypeWidener[] wideners, EventBeanManufacturer factory, EventType targetType, boolean singleRowOnly) {
        this.typable = typable;
        this.hasWideners = hasWideners;
        this.wideners = wideners;
        this.factory = factory;
        this.targetType = targetType;
        this.singleRowOnly = singleRowOnly;
    }

    public ExprEvaluator getExprEvaluator() {
        if (singleRowOnly) {
            return new SelectExprProcessorTypableSingleEvalSingleRow(this, typable.getTypableReturnEvaluator());
        }
        return new SelectExprProcessorTypableSingleEval(this, typable.getTypableReturnEvaluator());
    }

    public CodegenExpression evaluateCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        if (singleRowOnly) {
            return SelectExprProcessorTypableSingleEvalSingleRow.codegen(this, params, context);
        }
        return SelectExprProcessorTypableSingleEval.codegen(this, params, context);
    }

    public ExprForgeComplexityEnum getComplexity() {
        return ExprForgeComplexityEnum.INTER;
    }

    public Class getEvaluationType() {
        if (singleRowOnly) {
            return targetType.getUnderlyingType();
        }
        return JavaClassHelper.getArrayType(targetType.getUnderlyingType());
    }

    public void toEPL(StringWriter writer, ExprPrecedenceEnum parentPrecedence) {
        typable.getForgeRenderable().toEPL(writer, parentPrecedence);
    }

    public ExprNodeRenderable getForgeRenderable() {
        return this;
    }
}
