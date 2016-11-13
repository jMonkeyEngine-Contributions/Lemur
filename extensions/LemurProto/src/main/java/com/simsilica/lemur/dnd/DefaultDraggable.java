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

import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;

/**
 *  A default draggable implementation that will simply move a spatial
 *  in a particular plane based on a starting location and an x/y delta.
 *
 *  @author    Paul Speed
 */
public class DefaultDraggable implements Draggable {
 
    private Vector2f start;   
    private Spatial spatial;
    private Vector3f origin;
    private Vector3f xAxis;
    private Vector3f yAxis;
    
    private Vector2f currentLocation;
 
    /**
     *  Creates a DefaultDraggable that will set the location of the spatial in
     *  3D space relative to the specified origin.
     */   
    public DefaultDraggable( Vector2f start, 
                             Spatial spatial, Vector3f origin, Vector3f xAxis, Vector3f yAxis ) {
        this.start = start.clone();
        this.spatial = spatial;
        this.origin = origin.clone();
        this.xAxis = xAxis.clone();
        this.yAxis = yAxis.clone();
        this.currentLocation = start.clone();
    }
 
    /**
     *  Creates a 3D draggable that will move the specified spatial relative to
     *  its current translation in a plane relative to the current viewport camera.
     */
    public DefaultDraggable( ViewPort view, Spatial spatial, Vector2f start ) {
        Camera cam = view.getCamera();
        Vector3f origin = spatial.getWorldTranslation();
        Vector3f screenPos = cam.getScreenCoordinates(origin);
        Vector2f xScreen = new Vector2f(screenPos.x + 1, screenPos.y);
        Vector2f yScreen = new Vector2f(screenPos.x, screenPos.y + 1);

        
        // Find the world location for one pixel right and one pixel up
        // in the plane of our object.
        Vector3f xWorld = cam.getWorldCoordinates(xScreen, screenPos.z);
        Vector3f yWorld = cam.getWorldCoordinates(yScreen, screenPos.z);

        this.start = start.clone();
        this.spatial = spatial;
        this.origin = origin.clone();
        this.xAxis = xWorld.subtractLocal(origin);
        this.yAxis = yWorld.subtractLocal(origin);
        this.currentLocation = start.clone();
    }
    
    public Spatial getSpatial() {
        return spatial;
    }
 
    protected void updateTranslation() {
        float x = currentLocation.x - start.x;
        float y = currentLocation.y - start.y;
        Vector3f loc = origin.add(xAxis.mult(x)).addLocal(yAxis.mult(y));

        // Translate it into parent relative space
        loc = spatial.getParent().worldToLocal(loc, loc);
                 
        spatial.setLocalTranslation(loc);
    }
    
    public void setLocation( float x, float y ) {
        currentLocation.set(x, y);
        updateTranslation();
    }
 
    public Vector2f getLocation() {
        return currentLocation;
    }
 
    public void updateDragStatus( DragStatus status ) {
    }
    
    public void release() {
        spatial.removeFromParent();
    }
}

