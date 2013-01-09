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

package com.simsilica.lemur.event;


import com.jme3.app.Application;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.util.SafeArrayList;


/**
 *  AppState that registers a RawInputListener with the
 *  InputManager so that key events can optionally be received
 *  and consumed before normal listeners get them.
 *
 *  @author    Paul Speed
 */
public class KeyInterceptState extends BaseAppState
{
    private KeyObserver keyObserver = new KeyObserver();
 
    private SafeArrayList<KeyListener> keyListeners = new SafeArrayList<KeyListener>(KeyListener.class);

    public KeyInterceptState()
    {
        setEnabled(true);
    }
    
    public void addKeyListener( KeyListener l )
    {
        keyListeners.add(l);
    }
    
    public void removeKeyListener( KeyListener l ) 
    {
        keyListeners.remove(l);
    }

    protected void initialize( Application app )
    {
        // We do this as early as possible because we want to
        // make sure to be able to capture everything if we
        // are enabled.
        app.getInputManager().addRawInputListener(keyObserver);
    }
    
    protected void cleanup( Application app )
    {
        app.getInputManager().removeRawInputListener(keyObserver);
    }
    
    protected void enable()
    {
    }
    
    protected void disable()
    {
    }    

    protected void dispatch(KeyInputEvent evt)
    {
        if( !isEnabled() )
            return;
        for( KeyListener l : keyListeners.getArray() )
            l.onKeyEvent(evt); 
    } 

    protected class KeyObserver extends DefaultRawInputListener
    {
        @Override
        public void onKeyEvent(KeyInputEvent evt)
        {
            dispatch(evt);
        }    
    } 
}
