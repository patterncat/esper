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

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.blocks.CodegenLegoCast;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.*;
import com.espertech.esper.event.EventPropertyGetterSPI;

import java.io.StringWriter;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class SelectExprProcessorEvalByGetter implements ExprForge, ExprEvaluator, ExprNodeRenderable {
    private final int streamNum;
    private final EventPropertyGetterSPI getter;
    private final Class returnType;

    public SelectExprProcessorEvalByGetter(int streamNum, EventPropertyGetterSPI getter, Class returnType) {
        this.streamNum = streamNum;
        this.getter = getter;
        this.returnType = returnType;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        EventBean event = eventsPerStream[streamNum];
        if (event == null) {
            return null;
        }
        return getter.get(event);
    }

    public ExprForgeComplexityEnum getComplexity() {
        return ExprForgeComplexityEnum.SINGLE;
    }

    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public CodegenExpression evaluateCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        String method = context.addMethod(returnType, SelectExprProcessorEvalByGetter.class).add(params).begin()
                .declareVar(EventBean.class, "event", arrayAtIndex(params.passEPS(), constant(streamNum)))
                .ifRefNullReturnNull("event")
                .methodReturn(CodegenLegoCast.castSafeFromObjectType(returnType, getter.eventBeanGetCodegen(ref("event"), context)));
        return localMethodBuild(method).passAll(params).call();
    }

    public Class getEvaluationType() {
        return returnType;
    }

    public ExprNodeRenderable getForgeRenderable() {
        return this;
    }

    public void toEPL(StringWriter writer, ExprPrecedenceEnum parentPrecedence) {
        writer.append(this.getClass().getSimpleName());
    }
}
