/*
 * $Id$
 *
 * Copyright (c) 2012-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur.input;

import java.lang.reflect.*;


/**
 *  A convenience StateFunctionListener implementation that
 *  can call a method using reflection.  By default, the method
 *  is called on the "release" of a particular function, ie: when
 *  its state returns to Off.  Methods that take an InputState
 *  argument and are called by a StateMethodDelegate with the
 *  "takesArgument" parameter as 'true' will receive all events,
 *  POSTIVE, Negative, and Off.
 *
 *  @author    Paul Speed
 */
public class StateMethodDelegate implements StateFunctionListener {

    private Object target;
    private Method method;
    private boolean takesArgument;

    public StateMethodDelegate( Object target, String method ) {
        this(target, method, false);
    }

    public StateMethodDelegate( Object target, String method, boolean takesArgument ) {
        this.target = target;
        this.method = resolveMethod(target.getClass(), method, takesArgument);
        this.takesArgument = takesArgument;
    }

    public Object getTarget() {
        return target;
    }

    public String getMethodName() {
        return method.getName();
    }

    @SuppressWarnings("unchecked")
    protected static Method resolveMethod( Class targetClass, String name,
                                           boolean takesArgument ) {
        try {
            if( takesArgument ) {
                return targetClass.getMethod(name, InputState.class);
            } else {
                return targetClass.getMethod(name);
            }
        } catch( Exception e ) {
            throw new RuntimeException("Error resolving delegate method:" + name, e);
        }
    }

    protected void callMethod(InputState state) {
        try {
            if( takesArgument ) {
                method.invoke(target, state);
            } else {
                method.invoke(target);
            }
        } catch( Exception e ) {
            throw new RuntimeException("Error calling method:" + method, e);
        }
    }

    public void valueChanged( FunctionId func, InputState value, double tpf ) {
        // We only forward on the releases unless the "takesArgument" flag
        // is true and then we deliver everything
        if( takesArgument || value == InputState.Off ) {
            callMethod(value);
        }
    }
}
