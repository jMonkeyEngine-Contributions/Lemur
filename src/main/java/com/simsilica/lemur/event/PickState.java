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

package com.simsilica.lemur.event;

import com.jme3.app.state.AppState;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;

/**
 *  Interface that pick event app states will implement so that
 *  the GuiGlobals user need not worry about which one(s) is/are
 *  active.  Generally, only MouseAppState or TouchAppState are active
 *  at any one time.  If we ever support both being active at once then
 *  we can wrap them in a composite object that implements this interface.
 *
 *  @author    Paul Speed
 */
public interface PickState extends AppState {

    public static final String PICK_LAYER_SCENE = "scene";
    public static final String PICK_LAYER_GUI = "gui";

    public void setIncludeDefaultCollisionRoots( boolean b );
    public boolean getIncludeDefaultCollisionRoots();
    public void addCollisionRoot( ViewPort viewPort );
    public void addCollisionRoot( ViewPort viewPort, String layer );
    public void addCollisionRoot( Spatial root, ViewPort viewPort );
    public void addCollisionRoot( Spatial root, ViewPort viewPort, String layer );
    public void removeCollisionRoot( ViewPort viewPort );
    public void removeCollisionRoot( Spatial root );
    public void setPickLayerOrder( String... layers );
    public String[] getPickLayerOrder();
    
    /**
     *  Signifies that the specified owner needs the pick state to be enabled.
     */
    public void requestEnabled( Object owner );
    
    /**
     *  Signifies that the specified owner no longer needs the pick state to be enabled.
     *  Will return true if the state is still enabled (because of other requests) or
     *  false if the state is now disabled.
     */
    public boolean releaseEnabled( Object owner );
    
    /**
     *  Returns true if the specified owner has an active request for picking to
     *  be enabled.
     */
    public boolean hasRequestedEnabled( Object owner );
 
    /**
     *  Refreshes the enabled/disabled state based on the current
     *  request count.  This is useful to reset the 'stack' if a forced
     *  enable/disable was previously done.
     */   
    public boolean resetEnabled();

}
