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
package com.espertech.esper.epl.datetime.reformatop;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.codegen.core.CodegenBlock;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.core.CodegenMember;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.datetime.eval.DatetimeMethodEnum;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.core.ExprNode;
import com.espertech.esper.epl.expression.dot.ExprDotNodeFilterAnalyzerInput;
import com.espertech.esper.epl.expression.time.TimeAbacus;
import com.espertech.esper.epl.join.plan.FilterExprAnalyzerAffector;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class ReformatFormatForge implements ReformatForge, ReformatOp {

    private final DateFormat dateFormat;
    private final DateTimeFormatter dateTimeFormatter;
    private final TimeAbacus timeAbacus;

    public ReformatFormatForge(Object formatter, TimeAbacus timeAbacus) {
        if (formatter instanceof DateFormat) {
            dateFormat = (DateFormat) formatter;
            dateTimeFormatter = null;
        } else {
            dateFormat = null;
            dateTimeFormatter = (DateTimeFormatter) formatter;
        }
        this.timeAbacus = timeAbacus;
    }

    public ReformatOp getOp() {
        return this;
    }

    public synchronized Object evaluate(Long ts, EventBean[] eventsPerStream, boolean newData, ExprEvaluatorContext exprEvaluatorContext) {
        if (timeAbacus.getOneSecond() == 1000L) {
            return dateFormat.format(ts);
        }
        return dateFormat.format(timeAbacus.toDate(ts));
    }

    public CodegenExpression codegenLong(CodegenExpression inner, CodegenParamSetExprPremade params, CodegenContext context) {
        CodegenMember df = context.makeAddMember(DateFormat.class, dateFormat);
        CodegenBlock blockMethod = context.addMethod(String.class, ReformatFormatForge.class).add(long.class, "ts").begin();
        CodegenBlock syncBlock = blockMethod.synchronizedOn(ref(df.getMemberName()));
        if (timeAbacus.getOneSecond() == 1000L) {
            syncBlock.blockReturn(exprDotMethod(ref(df.getMemberName()), "format", ref("ts")));
        } else {
            syncBlock.blockReturn(exprDotMethod(ref(df.getMemberName()), "format", timeAbacus.toDateCodegen(ref("ts"))));
        }
        return localMethodBuild(blockMethod.methodEnd()).pass(inner).call();
    }

    public synchronized Object evaluate(Date d, EventBean[] eventsPerStream, boolean newData, ExprEvaluatorContext exprEvaluatorContext) {
        return dateFormat.format(d);
    }

    public CodegenExpression codegenDate(CodegenExpression inner, CodegenParamSetExprPremade params, CodegenContext context) {
        CodegenMember df = context.makeAddMember(DateFormat.class, dateFormat);
        CodegenBlock blockMethod = context.addMethod(String.class, ReformatFormatForge.class).add(Date.class, "d").begin()
                .synchronizedOn(ref(df.getMemberName()))
                .blockReturn(exprDotMethod(ref(df.getMemberName()), "format", ref("d")));
        return localMethodBuild(blockMethod.methodEnd()).pass(inner).call();
    }

    public synchronized Object evaluate(Calendar cal, EventBean[] eventsPerStream, boolean newData, ExprEvaluatorContext exprEvaluatorContext) {
        return dateFormat.format(cal.getTime());
    }

    public CodegenExpression codegenCal(CodegenExpression inner, CodegenParamSetExprPremade params, CodegenContext context) {
        CodegenMember df = context.makeAddMember(DateFormat.class, dateFormat);
        CodegenBlock blockMethod = context.addMethod(String.class, ReformatFormatForge.class).add(Calendar.class, "cal").begin()
                .synchronizedOn(ref(df.getMemberName()))
                .blockReturn(exprDotMethod(ref(df.getMemberName()), "format", exprDotMethod(ref("cal"), "getTime")));
        return localMethodBuild(blockMethod.methodEnd()).pass(inner).call();
    }

    public Object evaluate(LocalDateTime ldt, EventBean[] eventsPerStream, boolean newData, ExprEvaluatorContext exprEvaluatorContext) {
        return ldt.format(dateTimeFormatter);
    }

    public CodegenExpression codegenLDT(CodegenExpression inner, CodegenParamSetExprPremade params, CodegenContext context) {
        CodegenMember df = context.makeAddMember(DateTimeFormatter.class, dateTimeFormatter);
        return exprDotMethod(inner, "format", ref(df.getMemberName()));
    }

    public Object evaluate(ZonedDateTime zdt, EventBean[] eventsPerStream, boolean newData, ExprEvaluatorContext exprEvaluatorContext) {
        return zdt.format(dateTimeFormatter);
    }

    public CodegenExpression codegenZDT(CodegenExpression inner, CodegenParamSetExprPremade params, CodegenContext context) {
        CodegenMember df = context.makeAddMember(DateTimeFormatter.class, dateTimeFormatter);
        return exprDotMethod(inner, "format", ref(df.getMemberName()));
    }

    public Class getReturnType() {
        return String.class;
    }

    public FilterExprAnalyzerAffector getFilterDesc(EventType[] typesPerStream, DatetimeMethodEnum currentMethod, List<ExprNode> currentParameters, ExprDotNodeFilterAnalyzerInput inputDesc) {
        return null;
    }
}
