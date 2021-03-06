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
package com.espertech.esper.core.support;

import com.espertech.esper.client.ConfigurationEngineDefaults;
import com.espertech.esper.epl.agg.factory.AggregationFactoryFactoryDefault;
import com.espertech.esper.epl.core.EngineImportServiceImpl;
import com.espertech.esper.epl.expression.time.TimeAbacusMilliseconds;

import java.util.TimeZone;

public class SupportEngineImportServiceFactory {

    public static EngineImportServiceImpl make() {
        ConfigurationEngineDefaults.CodeGeneration codeGeneration = new ConfigurationEngineDefaults.CodeGeneration();
        codeGeneration.setEnablePropertyGetter(false);
        codeGeneration.setEnableExpression(false);
        return new EngineImportServiceImpl(true, true, true, false, null, TimeZone.getDefault(), TimeAbacusMilliseconds.INSTANCE, ConfigurationEngineDefaults.ThreadingProfile.NORMAL, null, AggregationFactoryFactoryDefault.INSTANCE, codeGeneration, "default", null);
    }
}
