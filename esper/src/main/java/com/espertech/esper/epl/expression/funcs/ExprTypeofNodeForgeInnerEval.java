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
package com.espertech.esper.epl.expression.funcs;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.core.ExprForgeComplexityEnum;
import com.espertech.esper.epl.expression.core.ExprNodeRenderable;
import com.espertech.esper.metrics.instrumentation.InstrumentationHelper;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class ExprTypeofNodeForgeInnerEval extends ExprTypeofNodeForge {

    private final ExprTypeofNode parent;

    public ExprTypeofNodeForgeInnerEval(ExprTypeofNode parent) {
        this.parent = parent;
    }

    public ExprEvaluator getExprEvaluator() {
        return new InnerEvaluator(parent.getChildNodes()[0].getForge().getExprEvaluator());
    }

    public CodegenExpression evaluateCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        String method = context.addMethod(String.class, ExprTypeofNodeForgeInnerEval.class).add(params).begin()
                .declareVar(Object.class, "result", parent.getChildNodes()[0].getForge().evaluateCodegen(params, context))
                .ifRefNullReturnNull("result")
                .methodReturn(exprDotMethodChain(ref("result")).add("getClass").add("getSimpleName"));
        return localMethodBuild(method).passAll(params).call();
    }

    public ExprNodeRenderable getForgeRenderable() {
        return parent;
    }

    public ExprForgeComplexityEnum getComplexity() {
        return ExprForgeComplexityEnum.INTER;
    }

    private static class InnerEvaluator implements ExprEvaluator {
        private final ExprEvaluator evaluator;

        InnerEvaluator(ExprEvaluator evaluator) {
            this.evaluator = evaluator;
        }

        @Override
        public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
            if (InstrumentationHelper.ENABLED) {
                InstrumentationHelper.get().qExprTypeof();
            }
            Object result = evaluator.evaluate(eventsPerStream, isNewData, context);
            if (result == null) {
                if (InstrumentationHelper.ENABLED) {
                    InstrumentationHelper.get().aExprTypeof(null);
                }
                return null;
            }
            if (InstrumentationHelper.ENABLED) {
                InstrumentationHelper.get().aExprTypeof(result.getClass().getSimpleName());
            }
            return result.getClass().getSimpleName();
        }
    }
}
