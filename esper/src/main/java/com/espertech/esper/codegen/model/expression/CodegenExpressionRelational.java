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
package com.espertech.esper.codegen.model.expression;

import java.util.Map;
import java.util.Set;

public class CodegenExpressionRelational implements CodegenExpression {
    private final CodegenExpression lhs;
    private final CodegenRelational op;
    private final CodegenExpression rhs;

    public CodegenExpressionRelational(CodegenExpression lhs, CodegenRelational op, CodegenExpression rhs) {
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    public void render(StringBuilder builder, Map<Class, String> imports) {
        lhs.render(builder, imports);
        builder.append(op.getOp());
        rhs.render(builder, imports);
    }

    public void mergeClasses(Set<Class> classes) {
        lhs.mergeClasses(classes);
        rhs.mergeClasses(classes);
    }

    public enum CodegenRelational {
        GE(">="),
        GT(">"),
        LE("<="),
        LT("<");

        private final String op;

        CodegenRelational(String op) {
            this.op = op;
        }

        public String getOp() {
            return op;
        }
    }
}
