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
package com.espertech.esper.avro.getter;

import com.espertech.esper.avro.core.AvroEventPropertyGetter;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.codegen.core.CodegenBlock;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder;
import com.espertech.esper.event.EventPropertyGetterSPI;
import org.apache.avro.generic.GenericData;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class AvroEventBeanGetterNestedIndexRootedMultilevel implements EventPropertyGetterSPI {
    private final int posTop;
    private final int index;
    private final AvroEventPropertyGetter[] nested;

    public AvroEventBeanGetterNestedIndexRootedMultilevel(int posTop, int index, AvroEventPropertyGetter[] nested) {
        this.posTop = posTop;
        this.index = index;
        this.nested = nested;
    }

    public Object get(EventBean eventBean) throws PropertyAccessException {
        GenericData.Record value = navigate((GenericData.Record) eventBean.getUnderlying());
        if (value == null) {
            return null;
        }
        return nested[nested.length - 1].getAvroFieldValue(value);
    }

    private String getCodegen(CodegenContext context) {
        return context.addMethod(Object.class, this.getClass()).add(GenericData.Record.class, "record").begin()
                .declareVar(GenericData.Record.class, "value", localMethod(navigateMethodCodegen(context), ref("record")))
                .ifRefNullReturnNull("value")
                .methodReturn(nested[nested.length - 1].underlyingGetCodegen(ref("value"), context));
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return true;
    }

    public Object getFragment(EventBean eventBean) throws PropertyAccessException {
        GenericData.Record value = navigate((GenericData.Record) eventBean.getUnderlying());
        if (value == null) {
            return null;
        }
        return nested[nested.length - 1].getAvroFragment(value);
    }

    private String getFragmentCodegen(CodegenContext context) {
        return context.addMethod(Object.class, this.getClass()).add(GenericData.Record.class, "record").begin()
                .declareVar(GenericData.Record.class, "value", localMethod(navigateMethodCodegen(context), ref("record")))
                .ifRefNullReturnNull("value")
                .methodReturn(nested[nested.length - 1].underlyingFragmentCodegen(ref("value"), context));
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingGetCodegen(castUnderlying(GenericData.Record.class, beanExpression), context);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression eventBeanFragmentCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingFragmentCodegen(castUnderlying(GenericData.Record.class, beanExpression), context);
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return localMethod(getCodegen(context), underlyingExpression);
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression underlyingFragmentCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return localMethod(getFragmentCodegen(context), underlyingExpression);
    }

    private GenericData.Record navigate(GenericData.Record record) {
        Object value = AvroEventBeanGetterNestedIndexRooted.getAtIndex(record, posTop, index);
        if (value == null) {
            return null;
        }
        return navigateRecord((GenericData.Record) value);
    }

    private String navigateMethodCodegen(CodegenContext context) {
        String navigateRecordMethod = navigateRecordMethodCodegen(context);
        return context.addMethod(GenericData.Record.class, this.getClass()).add(GenericData.Record.class, "record").begin()
                .declareVar(Object.class, "value", staticMethod(AvroEventBeanGetterNestedIndexRooted.class, "getAtIndex", ref("record"), constant(posTop), constant(index)))
                .ifRefNullReturnNull("value")
                .methodReturn(CodegenExpressionBuilder.localMethod(navigateRecordMethod, castRef(GenericData.Record.class, "value")));
    }

    private GenericData.Record navigateRecord(GenericData.Record record) {
        GenericData.Record current = record;
        for (int i = 0; i < nested.length - 1; i++) {
            Object value = nested[i].getAvroFieldValue(current);
            if (!(value instanceof GenericData.Record)) {
                return null;
            }
            current = (GenericData.Record) value;
        }
        return current;
    }

    private String navigateRecordMethodCodegen(CodegenContext context) {
        CodegenBlock block = context.addMethod(GenericData.Record.class, this.getClass()).add(GenericData.Record.class, "record").begin()
                .declareVar(GenericData.Record.class, "current", ref("record"))
                .declareVarNull(Object.class, "value");
        for (int i = 0; i < nested.length - 1; i++) {
            block.assignRef("value", nested[i].underlyingGetCodegen(ref("current"), context))
                    .ifRefNotTypeReturnConst("value", GenericData.Record.class, null)
                    .assignRef("current", castRef(GenericData.Record.class, "value"));
        }
        return block.methodReturn(ref("current"));
    }
}
