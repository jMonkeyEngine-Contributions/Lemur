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
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;


/**
 *  Contains information about a motion event over a particular
 *  spatial.
 *
 *  @author    Paul Speed
 */
public class CursorMotionEvent extends AbstractCursorEvent {

    /**
     *  Included for parity with the JME classes but mostly useless.
     */
    private int scroll;
    
    /**
     *  Tracks the amount of scroll change in the frame of motion.  Unlike
     *  x,y locations, the scroll delta is often more useful than the full scroll
     *  value since event listeners will have no way of knowing how much adjustment
     *  was done outside of their 'view' before being called again.
     */
    private int scrollDelta;
    
    public CursorMotionEvent( ViewPort view, Spatial target, float x, float y, 
                              int scroll, int scrollDelta, CollisionResult collision ) {
        super(view, target, x, y, collision);
                                      
        this.scroll = scroll;
        this.scrollDelta = scrollDelta;
    }
 
    /**
     *  Returns the full value of the 'scroll wheel' or scroll control at the time
     *  of this motion event.
     */  
    public int getScrollValue() {
        return scroll;
    }
    
    /**
     *  Returns the amount the scroll wheel moved during this mouse motion.
     */  
    public int getScrollDelta() {
        return scrollDelta;
    }    
}


