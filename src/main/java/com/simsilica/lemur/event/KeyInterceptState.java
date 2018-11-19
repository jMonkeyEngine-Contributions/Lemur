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
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.util.SafeArrayList;


/**
 *  AppState that registers a RawInputListener with the
 *  InputManager so that key events can optionally be received
 *  and consumed before normal listeners get them.
 *
 *  @author    Paul Speed
 */
public class KeyInterceptState extends BaseAppState {

    private KeyObserver keyObserver = new KeyObserver();
    private int modifiers;

    private SafeArrayList<KeyListener> keyListeners
                            = new SafeArrayList<KeyListener>(KeyListener.class);

    public KeyInterceptState( Application app ) {
        setEnabled(true);

        // We do this as early as possible because we want to
        // make sure to be able to capture everything if we
        // are enabled.
        app.getInputManager().addRawInputListener(keyObserver);
    }

    public void addKeyListener( KeyListener l ) {
        keyListeners.add(l);
    }

    public void removeKeyListener( KeyListener l ) {
        keyListeners.remove(l);
    }

    @Override
    protected void initialize( Application app ) {
    }

    @Override
    protected void cleanup( Application app ) {
        app.getInputManager().removeRawInputListener(keyObserver);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    protected void setModifier( int mask, boolean on ) {
        if( on ) {
            modifiers = modifiers | mask;
        } else {
            modifiers = modifiers & ~mask; 
        }
    }

    protected void dispatch(KeyInputEvent evt) {
        if( !isEnabled() )
            return;
            
        // Intercept for key modifiers
        int code = evt.getKeyCode();
        if( code == KeyInput.KEY_LSHIFT || code == KeyInput.KEY_RSHIFT ) {
            setModifier(KeyModifiers.SHIFT_DOWN, evt.isPressed());
        }
        if( code == KeyInput.KEY_LCONTROL || code == KeyInput.KEY_RCONTROL ) {
            setModifier(KeyModifiers.CONTROL_DOWN, evt.isPressed());
        }        
        if( code == KeyInput.KEY_LMENU || code == KeyInput.KEY_RMENU ) {
            setModifier(KeyModifiers.ALT_DOWN, evt.isPressed());
        }        
            
        ModifiedKeyInputEvent wrapper = null;
        for( KeyListener l : keyListeners.getArray() ) {
            // Only wrap if we will actually deliver
            if( wrapper == null ) {
                wrapper = new ModifiedKeyInputEvent(evt, modifiers);
            }
            l.onKeyEvent(wrapper);
        }
    }

    protected class KeyObserver extends DefaultRawInputListener {

        @Override
        public void onKeyEvent( KeyInputEvent evt ) {
            dispatch(evt);
        }
    }
    
}
