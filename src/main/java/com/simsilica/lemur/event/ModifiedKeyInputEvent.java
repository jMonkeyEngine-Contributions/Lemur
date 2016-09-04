/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
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

package com.simsilica.lemur.event;

import com.jme3.input.event.KeyInputEvent;

/**
 *  Extends the regular JME KeyInputEvent to support modifiers
 *  like shift, control, and alt.
 *
 *  @author    Paul Speed
 */
public class ModifiedKeyInputEvent extends KeyInputEvent {
    
    // Partly my preference would have been not to do it this way.
    // Extending KeyInputEvent is kind of ugly but it's the only way
    // I could incorporate key modifiers without breaking existing
    // apps that depend on the current interface.  Only really ugly
    // thing is that listeners that want to use this new event type
    // have to cast... but maybe we can migrate to dual listeners or
    // something later.

    private KeyInputEvent delegate;
    private int modifiers;
    
    public ModifiedKeyInputEvent( KeyInputEvent delegate, int modifiers ) {
        super(delegate.getKeyCode(), delegate.getKeyChar(), 
              delegate.isPressed(), delegate.isRepeating());
        this.delegate = delegate;
        this.modifiers = modifiers;                  
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public boolean hasModifiers( int mask ) {
        return KeyModifiers.hasModifiers(modifiers, mask);
    }
 
    /**
     *  Converts the values in this KeyInputEvent into a KeyAction.
     */
    public KeyAction toKeyAction() {
        // Extending KeyInputEvent does let us add nice things like this, 
        // though... so would have making our own non-JME event type.
        return new KeyAction(getKeyCode(), modifiers);     
    }     
 
    @Override   
    public void setConsumed() {
        // We want to consume the original event that JME delivered to us
        delegate.setConsumed();
    } 
}
