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
package com.espertech.esper.event.arr;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.event.BaseNestableEventUtil;
import com.espertech.esper.event.EventPropertyGetterSPI;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

/**
 * Getter for an array of event bean using a nested getter.
 */
public class ObjectArrayEventBeanArrayIndexedElementPropertyGetter implements ObjectArrayEventPropertyGetter {
    private final int propertyIndex;
    private final int index;
    private final EventPropertyGetterSPI nestedGetter;

    /**
     * Ctor.
     *
     * @param propertyIndex property index
     * @param index         array index
     * @param nestedGetter  nested getter
     */
    public ObjectArrayEventBeanArrayIndexedElementPropertyGetter(int propertyIndex, int index, EventPropertyGetterSPI nestedGetter) {
        this.propertyIndex = propertyIndex;
        this.index = index;
        this.nestedGetter = nestedGetter;
    }

    public Object getObjectArray(Object[] array) throws PropertyAccessException {
        // If the map does not contain the key, this is allowed and represented as null
        EventBean[] wrapper = (EventBean[]) array[propertyIndex];
        return BaseNestableEventUtil.getArrayPropertyValue(wrapper, index, nestedGetter);
    }

    private String getObjectArrayCodegen(CodegenContext context) {
        return context.addMethod(Object.class, this.getClass()).add(Object[].class, "array").begin()
                .declareVar(EventBean[].class, "wrapper", cast(EventBean[].class, arrayAtIndex(ref("array"), constant(propertyIndex))))
                .methodReturn(localMethod(BaseNestableEventUtil.getArrayPropertyValueCodegen(context, index, nestedGetter), ref("wrapper")));
    }

    public boolean isObjectArrayExistsProperty(Object[] array) {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Object get(EventBean obj) {
        return getObjectArray(BaseNestableEventUtil.checkedCastUnderlyingObjectArray(obj));
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Object getFragment(EventBean obj) {
        EventBean[] wrapper = (EventBean[]) BaseNestableEventUtil.checkedCastUnderlyingObjectArray(obj)[propertyIndex];
        return BaseNestableEventUtil.getArrayPropertyFragment(wrapper, index, nestedGetter);
    }

    private String getFragmentCodegen(CodegenContext context) {
        return context.addMethod(Object.class, this.getClass()).add(Object[].class, "array").begin()
                .declareVar(EventBean[].class, "wrapper", cast(EventBean[].class, arrayAtIndex(ref("array"), constant(propertyIndex))))
                .methodReturn(localMethod(BaseNestableEventUtil.getArrayPropertyFragmentCodegen(context, index, nestedGetter), ref("wrapper")));
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingGetCodegen(castUnderlying(Object[].class, beanExpression), context);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression eventBeanFragmentCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingFragmentCodegen(castUnderlying(Object[].class, beanExpression), context);
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return localMethod(getObjectArrayCodegen(context), underlyingExpression);
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression underlyingFragmentCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return localMethod(getFragmentCodegen(context), underlyingExpression);
    }
}