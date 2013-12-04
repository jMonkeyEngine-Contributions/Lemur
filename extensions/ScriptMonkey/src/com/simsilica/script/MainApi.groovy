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


// If the "scripts" directory doesn't exist then create it for the user
new File("scripts").mkdirs();

import com.simsilica.script.*;

// General utilities
Object getState( Class type ) {
    return app.stateManager.getState(type);
}

// A global 'log'
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
log = LoggerFactory.getLogger("scripts");


// Mode hooks
modeHooks = [:]

class ModeHook implements ModeListener {
    static Logger log = LoggerFactory.getLogger("scripts.ModeHook");
    
    String name;
    Boolean enabled;
    Set modes = [];    
    Closure onEnabled;
    Closure onDisabled;
 
    public ModeHook modes( String... array ) {
        modes.addAll(array);
        return this;   
    }
    
    public Set getModes() {
        return modes;
    }
 
    protected void updateEnabled() {
        boolean b = modes.contains(AppMode.getMode());
        if( b == enabled ) {
            return;
        }
        try {
            if( b ) {
                runEnabled();
            } else {
                runDisabled();
            }
        } catch( Exception e ) {
            log.error("Error running hooks", e );
        }
    }
 
    protected void runEnabled() {
        if( onEnabled == null )
            return;
        onEnabled();
    }
    
    protected void runDisabled() {
        if( onDisabled == null )
            return;
        onDisabled();
    }
    
    public ModeHook onEnabled( Closure c ) {    
        onEnabled = c; 
        return this;
    }
    
    public ModeHook onDisabled( Closure c ) {
        onDisabled = c;
        return this;
    }
    
    public void modeChanged( String mode, String lastMode ) {
        if( modes.contains(mode) || modes.contains(lastMode) ) {
            updateEnabled();
        }
    }
    
    public void release() {
        modeHooks.remove(name);
        AppMode.instance.removeModeListener(modeHook);
    }         
}

ModeHook modeHook( String name, Closure config ) {
    def result = modeHooks.get(name);
    if( result == null ) {
        result = new ModeHook("name":name);
        modeHooks.put(name, result);
        AppMode.instance.addModeListener(result);
    }
    if( config != null ) {
        config.setDelegate(result);
        config();
    }
    return result;
}

ModeHook modeHook( String name ) {
    return modeHook(name, null);
}


// Selection management
import com.jme3.scene.Spatial;
Spatial setSelected( Spatial s ) {
    if( getState(SelectionState) != null ) {
        getState(SelectionState).setSelectedSpatial(s);
    }
    return s;
} 

// Some default listener setup
// When the selection changes we will stick it in a binding
if( getState(SelectionState) != null ) {
    getState(SelectionState).addSelectionListener( { s, last ->
        selected = s;
    } as SelectionListener );
}
selected = null;


// Selection hooks
selectionHooks = [:]

// We could go directly to the listener interface but I think
// it's useful to mimic a consistent hook idiom and also it
// formalizes the selection and deselection parts.  Callers
// can always add listeners directly to the state if they
// want direct listener support.
class SelectionHook implements SelectionListener {
    static Logger log = LoggerFactory.getLogger("scripts.ModeHook");
    
    String name;
    Closure selected;
    Closure deselected;
    
    public SelectionHook onSelected( Closure c ) {    
        selected = c; 
        return this;
    }
    
    public SelectionHook onDeselected( Closure c ) {
        deselected = c;
        return this;
    }
    
    public void selectionChanged( Spatial selection, Spatial previous ) {
System.out.println( "selectionChanged(" + selection + ", " + previous + ")" );    
        try {
            if( previous != null && deselected != null ) {
                deselected(previous);
            } 
            if( selection != null && selected != null ) {
                selected(selection);
            }
        } catch( Exception e ) {
            log.error("Error running hooks", e);
        }
    }
}

SelectionListener selectionHook( String name, Closure config ) {
    def hook = selectionHooks.get(name);
    if( hook == null ) {
        hook = new SelectionHook(name:name);
        selectionHooks.put(name, hook); 
        getState(SelectionState).addSelectionListener(hook);
    }
    if( config != null ) {
        hook.with(config);
    }
    return hook; 
}

SelectionListener selectionHook( String name ) {
    return selectionHook(name, null);
}


// Some general helper functions.

void help() {
    println "Default imports:";
    println "    " + scripts.defaultImports.join("\n    ");
    println "Default bindings:";
    println "    " + scripts.initBindings.collect{ it }.join("\n    ");
    println "";
    println "Type: help ClassName";
    println "   to get information about a class.";
    println "Type: help someObject";
    println "   to get information about an object's properties.";
}

Class help( Class type ) {
    println "Info for:" + type;
    println "    super class:" + type.superclass;
    println "    properties:";     
    println "        " + type.metaClass.properties.findAll{it.getter.declaringClass.name == type.name}.collect{it.name}.join("\n        ");
    return type     
}

Object help( Object o, boolean all ) {
    println "Info for:" + o ;
    if( o == null ) {
        return;
    }
    if( o.hasProperty("help") ) {
        println o.help;
        return;
    }    
    def type = o.class;
    def local = type.metaClass.properties.findAll{it.getter.declaringClass.name == type.name}.collect{it.name}; 
    println "    class:" + o.class;
    println "    properties:" + (all ? " (including supertype properties)":"");
    println "        " + o.properties.findAll{ all || local.contains(it.key) }.collect{it}.join("\n        ");  
}

Object help( Object o ) {
    help( o, false );
}


