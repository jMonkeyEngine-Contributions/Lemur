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

package com.simsilica.lemur;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 *  A Command implementation that calls a configured
 *  no-argument method through reflection.
 *
 *  @author    Paul Speed
 */
public class MethodCommand<S> implements Command<S> {

    private Object delegate;
    private Method method;

    public MethodCommand( Object delegate, String methodName ) {
        this.delegate = delegate;

        try {
            this.method = delegate.getClass().getMethod(methodName);
        } catch( NoSuchMethodException e ) {
            throw new RuntimeException("Cannot find method:" + methodName + " on " + delegate.getClass(), e);
        }
    }

    public MethodCommand( Object delegate, Method method ) {
        this.delegate = delegate;
        this.method = method;
    }

    public void execute( S source ) {
        try {
            method.invoke(delegate);
        } catch( InvocationTargetException e ) {
            throw new RuntimeException("Error delegating to:" + method, e);
        } catch( IllegalAccessException e ) {
            throw new RuntimeException("Error delegating to:" + method, e);
        }
    }
}
