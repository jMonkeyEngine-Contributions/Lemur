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

import com.jme3.app.Application;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;


/**
 *
 *  @author    Paul Speed
 */
public class MouseAppState extends BaseAppState {

    private boolean includeDefaultNodes = true;
    private MouseObserver mouseObserver = new MouseObserver();
    
    private long sampleFrequency = 1000000000 / 60; // 60 fps
    private long lastSample = 0;

    /**
     *  The session that tracks the state of pick events from one
     *  event frame to the next.
     */
    private PickEventSession session = new PickEventSession();
    

    public MouseAppState( Application app ) {
        setEnabled(true);

        // We do this as early as possible because we want
        // first crack at the mouse events.
        app.getInputManager().addRawInputListener(mouseObserver);
    }

    public ViewPort findViewPort( Spatial s ) {
        return session.findViewPort(s);
    }

    public void addCollisionRoot( ViewPort viewPort ) {
        session.addCollisionRoot(viewPort);
    }

    public void addCollisionRoot( Spatial root, ViewPort viewPort ) {
        session.addCollisionRoot(root, viewPort);
    }

    public void removeCollisionRoot( ViewPort viewPort ) {
        session.removeCollisionRoot(viewPort);
    }

    public void removeCollisionRoot( Spatial root ) {
        session.removeCollisionRoot(root);
    }

    @Override
    protected void initialize( Application app ) {
        if( includeDefaultNodes ) {
            addCollisionRoot( app.getGuiViewPort() );
            addCollisionRoot( app.getViewPort() );
        }
    }

    @Override
    protected void cleanup( Application app ) {
        app.getInputManager().removeRawInputListener(mouseObserver);
        if( includeDefaultNodes ) {
            removeCollisionRoot( app.getGuiViewPort() );
            removeCollisionRoot( app.getViewPort() );
        }        
    }

    @Override
    protected void enable() {
        getApplication().getInputManager().setCursorVisible(true);
    }

    @Override
    protected void disable() {
        getApplication().getInputManager().setCursorVisible(false);
    }

    @Override
    public void update( float tpf ) {
        super.update(tpf);

        long time = System.nanoTime();
        if( time - lastSample < sampleFrequency )
            return;
        lastSample = time;

        Vector2f cursor = getApplication().getInputManager().getCursorPosition();

        session.cursorMoved((int)cursor.x, (int)cursor.y);
    }

    protected void dispatch(MouseButtonEvent evt) {
        if( session.buttonEvent(evt.getButtonIndex(), evt.getX(), evt.getY(), evt.isPressed()) )
            evt.setConsumed();
    }

    protected class MouseObserver extends DefaultRawInputListener {
        @Override
        public void onMouseMotionEvent( MouseMotionEvent evt ) {
            //if( isEnabled() )
            //    dispatch(evt);
        }

        @Override
        public void onMouseButtonEvent( MouseButtonEvent evt ) {
            if( isEnabled() ) {
                dispatch(evt);
            }
        }
    }
}


