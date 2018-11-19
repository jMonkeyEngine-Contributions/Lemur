/*
 * $Id$
 *
 * Copyright (c) 2012-2013 jMonkeyEngine
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

import com.google.common.base.Function;
import com.google.common.base.Functions;

import com.jme3.input.MouseInput;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial;


/**
 *  Work in progress.
 *
 *  @author    Paul Speed
 */
public class DragHandler extends DefaultCursorListener {

    private Vector2f drag = null;
    private Vector3f basePosition;
    private boolean consumeDrags = false;
    private boolean consumeDrops = false;

    private Function<Spatial, Spatial> draggableLocator = Functions.identity();

    public DragHandler() {
    }

    public DragHandler( Function<Spatial, Spatial> draggableLocator ) {
        this.draggableLocator = draggableLocator;
    }

    /**
     *  Sets the function that will be used to find the draggable spatial
     *  relative to the spatial that was clicked.  By default, this is the identity()
     *  function and will return the spatial that was clicked.
     */
    public void setDraggableLocator( Function<Spatial, Spatial> draggableLocator ) {
        this.draggableLocator = draggableLocator;
    }
    
    public Function<Spatial, Spatial> getDraggableLocator() {
        return draggableLocator;
    }

    public boolean isDragging() {
        return drag != null;
    }

    protected Vector2f getDragStartLocation() {
        return drag;
    }

    /**
     *  Finds the draggable spatial for the specified capture spatial.
     *  By default this just returns the capture  because the parentLocator 
     *  function is the identity function.  This can be overridden by specifying
     *  a different function or overriding this method.
     */
    protected Spatial findDraggable( Spatial capture ) {
        return draggableLocator.apply(capture);
    }

    protected void startDrag( CursorButtonEvent event, Spatial target, Spatial capture ) {
        drag = new Vector2f(event.getX(), event.getY());
        basePosition = findDraggable(capture).getWorldTranslation().clone();
        event.setConsumed();
    }

    protected void endDrag( CursorButtonEvent event, Spatial target, Spatial capture ) {
        if( consumeDrops )
            event.setConsumed();
        drag = null;
        basePosition = null;
    }

    @Override
    public void cursorButtonEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
        if( event.getButtonIndex() != MouseInput.BUTTON_LEFT )
            return;

        if( event.isPressed() ) {
            startDrag(event, target, capture);
        } else {
            // Dragging is done.
            // Only delegate the up events if we were dragging in the first place.
            if( drag != null ) {
                endDrag(event, target, capture);
            }
        }
    }

    @Override
    public void cursorMoved( CursorMotionEvent event, Spatial target, Spatial capture ) {
        if( drag == null || capture == null )
            return;

        ViewPort vp = event.getViewPort(); 
        Camera cam = vp.getCamera();

        if( consumeDrags ) {
            event.setConsumed();
        }

        // If it's an ortho camera then we'll assume 1:1 mapping
        // for now.
        if( cam.isParallelProjection() || capture.getQueueBucket() == Bucket.Gui ) {
            Vector2f current = new Vector2f(event.getX(), event.getY());
            Vector2f delta = current.subtract(drag);
 
            Spatial draggable = findDraggable(capture);
            
            // Make sure if Z has changed that it is applied to base
            basePosition.z = draggable.getWorldTranslation().z;
 
            // Convert the world position into local space
            Vector3f localPos = basePosition.add(delta.x, delta.y, 0);
            if( draggable.getParent() != null ) {
                localPos = draggable.getParent().worldToLocal(localPos, null);
            }
            
            draggable.setLocalTranslation(localPos);
            return;
        }

        // Figure out how far away the center of the spatial is
        Vector3f pos = basePosition; 
        Vector3f localPos = pos.subtract(cam.getLocation());
        float dist = cam.getDirection().dot(localPos);

        // Figure out what one "unit" up and down would be
        // at this distance.
        Vector3f v1 = cam.getScreenCoordinates(pos, null);
        Vector3f right = cam.getScreenCoordinates(pos.add(cam.getLeft().negate()), null);
        Vector3f up = cam.getScreenCoordinates(pos.add(cam.getUp()), null);

        Vector2f units = new Vector2f(right.x - v1.x, up.y - v1.y);

        // So... convert the actual screen movement to world space
        // movement along the camera plane.
        Vector2f current = new Vector2f(event.getX(), event.getY());
        Vector2f delta = current.subtract(drag);

        // Need to maintain the sign of the drag delta
        delta.x /= Math.abs(units.x);
        delta.y /= Math.abs(units.y);

        // Adjust the spatial's position accordingly
        Vector3f newPos = pos.add(cam.getLeft().mult(-delta.x));
        newPos.addLocal(cam.getUp().mult(delta.y));

        Spatial draggable = findDraggable(capture);
        Vector3f local = draggable.getParent().worldToLocal(newPos, null);
        draggable.setLocalTranslation(local);
    }
}

