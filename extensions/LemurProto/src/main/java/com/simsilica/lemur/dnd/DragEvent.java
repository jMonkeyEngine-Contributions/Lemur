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

package com.simsilica.lemur.dnd;

import com.google.common.base.MoreObjects;

import com.jme3.collision.CollisionResult;
import com.jme3.math.Vector2f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.event.AbstractCursorEvent;

/**
 *
 *
 *  @author    Paul Speed
 */
public class DragEvent {
    
    private DragSession session;
    private AbstractCursorEvent cursorEvent;
    private CollisionResult collision;
    
    public DragEvent( DragSession session, AbstractCursorEvent cursorEvent ) {
        this(session, cursorEvent, cursorEvent.getCollision());
    }
    
    public DragEvent( DragSession session, AbstractCursorEvent cursorEvent, 
                      CollisionResult collision ) {
        this.session = session;
        this.cursorEvent = cursorEvent;
        this.collision = collision;                      
    }
    
    public DragSession getSession() {
        return session;
    }
    
    public float getX() {
        return cursorEvent.getX();
    }
    
    public float getY() {
        return cursorEvent.getY();
    }
    
    public Vector2f getLocation() {
        return new Vector2f(getX(), getY());
    }
      
    public CollisionResult getCollision() {
        return collision;
    }
    
    public ViewPort getViewPort() {
        return cursorEvent.getViewPort(); 
    }
    
    public Spatial getTarget() {
        return cursorEvent.getTarget();
    }
 
    @Override   
    public String toString() {
        return MoreObjects.toStringHelper(getClass().getSimpleName())
            .add("session", session)
            .add("location", "[" + getX() + ", " + getY() + "]")
            .add("collision", getCollision())
            .add("viewPort", getViewPort())
            .add("target", getTarget())
            .toString(); 
    }
}

