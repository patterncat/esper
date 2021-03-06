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
import com.espertech.esper.codegen.core.CodegenMember;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.event.EventAdapterService;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

/**
 * Base class for getters for a dynamic property (syntax field.inner?), caches methods to use for classes.
 */
public abstract class DynamicPropertyGetterBase implements BeanEventPropertyGetter {
    private final EventAdapterService eventAdapterService;
    private final CopyOnWriteArrayList<DynamicPropertyDescriptor> cache;
    private CodegenMember codegenCache;
    private CodegenMember codegenThis;
    private CodegenMember codegenEventAdapterService;

    /**
     * To be implemented to return the method required, or null to indicate an appropriate method could not be found.
     *
     * @param clazz to search for a matching method
     * @return method if found, or null if no matching method exists
     */
    protected abstract Method determineMethod(Class clazz);

    /**
     * Call the getter to obtains the return result object, or null if no such method exists.
     *
     * @param descriptor provides method information for the class
     * @param underlying is the underlying object to ask for the property value
     * @return underlying
     */
    protected abstract Object call(DynamicPropertyDescriptor descriptor, Object underlying);

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     * @param cache cache
     * @param getter getter
     * @param object object
     * @param eventAdapterService event server
     * @return property
     */
    public static Object cacheAndCall(CopyOnWriteArrayList<DynamicPropertyDescriptor> cache, DynamicPropertyGetterBase getter, Object object, EventAdapterService eventAdapterService) {
        DynamicPropertyDescriptor desc = getPopulateCache(cache, getter, object, eventAdapterService);
        if (desc.getMethod() == null) {
            return null;
        }
        return getter.call(desc, object);
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     * @param cache cache
     * @param getter getter
     * @param object object
     * @param eventAdapterService event server
     * @return exists-flag
     */
    public static boolean cacheAndExists(CopyOnWriteArrayList<DynamicPropertyDescriptor> cache, DynamicPropertyGetterBase getter, Object object, EventAdapterService eventAdapterService) {
        DynamicPropertyDescriptor desc = getPopulateCache(cache, getter, object, eventAdapterService);
        if (desc.getMethod() == null) {
            return false;
        }
        return true;
    }

    public DynamicPropertyGetterBase(EventAdapterService eventAdapterService) {
        this.cache = new CopyOnWriteArrayList<DynamicPropertyDescriptor>();
        this.eventAdapterService = eventAdapterService;
    }

    public Object getBeanProp(Object object) throws PropertyAccessException {
        return cacheAndCall(cache, this, object, eventAdapterService);
    }

    public Class getTargetType() {
        return Object.class;
    }

    public boolean isBeanExistsProperty(Object object) {
        return cacheAndExists(cache, this, object, eventAdapterService);
    }

    public final Object get(EventBean event) throws PropertyAccessException {
        return cacheAndCall(cache, this, event.getUnderlying(), eventAdapterService);
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return cacheAndExists(cache, this, eventBean.getUnderlying(), eventAdapterService);
    }

    public Class getBeanPropType() {
        return Object.class;
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingGetCodegen(exprDotUnderlying(beanExpression), context);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingExistsCodegen(exprDotUnderlying(beanExpression), context);
    }

    public CodegenExpression eventBeanFragmentCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingFragmentCodegen(exprDotUnderlying(beanExpression), context);
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        codegenMembers(context);
        return staticMethod(this.getClass(), "cacheAndCall", ref(codegenCache.getMemberName()), ref(codegenThis.getMemberName()), underlyingExpression, ref(codegenEventAdapterService.getMemberName()));
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        codegenMembers(context);
        return staticMethod(this.getClass(), "cacheAndExists", ref(codegenCache.getMemberName()), ref(codegenThis.getMemberName()), underlyingExpression, ref(codegenEventAdapterService.getMemberName()));
    }

    public CodegenExpression underlyingFragmentCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        codegenMembers(context);
        return staticMethod(BaseNativePropertyGetter.class, "getFragmentDynamic", underlyingGetCodegen(underlyingExpression, context), ref(codegenEventAdapterService.getMemberName()));
    }

    public Object getFragment(EventBean eventBean) {
        Object result = get(eventBean);
        return BaseNativePropertyGetter.getFragmentDynamic(result, eventAdapterService);
    }

    private static DynamicPropertyDescriptor getPopulateCache(CopyOnWriteArrayList<DynamicPropertyDescriptor> cache, DynamicPropertyGetterBase dynamicPropertyGetterBase, Object obj, EventAdapterService eventAdapterService) {
        // Check if the method is already there
        Class target = obj.getClass();
        for (DynamicPropertyDescriptor desc : cache) {
            if (desc.getClazz() == target) {
                return desc;
            }
        }

        // need to add it
        synchronized (dynamicPropertyGetterBase) {
            for (DynamicPropertyDescriptor desc : cache) {
                if (desc.getClazz() == target) {
                    return desc;
                }
            }

            // Lookup method to use
            Method method = dynamicPropertyGetterBase.determineMethod(target);

            // Cache descriptor and create fast method
            DynamicPropertyDescriptor propertyDescriptor;
            if (method == null) {
                propertyDescriptor = new DynamicPropertyDescriptor(target, null, false);
            } else {
                FastClass fastClass = FastClass.create(eventAdapterService.getEngineImportService().getFastClassClassLoader(target), target);
                FastMethod fastMethod = fastClass.getMethod(method);
                propertyDescriptor = new DynamicPropertyDescriptor(target, fastMethod, fastMethod.getParameterTypes().length > 0);
            }
            cache.add(propertyDescriptor);
            return propertyDescriptor;
        }
    }

    private void codegenMembers(CodegenContext context) {
        if (codegenCache == null) {
            codegenCache = context.makeMember(CopyOnWriteArrayList.class, DynamicPropertyDescriptor.class, cache);
            codegenThis = context.makeMember(DynamicPropertyGetterBase.class, this);
            codegenEventAdapterService = context.makeMember(EventAdapterService.class, eventAdapterService);
        }
        context.addMember(codegenCache);
        context.addMember(codegenThis);
        context.addMember(codegenEventAdapterService);
    }
}
