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

import com.jme3.input.KeyNames;


/**
 *  Defines a key action including potential modifiers.
 *
 *  @author    Paul Speed
 */
public class KeyAction {

    /**
     *  @deprecated Use KeyModifiers.CONTROL_DOWN instead.
     */
    @Deprecated
    public static final int CONTROL_DOWN = KeyModifiers.CONTROL_DOWN;
    private static final KeyNames names = new KeyNames();

    private int keyCode;
    private int modifiers;

    public KeyAction( int keyCode, int... modifiers ) {
        this.keyCode = keyCode;
        int m = 0;
        for( int i : modifiers ) {
            m = m | i;
        }
        this.modifiers = m;
    }

    @Override
    public boolean equals( Object o ) {
        if( o == this )
            return true;
        if( o == null || o.getClass() != getClass() )
            return false;
        KeyAction other = (KeyAction)o;
        if( keyCode != other.keyCode )
            return false;
        if( modifiers != other.modifiers )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return keyCode << 8 | modifiers;
    }

    public int getModifiers() {
        return modifiers;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean hasModifier( int mod ) {
        return (modifiers & mod) == mod;
    }

    @Override
    public String toString() {
        String name = names.getName(keyCode);
        StringBuilder sb = new StringBuilder("KeyAction[");
        if( hasModifier(KeyModifiers.CONTROL_DOWN) ) {
            sb.append( "Control " );
        }
        if( hasModifier(KeyModifiers.SHIFT_DOWN) ) {
            sb.append( "Shift " );
        }
        sb.append(name);
        return sb.toString() + "]";
    }
}
