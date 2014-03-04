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

package com.simsilica.lemur.focus;


import com.jme3.app.Application;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.focus.FocusTarget;


/**
 *  AppState that manages the focus transition between
 *  one FocusTarget and another.
 *
 *  @author    Paul Speed
 */
public class FocusManagerState extends BaseAppState {

    private Spatial focus;
    private FocusTarget focusTarget;

    public FocusManagerState() {
        setEnabled(true);
    }

    protected FocusTarget getFocusTarget( Spatial s ) {
        if( s == null )
            return null;

        for( int i = 0; i < s.getNumControls(); i++ ) {
            Control c = s.getControl(i);
            if( c instanceof FocusTarget )
                return (FocusTarget)c;
        }
        return null;
    }

    public void setFocus( Spatial focus ) {
        if( this.focus == focus )
            return;

        if( focusTarget != null && isEnabled() ) {
            // Lose focus from the old one.
            focusTarget.focusLost();
        }
        this.focus = focus;
        focusTarget = getFocusTarget(focus);

        if( focusTarget != null && isEnabled() ) {
            // Gain focus
            focusTarget.focusGained();
        }
    }

    public Spatial getFocus() {
        return focus;
    }

    @Override
    protected void initialize( Application app ) {
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        if( focusTarget != null ) {
            // Gain focus
            focusTarget.focusGained();
        }
    }

    @Override
    protected void disable() {
        if( focusTarget != null ) {
            // Lose focus
            focusTarget.focusLost();
        }
    }
}


