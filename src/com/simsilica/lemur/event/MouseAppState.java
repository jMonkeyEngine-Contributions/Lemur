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


import com.jme3.app.Application;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector2f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;


/**
 *  Keeps track of a set of "collision roots" for mouse picking
 *  and performs the pick event processing necessary to deliver
 *  MouseEvents and CollisionEvents to spatials with either the
 *  MouseEventControl or CursorEventControl attached.  (The
 *  actual event delivery is done by the PickEventSession class
 *  for which this app state has one active session.)
 *
 *  <h2>PickEventSession behavior:</h2>
 *
 *  <p>Collision roots may either be perspective or orthogonal
 *  and the appropriate type of collision is done.  The ViewPort's
 *  camera is used to detect the difference.</p>
 *
 *  <p>Events are delivered in near to far order to any 'target'
 *  that the cursor ray collides with until the event is consumed.
 *  Enter and exit events are delivered as targets are acquired or
 *  lost.</p>
 *
 *  <p>If a button down event happens over a target then it is considered
 *  'captured'.  This spatial will be provided to subsequent events
 *  in addition to the normal target.  Furthermore, any new motion
 *  events are always delivered to the captured spatial first.<p>  
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

    @Deprecated
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


