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
import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.localMethodBuild;
import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.ref;
import static com.espertech.esper.codegen.model.expression.CodegenExpressionRelational.CodegenRelational.GE;

public class EnumTakeWhileLastIndexEventsForgeEval implements EnumEval {

    private final EnumTakeWhileLastIndexEventsForge forge;
    private final ExprEvaluator innerExpression;

    public EnumTakeWhileLastIndexEventsForgeEval(EnumTakeWhileLastIndexEventsForge forge, ExprEvaluator innerExpression) {
        this.forge = forge;
        this.innerExpression = innerExpression;
    }

    public Object evaluateEnumMethod(EventBean[] eventsLambda, Collection enumcoll, boolean isNewData, ExprEvaluatorContext context) {
        if (enumcoll.isEmpty()) {
            return enumcoll;
        }

        ObjectArrayEventBean indexEvent = new ObjectArrayEventBean(new Object[1], forge.indexEventType);
        eventsLambda[forge.streamNumLambda + 1] = indexEvent;
        Object[] props = indexEvent.getProperties();

        Collection<EventBean> beans = (Collection<EventBean>) enumcoll;
        if (enumcoll.size() == 1) {
            EventBean item = beans.iterator().next();
            props[0] = 0;
            eventsLambda[forge.streamNumLambda] = item;

            Object pass = innerExpression.evaluate(eventsLambda, isNewData, context);
            if (pass == null || (!(Boolean) pass)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(item);
        }

        EventBean[] all = EnumTakeWhileLastEventsForgeEval.takeWhileLastEventBeanToArray(enumcoll);
        ArrayDeque<Object> result = new ArrayDeque<Object>();
        int index = 0;

        for (int i = all.length - 1; i >= 0; i--) {

            props[0] = index++;
            eventsLambda[forge.streamNumLambda] = all[i];

            Object pass = innerExpression.evaluate(eventsLambda, isNewData, context);
            if (pass == null || (!(Boolean) pass)) {
                break;
            }
            result.addFirst(all[i]);
        }

        return result;
    }

    public static CodegenExpression codegen(EnumTakeWhileLastIndexEventsForge forge, CodegenParamSetEnumMethodNonPremade args, CodegenContext context) {
        CodegenMember indexTypeMember = context.makeAddMember(ObjectArrayEventType.class, forge.indexEventType);
        CodegenParamSetEnumMethodPremade premade = CodegenParamSetEnumMethodPremade.INSTANCE;
        CodegenBlock block = context.addMethod(Collection.class, EnumTakeWhileLastIndexEventsForgeEval.class).add(premade).begin()
                .ifCondition(exprDotMethod(premade.enumcoll(), "isEmpty"))
                .blockReturn(premade.enumcoll())
                .declareVar(ObjectArrayEventBean.class, "indexEvent", newInstance(ObjectArrayEventBean.class, newArray(Object.class, constant(1)), ref(indexTypeMember.getMemberName())))
                .assignArrayElement(premade.eps(), constant(forge.streamNumLambda + 1), ref("indexEvent"))
                .declareVar(Object[].class, "props", exprDotMethod(ref("indexEvent"), "getProperties"));

        CodegenBlock blockSingle = block.ifCondition(equalsIdentity(exprDotMethod(premade.enumcoll(), "size"), constant(1)))
                .declareVar(EventBean.class, "item", cast(EventBean.class, exprDotMethodChain(premade.enumcoll()).add("iterator").add("next")))
                .assignArrayElement("props", constant(0), constant(0))
                .assignArrayElement(premade.eps(), constant(forge.streamNumLambda), ref("item"));
        CodegenLegoBooleanExpression.codegenReturnValueIfNullOrNotPass(blockSingle, forge.innerExpression, context, staticMethod(Collections.class, "emptyList"));
        blockSingle.blockReturn(staticMethod(Collections.class, "singletonList", ref("item")));

        block.declareVar(ArrayDeque.class, "result", newInstance(ArrayDeque.class))
                .declareVar(EventBean[].class, "all", staticMethod(EnumTakeWhileLastEventsForgeEval.class, "takeWhileLastEventBeanToArray", premade.enumcoll()))
                .declareVar(int.class, "index", constant(0));
        CodegenBlock forEach = block.forLoop(int.class, "i", op(arrayLength(ref("all")), "-", constant(1)), relational(ref("i"), GE, constant(0)), decrement("i"))
                .assignArrayElement("props", constant(0), increment("index"))
                .assignArrayElement(premade.eps(), constant(forge.streamNumLambda), arrayAtIndex(ref("all"), ref("i")));
        CodegenLegoBooleanExpression.codegenBreakIfNullOrNotPass(forEach, forge.innerExpression, context);
        forEach.expression(exprDotMethod(ref("result"), "addFirst", arrayAtIndex(ref("all"), ref("i"))));
        String method = block.methodReturn(ref("result"));
        return localMethodBuild(method).passAll(args).call();
    }
}
