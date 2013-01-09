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

import com.jme3.input.event.*;
import com.jme3.math.*;
import com.jme3.renderer.*;
import com.jme3.scene.control.*;
import com.jme3.scene.Spatial;


/**
 *
 *  @author    Paul Speed
 */
public class MouseEventControl extends AbstractControl
{
    private List<MouseListener> listeners = new CopyOnWriteArrayList<MouseListener>();

    public MouseEventControl()
    {
    }

    public MouseEventControl( MouseListener... listeners )
    {
        this.listeners.addAll( Arrays.asList(listeners) );
    }

    public <T extends MouseListener> T getMouseListener( Class<T> type )
    {
        for( MouseListener l : listeners )
            {
            if( l.getClass() == type )
                return (T)l;
            }
        return null;
    }

    public boolean isEmpty()
    {
        return listeners.isEmpty();
    }

    public void addMouseListener( MouseListener l )
    {
        listeners.add(l);
    }
    
    public void removeMouseListener( MouseListener l )
    {
        listeners.remove(l);
    }

    public void mouseButtonEvent( MouseButtonEvent event, Spatial target, Spatial capture )
    {
        for( MouseListener l : listeners )
            l.mouseButtonEvent(event, target, capture);
    }
 
    public void mouseEntered( MouseMotionEvent event, Spatial target, Spatial capture )
    {
        for( MouseListener l : listeners )
            l.mouseEntered(event, target, capture);
    }

    public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture )
    {
        for( MouseListener l : listeners )
            l.mouseExited(event, target, capture);
    }
 
    public void mouseMoved( MouseMotionEvent event, Spatial target, Spatial capture )
    {
        for( MouseListener l : listeners )
            l.mouseMoved(event, target, capture);
    }
    
    protected void controlRender( RenderManager rm, ViewPort vp )
    {
    }
        
    protected void controlUpdate( float tpf )
    {
    }
}

