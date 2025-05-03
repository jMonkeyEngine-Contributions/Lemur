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
import com.jme3.input.MouseInput;
import com.jme3.input.event.TouchEvent;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
public class TouchAppState extends BasePickState {
    private static final Logger logger = Logger.getLogger(TouchAppState.class.getName());

    private TouchObserver touchObserver = new TouchObserver();

    protected Map<Integer, PointerData> pointerDataMap = new HashMap<Integer, PointerData>();

    /**
     * Storage class for the session and last location of the touch events
     *  for a single pointer (finger for touch). <br>
     * Not using a Vector2f to minimize garbage collection since the
     *  touch event from jME provides 2 floats for x and y.
     */
    protected class PointerData {
        private int pointerId;
        private PickEventSession session;
        private int lastX;
        private int lastY;

        protected PointerData(int pointerId, PickEventSession session, int lastX, int lastY) {
            this.pointerId = pointerId;
            this.session = session;
            this.lastX = lastX;
            this.lastY = lastY;
        }
    }

    public TouchAppState( Application app ) {
        setEnabled(true);

        // We do this as early as possible because we want
        // first crack at the mouse events.
        app.getInputManager().addRawInputListener(touchObserver);
    }

    @Override
    protected void cleanup( Application app ) {
        app.getInputManager().removeRawInputListener(touchObserver);
        pointerDataMap.clear();
    }

    /**
     * Dispatches the last touch locations to the active PickEventSessions. <br>
     * When touch motion events occur, the touch location is stored and then
     * dispatched at the frequency defined to avoid sending more motions
     * than necessary.
     * An early out is provided if no PickEventSessions are active (ie. no touch
     * pointers are active).
     */
    @Override
    protected void dispatchMotion() {
        if (pointerDataMap.isEmpty()) {
            return;
        }
        for (Entry<Integer, PointerData> entry: pointerDataMap.entrySet()) {
            PointerData pointerData = entry.getValue();
            pointerData.session.cursorMoved(
                    pointerData.lastX, pointerData.lastY);
        }
    }

    /**
     * Dispatches a button action to the appropriate PickEventSession for the
     * touch pointer provided.
     * @param pointerData  PointerData object for the appropriate touch pointer
     * @param pressed  True when pressed, False when released
     * @return  True if the PickEventSession consumed the event, False otherwise.
     */
    protected boolean dispatchButton(PointerData pointerData, boolean pressed) {
        // We are passing BUTTON_LEFT to buttonEvent all the time.
        // This is ok because touch motion is separated by individual
        // targetSesstions for each finger.  Since each session only gets the
        // touch motion for the finger that is associated with the session,
        // it's ok to not track the touch pointerId inside each session.
        boolean buttonConsumed = pointerData.session.buttonEvent(
                MouseInput.BUTTON_LEFT, pointerData.lastX, pointerData.lastY, pressed);
        if (buttonConsumed && !pressed) {
            // For the UP event, clear the hitTarget so mouseExited will be
            // called.  This is necessary because for touch there are no additional
            // mouse motions after the UP to cause mouseExited to be called.
            pointerData.session.clearHitTarget();
        }
        return buttonConsumed;
    }

    /**
     * Returns (or creates) the PointerData object with the appropriate
     * PickEventSession and X/Y coordinates for the provided pointerId. <br>
     * The provided X and Y locations are stored in the PointerData object for
     * created PointerData objects and updated if the PointerData object already exists.
     * @param pointerId  Touch pointer id
     * @param x  X component of the touch location in pixels
     * @param y  Y component of the touch location in pixels
     * @return  Associated PointerData object which contains the appropriate
     * PickEventSession and X/Y coordinates of the last touch event
     */
    protected PointerData getPointerData(int pointerId, int x, int y) {
        PointerData pointerData;
        if (pointerDataMap.isEmpty()) {
            pointerData = new PointerData(pointerId, getSession(), x, y);
        } else if (!pointerDataMap.containsKey(pointerId)) {
            pointerData = new PointerData(pointerId, getSession().clone(), x, y);
        } else {
            pointerData = pointerDataMap.get(pointerId);
            pointerData.lastX = x;
            pointerData.lastY = y;
        }
        pointerDataMap.put(pointerId, pointerData);
        return pointerData;
    }

    /**
     * TouchObserver provides the touch event data (pointer, x, and y) to the
     * Lemur pick session for processing.
     */
    protected class TouchObserver extends DefaultRawInputListener {

        @Override
        public void onTouchEvent(TouchEvent te) {
            if (!isEnabled()) {
                return;
            }
            PointerData pointerData;
            switch (te.getType()) {
                case TAP:
                    pointerData = pointerDataMap.get(te.getPointerId());
                    if (pointerData != null) {
                        pointerData.lastX = (int)te.getX();
                        pointerData.lastY = (int)te.getY();
                        if (dispatchButton(pointerData, false)) {
                            te.setConsumed();
                        }
                        pointerDataMap.remove(te.getPointerId());
                    }
                    break;
                case MOVE:
                    pointerData = pointerDataMap.get(te.getPointerId());
                    if (pointerData != null) {
                        pointerData.lastX = (int)te.getX();
                        pointerData.lastY = (int)te.getY();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
