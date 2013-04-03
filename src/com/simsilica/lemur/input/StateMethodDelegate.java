/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.input;

import java.lang.reflect.*;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class StateMethodDelegate implements StateFunctionListener
{
    private Object target;
    private Method method;
    private boolean takesArgument;
    
    public StateMethodDelegate( Object target, String method )
    {
        this(target, method, false);
    }
    
    public StateMethodDelegate( Object target, String method, boolean takesArgument )
    {
        this.target = target;
        this.method = resolveMethod(target.getClass(), method, takesArgument);
        this.takesArgument = takesArgument;
    }

    public Object getTarget()
    {
        return target;
    }
    
    public String getMethodName()
    {
        return method.getName();
    }

    protected static Method resolveMethod( Class targetClass, String name, boolean takesArgument )
    {
        try
            {
            if( takesArgument )
                return targetClass.getMethod( name, InputState.class );
            else
                return targetClass.getMethod( name );            
            }
        catch( Exception e )
            {
            throw new RuntimeException( "Error resolving delegate method:" + name, e );
            }
    }
    
    protected void callMethod(InputState state)
    {
        try
            {
            if( takesArgument )
                method.invoke(target, state);
            else 
                method.invoke(target);
            }
        catch( Exception e )
            {
            throw new RuntimeException( "Error calling method:" + method, e );
            }
    }

    public void valueChanged( FunctionId func, InputState value, double tpf )
    {
        // We only forward on the releases unless the "takesArgument" flag
        // is true and then we deliver everything
        if( takesArgument || value == InputState.OFF )
            callMethod(value);
    }
}
