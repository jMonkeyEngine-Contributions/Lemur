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

import java.util.*;
import java.util.concurrent.*;

import com.jme3.renderer.*;
import com.jme3.scene.control.*;
import com.jme3.scene.Spatial;


/**
 *  A control that can be added to any Spatial to provide
 *  standard CursorListener/CursorEvent support.  The only requirement
 *  is that the Spatial must be somewhere in a hierarchy that has
 *  been provided to the MouseAppState or GuiGlobals class and that
 *  MouseAppState is active (either manually attached to the StateManager
 *  or done automatically by GuiGlobals.initialize()) or is otherwise
 *  a part of a PickEventSession.
 *
 *  @author    Paul Speed
 */
public class CursorEventControl extends AbstractControl {

    private List<CursorListener> listeners = new CopyOnWriteArrayList<CursorListener>();

    public CursorEventControl() {
    }

    public CursorEventControl( CursorListener... listeners ) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    /**
     *  Convenience method that will add a CursorEventControl if it
     *  doesn't exist, while adding the specified listeners.
     */
    public static void addListenersToSpatial( Spatial s, CursorListener... listeners ) {
        if( s == null ) {
            return;
        }
        CursorEventControl cec = s.getControl(CursorEventControl.class);
        if( cec == null ) {
            s.addControl(new CursorEventControl(listeners));
        } else {
            cec.listeners.addAll(Arrays.asList(listeners));
        }
    }

    /**
     *  Convenience method that will remove the specified listeners
     *  from a Spatial only if a CursorEventControl already exists.
     */
    public static void removeListenersFromSpatial( Spatial s, CursorListener... listeners ) {
        if( s == null ) {
            return;
        }
        CursorEventControl mec = s.getControl(CursorEventControl.class);        
        if( mec == null ) {
            return;
        } else {
            mec.listeners.removeAll(Arrays.asList(listeners));
        }
    }

    public <T extends CursorListener> T getMouseListener( Class<T> type ) {
        for( CursorListener l : listeners ) {
            if( l.getClass() == type ) {
                return type.cast(l);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public void addMouseListener( CursorListener l ) {
        listeners.add(l);
    }

    public void removeMouseListener( CursorListener l ) {
        listeners.remove(l);
    }

    public void cursorButtonEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
        for( CursorListener l : listeners ) {
            l.cursorButtonEvent(event, target, capture);
        }
    }

    public void cursorEntered( CursorMotionEvent event, Spatial target, Spatial capture ) {
        for( CursorListener l : listeners ) {
            l.cursorEntered(event, target, capture);
        }
    }

    public void cursorExited( CursorMotionEvent event, Spatial target, Spatial capture ) {
        for( CursorListener l : listeners ) {
            l.cursorExited(event, target, capture);
        }
    }

    public void cursorMoved( CursorMotionEvent event, Spatial target, Spatial capture ) {
        for( CursorListener l : listeners ) {
            l.cursorMoved(event, target, capture);
        }
    }

    @Override
    protected void controlRender( RenderManager rm, ViewPort vp ) {
    }

    @Override
    protected void controlUpdate( float tpf ) {
    }
}

