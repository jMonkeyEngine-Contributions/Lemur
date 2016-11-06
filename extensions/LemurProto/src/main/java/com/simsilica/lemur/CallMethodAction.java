/*
 * $Id$
 * 
 * Copyright (c) 2015, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *
 *  @author    Paul Speed
 */
public class CallMethodAction extends Action {

    static Logger log = LoggerFactory.getLogger(CallMethodAction.class);

    private String methodName;
    private Object object;
    private Method method;
    
    public CallMethodAction() {
    }

    public CallMethodAction( String name, Object delegate, String methodName ) {
        super(name);
        setMethod(delegate, methodName);
    }
    
    public CallMethodAction( Object delegate, String methodName ) {
        this(methodToName(methodName), delegate, methodName);
    }

    protected static String methodToName( String m ) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(m.charAt(0)));
        boolean last = true; 
        for( int i = 1; i < m.length(); i++ ) {
            char c = m.charAt(i);
            boolean upper = Character.isUpperCase(c);
            if( upper && !last ) {
                sb.append(' ');                
            }
            sb.append(c);
            last = upper;
        }
        return sb.toString();   
    }

    protected boolean isValidArgument( Class type ) {
        return Button.class.isAssignableFrom(type)
               || Action.class.isAssignableFrom(type);
    }
    
    protected boolean isValidArgumentList( Class[] types ) {
        if( log.isTraceEnabled() ) {
            log.trace("isValidArgumentList(" + java.util.Arrays.asList(types) + ")");
        }
        for( Class c : types ) {
            if( !isValidArgument(c) ) {
                if( log.isTraceEnabled() ) {
                    log.trace("isValidArgument(" + c + ") = false");
                }
                return false;
            } else {
                if( log.isTraceEnabled() ) {
                    log.trace("isValidArgument(" + c + ") = true");
                }
            }
        }
        return true;
    }

    protected Object toParm( Button source, Class type ) {
        if( Button.class.isAssignableFrom(type) ) {
            return source;
        }
        if( Action.class.isAssignableFrom(type) ) {
            return this;
        }
        return null;
    }    

    protected void findMethod() {
        if( object == null || methodName == null ) {
            return;
        }
        // It's not a straight lookup because we support
        // protected methods and methods that take some arguments
        // like the action or the button.
        Class type = object.getClass();
        findMethod(type);
    }        
        
    protected void findMethod( Class type ) {
        log.trace("Finding method:" + methodName + " on:" + type);        
        for( Method m : type.getDeclaredMethods() ) {
            log.trace("Checking method:" + m); 
            if( !methodName.equals(m.getName()) ) {
                continue;
            }
            Class[] parms = m.getParameterTypes();
            if( parms.length > 2 ) {
                continue;
            }
            if( !isValidArgumentList(parms) ) {
                continue;
            }
 
            // Else it matches           
            this.method = m;
            
            // Make sure we can call it
            if( !m.isAccessible() ) {
                m.setAccessible(true);
            }
            
            break;
        }
        if( method == null && type != Object.class ) {
            findMethod(type.getSuperclass());
        } 
        log.trace("Found:" + method);        
        if( this.method == null ) {
            throw new RuntimeException("Method not found for:" + methodName + " on type:" + type);
        }        
    }    
    
    public void setMethod( Object object, String methodName ) {
        if( Objects.equals(this.object, object) && Objects.equals(this.methodName, methodName) ) {
            return;
        }
        this.object = object;
        this.methodName = methodName;
        findMethod(); 
        incrementVersion();       
    }
 
    public String getMethodName() {
        return methodName;
    }
    
    public Object getDelegate() {
        return object;
    }   

    @Override
    public void execute( Button source ) {
        if( method == null ) {
            throw new RuntimeException("No method specified.");
        }
        Class[] parmTypes = method.getParameterTypes(); 
        Object[] args = new Object[parmTypes.length];
        for( int i = 0; i < args.length; i++ ) {
            args[i] = toParm(source, parmTypes[i]);
        }
        try {
            method.invoke(object, args);
        } catch( IllegalArgumentException e ) {
            // The JDK gives basically no info so it's up to us.
            StringBuilder sb = new StringBuilder();
            for( int i = 0; i < args.length; i++ ) {
                if( sb.length() > 0 ) {
                    sb.append(", ");
                }
                sb.append("(" + parmTypes[i] + ")");
                sb.append(args[i]);
            }
            throw new RuntimeException("Error calling:" + method + " with parameters [" + sb + "]", e); 
        } catch( IllegalAccessException | InvocationTargetException e ) {
            throw new RuntimeException("Error invoking action method:" + methodName, e);
        }
    }
    
    @Override
    protected void appendFields( StringBuilder sb ) {
        super.appendFields(sb);
        sb.append(", methodName=").append(methodName);
        sb.append(", object=").append(object);
    }    
}
