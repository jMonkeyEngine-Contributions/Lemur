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

import com.jme3.collision.CollisionResult;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;


/**
 *  Abstract base class for events related to cursor interactions
 *  with spatials.
 *
 *  @author    Paul Speed
 */
public abstract class AbstractCursorEvent {

    private boolean consumed = false;
    
    private ViewPort view;
    private Spatial target;
    private float x;
    private float y;
    private CollisionResult collision; 

    protected AbstractCursorEvent( ViewPort view, Spatial target, float x, float y, 
                                   CollisionResult collision ) {
        this.view = view;
        this.target = target;                              
        this.x = x;
        this.y = y;
        this.collision = collision;                              
    }
    
    public ViewPort getViewPort() {
        return view;
    }
    
    public Spatial getTarget() {
        return target;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
 
    public Vector2f getLocation() {
        return new Vector2f(x, y);
    }
    
    public CollisionResult getCollision() {
        return collision;
    }
 
    public void setConsumed() {
        consumed = true;
    }
    
    public boolean isConsumed() {
        return consumed;
    }

    public Vector3f getRelativeViewCoordinates( Spatial relativeTo, Vector3f pos ) {
        // Calculate the world position relative to the spatial
        pos = relativeTo.localToWorld(pos, null);

        Camera cam = view.getCamera();
        if( cam.isParallelProjection() ) {
            return pos.clone();
        }

        return cam.getScreenCoordinates(pos);
    }
 
    protected String parmsToString() {
        return "x=" + x + ", y=" + y + ", target=" + target + ", view=" + view + ", collision=" + collision;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + parmsToString() + "]";
    }
}


