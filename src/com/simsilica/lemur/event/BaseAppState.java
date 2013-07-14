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
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


 
/**
 *  Base class app state that includes built-in
 *  convenience management for enable/disable/etc.
 *
 *  @author    Paul Speed
 */ 
public abstract class BaseAppState implements AppState
{
    static Logger log = LoggerFactory.getLogger(BaseAppState.class);
    
    private Application app;
    private boolean initialized;
    private boolean enabled = true;

    protected abstract void initialize( Application app );
    protected abstract void cleanup( Application app );
    protected abstract void enable();
    protected abstract void disable();
    
    public final void initialize( AppStateManager stateManager, Application app ) 
    {
        if( log.isTraceEnabled() )
            log.trace( "initialize():" + this );
            
        this.app = app;
        initialized = true;
        initialize(app);        
        if( isEnabled() )
            enable();
    }

    public final boolean isInitialized() 
    {
        return initialized;
    }

    public final Application getApplication()
    {
        return app;
    }

    public final AppStateManager getStateManager()
    {
        return app.getStateManager();
    }
    
    public final <T extends AppState> T getState( Class<T> type )
    {
        return getStateManager().getState(type);
    }

    public final void setEnabled(boolean enabled) 
    {
        if( this.enabled == enabled )
            return;
        this.enabled = enabled;
        if( !isInitialized() )
            return;
        if( enabled )
            {
            log.trace( "enable():" + this );
            enable();
            }
        else
            {
            log.trace( "disable():" + this );
            disable();
            }
    }
    
    public final boolean isEnabled() 
    {
        return enabled;
    }

    public void stateAttached(AppStateManager stateManager) 
    {
    }

    public void stateDetached(AppStateManager stateManager) 
    {
    }

    public void update(float tpf) 
    {
    }

    public void render(RenderManager rm) 
    {
    }

    public void postRender()
    {
    }

    public final void cleanup() 
    {
        if( log.isTraceEnabled() )
            log.trace( "cleanup():" + this );
            
        if( isEnabled() )
            disable();
        cleanup(app);
        initialized = false;
    }
}
