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
package com.espertech.esper.epl.enummethod.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.core.CodegenBlock;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMember;
import com.espertech.esper.codegen.model.blocks.CodegenLegoBooleanExpression;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetEnumMethodNonPremade;
import com.espertech.esper.codegen.model.method.CodegenParamSetEnumMethodPremade;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.event.arr.ObjectArrayEventBean;
import com.espertech.esper.event.arr.ObjectArrayEventType;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class EnumTakeWhileScalarForgeEval implements EnumEval {

    private final EnumTakeWhileScalarForge forge;
    private final ExprEvaluator innerExpression;

    public EnumTakeWhileScalarForgeEval(EnumTakeWhileScalarForge forge, ExprEvaluator innerExpression) {
        this.forge = forge;
        this.innerExpression = innerExpression;
    }

    public Object evaluateEnumMethod(EventBean[] eventsLambda, Collection enumcoll, boolean isNewData, ExprEvaluatorContext context) {
        if (enumcoll.isEmpty()) {
            return enumcoll;
        }

        ObjectArrayEventBean evalEvent = new ObjectArrayEventBean(new Object[1], forge.type);
        eventsLambda[forge.streamNumLambda] = evalEvent;
        Object[] props = evalEvent.getProperties();

        if (enumcoll.size() == 1) {
            Object item = enumcoll.iterator().next();
            props[0] = item;

            Object pass = innerExpression.evaluate(eventsLambda, isNewData, context);
            if (pass == null || (!(Boolean) pass)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(item);
        }

        ArrayDeque<Object> result = new ArrayDeque<Object>();

        for (Object next : enumcoll) {

            props[0] = next;

            Object pass = innerExpression.evaluate(eventsLambda, isNewData, context);
            if (pass == null || (!(Boolean) pass)) {
                break;
            }

            result.add(next);
        }

        return result;
    }

    public static CodegenExpression codegen(EnumTakeWhileScalarForge forge, CodegenParamSetEnumMethodNonPremade args, CodegenContext context) {
        CodegenMember typeMember = context.makeAddMember(ObjectArrayEventType.class, forge.type);
        CodegenParamSetEnumMethodPremade premade = CodegenParamSetEnumMethodPremade.INSTANCE;
        CodegenBlock block = context.addMethod(Collection.class, EnumTakeWhileScalarForgeEval.class).add(premade).begin()
                .ifCondition(exprDotMethod(premade.enumcoll(), "isEmpty"))
                .blockReturn(premade.enumcoll())
                .declareVar(ObjectArrayEventBean.class, "evalEvent", newInstance(ObjectArrayEventBean.class, newArray(Object.class, constant(1)), ref(typeMember.getMemberName())))
                .assignArrayElement(premade.eps(), constant(forge.streamNumLambda), ref("evalEvent"))
                .declareVar(Object[].class, "props", exprDotMethod(ref("evalEvent"), "getProperties"));

        CodegenBlock blockSingle = block.ifCondition(equalsIdentity(exprDotMethod(premade.enumcoll(), "size"), constant(1)))
                .declareVar(Object.class, "item", exprDotMethodChain(premade.enumcoll()).add("iterator").add("next"))
                .assignArrayElement("props", constant(0), ref("item"));
        CodegenLegoBooleanExpression.codegenReturnValueIfNullOrNotPass(blockSingle, forge.innerExpression, context, staticMethod(Collections.class, "emptyList"));
        blockSingle.blockReturn(staticMethod(Collections.class, "singletonList", ref("item")));

        block.declareVar(ArrayDeque.class, "result", newInstance(ArrayDeque.class));
        CodegenBlock forEach = block.forEach(Object.class, "next", premade.enumcoll())
                .assignArrayElement("props", constant(0), ref("next"));
        CodegenLegoBooleanExpression.codegenBreakIfNullOrNotPass(forEach, forge.innerExpression, context);
        forEach.expression(exprDotMethod(ref("result"), "add", ref("next")));
        String method = block.methodReturn(ref("result"));
        return localMethodBuild(method).passAll(args).call();
    }
}
