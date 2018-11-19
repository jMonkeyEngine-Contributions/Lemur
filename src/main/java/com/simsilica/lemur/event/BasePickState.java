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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector2f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;


/**
 *  Consolidates the PickEventSession management for doing
 *  scene picking.  This is the base class for the MouseAppState
 *  and the TouchAppState.
 *
 *  @author    Paul Speed
 */
public abstract class BasePickState extends BaseAppState
                                    implements PickState {
    static Logger log = LoggerFactory.getLogger(BasePickState.class);

    private boolean includeDefaultNodes = true;

    private long sampleFrequency = 1000000000 / 60; // 60 fps
    private long lastSample = 0;

    /**
     *  The session that tracks the state of pick events from one
     *  event frame to the next.
     */
    private PickEventSession session = new PickEventSession();

    /**
     *  Keeps track of the owners interested in picking being enabled
     *  as well as how many times they have requested picking.
     */
    private Map<Object, Integer> owners = new HashMap<>();
    private int totalRequests = 0;

    protected BasePickState() {
    }

    protected final PickEventSession getSession() {
        return session;
    }

    /**
     *  Signifies that the specified owner needs the pick state to be enabled.
     */
    @Override
    public void requestEnabled( Object owner ) {
        Integer existing = owners.get(owner);
        if( existing == null ) {
            owners.put(owner, 1);            
        } else {
            owners.put(owner, existing + 1);
        }
        totalRequests++;
        if( log.isTraceEnabled() ) {
            log.trace("request: Total enabled requests:" + totalRequests);
        }
        setEnabled(true);
    }
    
    /**
     *  Signifies that the specified owner no longer needs the pick state to be enabled.
     *  Will return true if the state is still enabled (because of other requests) or
     *  false if the state is now disabled.
     */
    @Override
    public boolean releaseEnabled( Object owner ) {
        Integer existing = owners.get(owner);
        if( existing == null || existing == 0 ) {
            throw new IllegalArgumentException("Invalid owner, no requests pending");
        }        
        if( existing == 1 ) {
            owners.remove(owner);
        } else {
            owners.put(owner, existing-1);
        }
        totalRequests--;
        if( log.isTraceEnabled() ) {
            log.trace("release: Total enabled requests:" + totalRequests);
        }
        setEnabled(totalRequests > 0);
        return isEnabled();
    }
    
    @Override
    public boolean hasRequestedEnabled( Object owner ) {    
        Integer existing = owners.get(owner);
        return existing != null && existing > 0;
    }    
    
    @Override    
    public boolean resetEnabled() {
        if( log.isTraceEnabled() ) {
            log.trace("reset: Total enabled requests:" + totalRequests);
        }
        setEnabled(totalRequests > 0);
        return isEnabled();
    }

    public void setIncludeDefaultCollisionRoots( boolean b ) {
        this.includeDefaultNodes = b;
        if( isInitialized() ) {
            if( b ) {
                addCollisionRoot(getApplication().getGuiViewPort(), PICK_LAYER_GUI);
                addCollisionRoot(getApplication().getViewPort(), PICK_LAYER_SCENE);
            } else {
                removeCollisionRoot(getApplication().getGuiViewPort());
                removeCollisionRoot(getApplication().getViewPort());
            }
        }
    }

    public boolean getIncludeDefaultCollisionRoots() {
        return includeDefaultNodes;
    }

    @Deprecated
    public ViewPort findViewPort( Spatial s ) {
        return session.findViewPort(s);
    }

    public void addCollisionRoot( ViewPort viewPort ) {
        session.addCollisionRoot(viewPort);
    }

    public void addCollisionRoot( ViewPort viewPort, String layer ) {
        session.addCollisionRoot(viewPort, layer);
    }

    public void addCollisionRoot( Spatial root, ViewPort viewPort ) {
        session.addCollisionRoot(root, viewPort);
    }

    public void addCollisionRoot( Spatial root, ViewPort viewPort, String layer ) {
        session.addCollisionRoot(root, viewPort, layer);
    }

    public void removeCollisionRoot( ViewPort viewPort ) {
        session.removeCollisionRoot(viewPort);
    }

    public void removeCollisionRoot( Spatial root ) {
        session.removeCollisionRoot(root);
    }

    /**
     *  Sets the order in which the pick layers will be checked for collisions.
     *  The default ordering is PICK_LAYER_GUI then PICK_LAYER_SCENE.
     */
    public void setPickLayerOrder( String... layers ) {
        session.setPickLayerOrder(layers);
    }

    public String[] getPickLayerOrder() {
        return session.getPickLayerOrder();
    }

    @Override
    protected void initialize( Application app ) {
        if( includeDefaultNodes ) {
            addCollisionRoot(getApplication().getGuiViewPort(), PICK_LAYER_GUI);
            addCollisionRoot(getApplication().getViewPort(), PICK_LAYER_SCENE);
        }
    }

    @Override
    protected void cleanup( Application app ) {
        if( includeDefaultNodes ) {
            removeCollisionRoot(app.getGuiViewPort());
            removeCollisionRoot(app.getViewPort());
        }
    }

    @Override
    protected void onEnable() {
        getApplication().getInputManager().setCursorVisible(true);
    }

    @Override
    protected void onDisable() {
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

    protected abstract void dispatchMotion();


}
