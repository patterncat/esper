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
package com.espertech.esper.event.bean;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.EventPropertyGetterAndIndexed;
import com.espertech.esper.event.vaevent.PropertyUtility;
import com.espertech.esper.util.JavaClassHelper;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.event.bean.IterableMethodPropertyGetter.getBeanEventIterableValue;

/**
 * Getter for a iterable property identified by a given index, using the CGLIB fast method.
 */
public class IterableFastPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter, EventPropertyGetterAndIndexed {
    private final FastMethod fastMethod;
    private final int index;

    /**
     * Constructor.
     *
     * @param method              the underlying method
     * @param fastMethod          is the method to use to retrieve a value from the object
     * @param index               is tge index within the array to get the property from
     * @param eventAdapterService factory for event beans and event types
     */
    public IterableFastPropertyGetter(Method method, FastMethod fastMethod, int index, EventAdapterService eventAdapterService) {
        super(eventAdapterService, JavaClassHelper.getGenericReturnType(method, false), null);
        this.index = index;
        this.fastMethod = fastMethod;

        if (index < 0) {
            throw new IllegalArgumentException("Invalid negative index value");
        }
    }

    public Object getBeanProp(Object object) throws PropertyAccessException {
        return getBeanProp(object, index);
    }

    public Object getBeanProp(Object object, int index) throws PropertyAccessException {
        try {
            Object value = fastMethod.invoke(object, null);
            return getBeanEventIterableValue(value, index);
        } catch (ClassCastException e) {
            throw PropertyUtility.getMismatchException(fastMethod.getJavaMethod(), object, e);
        } catch (InvocationTargetException e) {
            throw PropertyUtility.getInvocationTargetException(fastMethod.getJavaMethod(), e);
        }
    }

    public Object get(EventBean eventBean, int index) throws PropertyAccessException {
        return getBeanProp(eventBean.getUnderlying(), index);
    }

    public boolean isBeanExistsProperty(Object object) {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public final Object get(EventBean obj) throws PropertyAccessException {
        Object underlying = obj.getUnderlying();
        return getBeanProp(underlying);
    }

    public String toString() {
        return "ListFastPropertyGetter " +
                " fastMethod=" + fastMethod.toString() +
                " index=" + index;
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Class getBeanPropType() {
        return JavaClassHelper.getGenericReturnType(fastMethod.getJavaMethod(), false);
    }

    public Class getTargetType() {
        return fastMethod.getDeclaringClass();
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingGetCodegen(castUnderlying(getTargetType(), beanExpression), context);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return localMethod(IterableMethodPropertyGetter.getBeanPropCodegen(context, getBeanPropType(), getTargetType(), fastMethod.getJavaMethod()), underlyingExpression, constant(index));
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression eventBeanGetIndexedCodegen(CodegenContext context, CodegenExpression beanExpression, CodegenExpression key) {
        return localMethod(IterableMethodPropertyGetter.getBeanPropCodegen(context, getBeanPropType(), getTargetType(), fastMethod.getJavaMethod()), castUnderlying(getTargetType(), beanExpression), key);
    }
}
