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

import com.espertech.esper.epl.expression.core.ExprEnumerationForge;
import com.espertech.esper.epl.rettype.EPType;

public class ExprDotEnumerationSourceForge {
    private final EPType returnType;
    private final Integer streamOfProviderIfApplicable;
    private final ExprEnumerationForge enumeration;

    public ExprDotEnumerationSourceForge(EPType returnType, Integer streamOfProviderIfApplicable, ExprEnumerationForge enumeration) {
        this.returnType = returnType;
        this.streamOfProviderIfApplicable = streamOfProviderIfApplicable;
        this.enumeration = enumeration;
    }

    public ExprEnumerationForge getEnumeration() {
        return enumeration;
    }

    public EPType getReturnType() {
        return returnType;
    }

    public Integer getStreamOfProviderIfApplicable() {
        return streamOfProviderIfApplicable;
    }
}
