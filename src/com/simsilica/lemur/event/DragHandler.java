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

import com.jme3.input.MouseInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;


/**
 *  Work in progress.
 *
 *  @author    Paul Speed
 */
public class DragHandler extends DefaultMouseListener {

    private Vector2f drag = null;
    private Vector3f basePosition;
    private boolean consumeDrags = false;
    private boolean consumeDrops = false;

    public DragHandler() {
    }

    public boolean isDragging() {
        return drag != null;
    }

    protected void startDrag( MouseButtonEvent event, Spatial target, Spatial capture ) {
        drag = new Vector2f(event.getX(), event.getY());
        basePosition = capture.getWorldTranslation().clone();
        event.setConsumed();
    }

    protected void endDrag( MouseButtonEvent event, Spatial target, Spatial capture ) {
        if( consumeDrops )
            event.setConsumed();
        drag = null;
        basePosition = null;
    }

    @Override
    public void mouseButtonEvent( MouseButtonEvent event, Spatial target, Spatial capture ) {
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
    public void mouseMoved( MouseMotionEvent event, Spatial target, Spatial capture ) {
        if( drag == null || capture == null )
            return;

        ViewPort vp = GuiGlobals.getInstance().getCollisionViewPort( capture );
        Camera cam = vp.getCamera();

        if( consumeDrags ) {
            event.setConsumed();
        }

        // If it's an ortho camera then we'll assume 1:1 mapping
        // for now.
        if( cam.isParallelProjection() ) {
            Vector2f current = new Vector2f(event.getX(), event.getY());
            Vector2f delta = current.subtract(drag);
            capture.setLocalTranslation(basePosition.add(delta.x, delta.y, 0));
            return;
        }

        // Figure out how far away the center of the spatial is
        Vector3f pos = basePosition; //capture.getWorldTranslation();
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

        Vector3f local = capture.getParent().worldToLocal(newPos, null);
        capture.setLocalTranslation(local);
    }
}

