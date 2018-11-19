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

import com.jme3.input.event.*;
import com.jme3.math.Vector2f;
import com.jme3.scene.*;
import com.simsilica.lemur.Command;


/**
 *  A MouseListener implementation that will track the hover
 *  state over entered and exited objects and Command&lt;Spatial&gt;
 *  when a 'hover' state exists over a particular Spatial that
 *  has this listener registered.
 *
 *  @author    Paul Speed
 */
public class HoverMouseListener<T extends Spatial> extends DefaultMouseListener {

    private Command<T> command; 
    private long hoverStart; 
    private double triggerTime;
    private Spatial lastSpatial; 
    private Vector2f lastPosition = new Vector2f();
    private MouseMotionEvent lastEvent;
    private boolean hoverSent;

    public HoverMouseListener(Command<T> command) {
        this(3.0, command);
    }
    
    public HoverMouseListener( double triggerTime, Command<T> command ) {
        this.triggerTime = triggerTime;
        this.command = command;
    } 

    @Override
    public void mouseButtonEvent( MouseButtonEvent event, Spatial target, Spatial capture ) {
        reset(event.getX(), event.getY(), target);
    }

    @Override
    public void mouseEntered( MouseMotionEvent event, Spatial target, Spatial capture ) {        
    }

    @Override
    public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture ) {
    }

    @Override
    public void mouseMoved( MouseMotionEvent event, Spatial target, Spatial capture ) {
 
        if( lastEvent == event )
            return;
        lastEvent = event;               
               
        if( lastSpatial != target ) {
            reset(event.getX(), event.getY(), target);
            return;
        }
        double x = event.getX() - lastPosition.x;
        double y = event.getY() - lastPosition.y;
        double dSq = x * x + y * y;
        if( dSq > 4 ) { 
            // 2 pixels radius
            reset(event.getX(), event.getY(), target);
            return;
        }
        
        if( hoverSent ) {
            return;
        }
        
        // Else check time
        long time = System.currentTimeMillis();
        if( (time - hoverStart) / 1000.0 > triggerTime ) {
            executeCommand(target);
            hoverSent = true;
        }        
    }
 
    // Simply to allow us to isolate the generics warning suppression
    @SuppressWarnings("unchecked")
    protected void executeCommand( Spatial target ) {
        command.execute((T)target);
    }
    
    protected void reset( float x, float y, Spatial s ) {
        hoverStart = System.currentTimeMillis();
        lastSpatial = s;
        lastPosition.set(x, y);
        if( hoverSent ) {
            // Send the reset
            command.execute(null);
        }
        hoverSent = false;            
    }
}

