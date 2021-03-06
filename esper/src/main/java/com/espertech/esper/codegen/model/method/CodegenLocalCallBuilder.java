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
package com.espertech.esper.codegen.model.method;

import com.espertech.esper.codegen.model.expression.CodegenExpression;

import java.util.ArrayList;
import java.util.List;

public class CodegenLocalCallBuilder {

    private final String methodName;
    private final List<CodegenPassSet> parameterSets = new ArrayList<>(2);

    public CodegenLocalCallBuilder(String methodName) {
        this.methodName = methodName;
    }

    public CodegenLocalCallBuilder passAll(CodegenParamSet params) {
        parameterSets.add(params.getPassAll());
        return this;
    }

    public CodegenLocalCallBuilder pass(CodegenExpression expression) {
        parameterSets.add(new CodegenPassSetSingle(expression));
        return this;
    }

    public CodegenExpression call() {
        return new CodegenExpressionLocalMethodParamSet(methodName, parameterSets);
    }
}
