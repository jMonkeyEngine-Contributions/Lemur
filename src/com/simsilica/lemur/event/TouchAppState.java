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
import com.jme3.input.event.TouchEvent;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 *  Similar to MouseAppState, this state adapts touch and multitouch
 *  events to mouse and cursor events.  In the case of multitouch,
 *  each separate touch ID gets its own PickEventSession.  For more
 *  details on how a PickEventSession is handled, see MouseAppState
 *  or PickEventSession.
 *
 *  @author    iwgeric
 */
public class TouchAppState extends BaseAppState {
    private static final Logger logger = Logger.getLogger(TouchAppState.class.getName());

    private boolean includeDefaultNodes = true;
    private TouchObserver touchObserver = new TouchObserver();

    private long sampleFrequency = 1000000000 / 60; // 60 fps
    private long lastSample = 0;

    /**
     *  The session that tracks the state of pick events from one
     *  event frame to the next.
     */
    private PickEventSession session = new PickEventSession();
    private Map<Integer, PickEventSession> sessionMap = new HashMap<Integer, PickEventSession>();
    private Map<Integer, Location> locationMap = new HashMap<Integer, Location>();

    // storage class for the last touch locations of the active pointers
    private class Location {
        private int x;
        private int y;

        private Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private void set(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    public TouchAppState( Application app ) {
        setEnabled(true);

        // We do this as early as possible because we want
        // first crack at the mouse events.
        app.getInputManager().addRawInputListener(touchObserver);
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
        app.getInputManager().removeRawInputListener(touchObserver);
        if( includeDefaultNodes ) {
            removeCollisionRoot( app.getGuiViewPort() );
            removeCollisionRoot( app.getViewPort() );
        }
        sessionMap.clear();
        locationMap.clear();
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

        dispatchMotion();
    }

    protected void dispatchMotion() {
        for (int pointerId: sessionMap.keySet()) {
            Location lastLocation = locationMap.get(pointerId);
            if (lastLocation != null) {
                sessionMap.get(pointerId).cursorMoved(lastLocation.x, lastLocation.y);
            }
        }
    }

    protected boolean dispatchButton(int pointerId, int x, int y, boolean pressed) {
        PickEventSession targetSession = sessionMap.get(pointerId);
        // call cursorMoved to update the hitTarget before calling buttonEvent
        boolean moveConsumed = targetSession.cursorMoved(x, y);
        // We are passing BUTTON_LEFT to buttonEvent all the time.
        // This is ok because touch motion is separated by individual
        // targetSesstions for each finger.  Since each session only gets the
        // touch motion for the finger that is associated with the session,
        // it's ok to not track the touch pointerId inside each session.
        boolean buttonConsumed = targetSession.buttonEvent(MouseInput.BUTTON_LEFT, x, y, pressed);
        if (buttonConsumed && !pressed) {
            targetSession.clearHitTarget();
        }
        return buttonConsumed;
    }

    protected PickEventSession getPickSession(int pointerId) {
        PickEventSession targetSession;
        if (sessionMap.isEmpty()) {
            targetSession = session;
        } else if (!sessionMap.containsKey(pointerId)) {
            targetSession = session.clone();
        } else {
            targetSession = sessionMap.get(pointerId);
        }
        return targetSession;
    }

    protected class TouchObserver extends DefaultRawInputListener {

        @Override
        public void onTouchEvent(TouchEvent te) {
            if (!isEnabled()) {
                return;
            }
            switch (te.getType()) {
                case DOWN:
                    sessionMap.put(te.getPointerId(), getPickSession(te.getPointerId()));
                    locationMap.put(te.getPointerId(), new Location((int)te.getX(), (int)te.getY()));
                    if (dispatchButton(te.getPointerId(), (int)te.getX(), (int)te.getY(), true)) {
                        te.setConsumed();
                    }
                    break;
                case MOVE:
                    Location lastLocation = locationMap.get(te.getPointerId());
                    if (lastLocation != null) {
                        lastLocation.set((int)te.getX(), (int)te.getY());
                    }
                    break;
                case UP:
                    if (dispatchButton(te.getPointerId(), (int)te.getX(), (int)te.getY(), false)) {
                        te.setConsumed();
                    }
                    locationMap.remove(te.getPointerId());
                    sessionMap.remove(te.getPointerId());
                    break;
                default:
                    break;
            }
        }
    }
}


