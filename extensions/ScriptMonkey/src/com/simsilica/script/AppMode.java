/*
 * $Id$
 *
 * Copyright (c) 2013-2013 jMonkeyEngine
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

package com.simsilica.script;

import com.google.common.base.Objects;
import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.simsilica.lemur.event.BaseAppState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 *  Keeps track of a global application "mode" that can be watched for
 *  changes and/or tied to the enable/disable of other app states. 
 *
 *  @author    Paul Speed
 */
public class AppMode extends BaseAppState {

    private static AppMode instance = new AppMode();

    private Thread renderThread;
    private String currentMode;

    // We use copy-on-write list so that we can modify it during
    // iteration.
    private List<ModeListener> listeners = new CopyOnWriteArrayList<ModeListener>();

    private ConcurrentLinkedQueue<String> modeChanges = new ConcurrentLinkedQueue<String>();

    private Map<AppState, ModeEnabler> modeEnabled = new HashMap<AppState, ModeEnabler>();

    private LinkedList<String> modeStack = new LinkedList<String>();

    protected AppMode() {
    }

    public static AppMode getInstance() {
        return instance;
    }

    protected void setAppMode( String mode ) {
    
        if( renderThread != Thread.currentThread() ) {
            modeChanges.add(mode);
            return;
        }

        if( Objects.equal(this.currentMode, mode) ) {
            return;
        }

        // Otherwise, just notify the listeners
        String lastMode = currentMode;
        this.currentMode = mode;
        for( ModeListener l : listeners ) {
            l.modeChanged(currentMode, lastMode);
        }
    }

    protected String getAppMode() {
        return currentMode;
    }

    protected boolean pushAppMode( String mode ) {
    
        if( Objects.equal(mode, currentMode) )
            return false;

        modeStack.addFirst(currentMode);
        setAppMode(mode);

        return true;
    }

    protected void popAppMode( String mode ) {
    
        if( !mode.equals(currentMode) )
            throw new IllegalStateException( "Attempting to pop from a mode that is not current." );
        if( modeStack.isEmpty() )
            throw new IllegalStateException( "Mode stack is empty." );
        setAppMode(modeStack.removeFirst());
    }

    public static void setMode( String mode ) {
        getInstance().setAppMode(mode);
    }

    public static boolean changeMode( String from, String to ) {    
        if( !from.equals(getMode()) )
            return false;
        setMode(to);
        return true;
    }

    public static String getMode() {
        return getInstance().getAppMode();
    }

    public static boolean pushMode( String mode ) {
        return getInstance().pushAppMode(mode);
    }

    public static void popMode( String mode ) {
        getInstance().popAppMode(mode);
    }

    @Override
    public void update( float tpf ) {
        // Perform any delayed mode changes
        while( !modeChanges.isEmpty() ) {
            setAppMode(modeChanges.poll());
        }
    }

    protected ModeEnabler getEnabler( AppState state, boolean create ) {
    
        ModeEnabler result = modeEnabled.get(state);
        if( result == null && create ) {
            result = new ModeEnabler(state);
            listeners.add( result );
            modeEnabled.put( state, result );
        }
        return result;
    }

    public void addModeListener( ModeListener l ) {
        listeners.add(l);
    }

    public void removeModeListener( ModeListener l ) {
        listeners.remove(l);
    }

    /**
     *  Setup the specified app state to be enabled whenever
     *  any of the registered modes is the current mode or disabled
     *  when none of the registered modes are the current mode.
     */
    public void onModeEnable( AppState state, String... modes ) {
        ModeEnabler enabler = getEnabler(state, true);
        if( enabler.enabled == null ) {
            enabler.enabled = true;
        } else if( !enabler.enabled ) {
            throw new RuntimeException( "State is already setup with a disabled mode set." );
        }

        enabler.modes.addAll(Arrays.asList(modes));
    }

    /**
     *  Setup the specified app state to be disabled whenever
     *  any of the registered modes is the current mode or enabled
     *  when none of the registered modes are the current mode.
     *  This is the opposite of onModeEnable and an app state can
     *  only be in one mode or another at a time.
     */
    public void onModeDisable( AppState state, String... modes ) {
        ModeEnabler enabler = getEnabler(state, true);
        if( enabler.enabled == null )
            enabler.enabled = false;
        else if( enabler.enabled )
            throw new RuntimeException( "State is already setup with an enabled mode set." );

        enabler.modes.addAll( Arrays.asList(modes) );
    }

    public void unlinkModes( AppState state, String... modes ) {
        ModeEnabler enabler = getEnabler(state, false);
        if( enabler == null ) {
            return;
        }
        enabler.modes.removeAll( Arrays.asList(modes) );
    }

    /**
     *  Clears all linked mode settings setup for the specified app state.
     */
    public void clearModeLinks( AppState state ) {
        ModeEnabler enabler = modeEnabled.remove(state);
        if( enabler != null ) {
            listeners.remove( enabler );
        }
    }

    @Override
    protected void initialize( Application app ) {
        this.renderThread = Thread.currentThread();
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
    }

    @Override
    protected void disable() {
    }

    protected class ModeEnabler implements ModeListener {
    
        private AppState state;
        private Boolean enabled;
        private Set<String> modes = new HashSet<String>();

        public ModeEnabler( AppState state ) {
            this.state = state;
        }

        public void modeChanged( String mode, String lastMode ) {
            boolean was = modes.contains(lastMode);
            boolean is = modes.contains(mode);
            if( was == is ) {
                return; // avoid unnecessary enable changes
            }

            if( is ) {
                state.setEnabled( enabled );
            } else {
                state.setEnabled( !enabled );
            }
        }

        @Override
        public String toString() {
            return "ModeEnabler[state="+ state + ", enabled=" + enabled + ", modes=" + modes + "]";
        }
    }
}
