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


// Some functions for creating HUD gui windows


import com.simsilica.lemur.*;
import com.simsilica.lemur.style.*;
import com.simsilica.script.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

scripts.addDefaultImports( "com.simsilica.lemur.*" );


// Expose the HUD directly as a binding
hud = getState(HudState.class)

// Some Lemur-specific metaclass add-ons
Container.metaClass {
    leftShift { Panel p ->
        delegate.addChild(p);
    }
}

// We could go directly to the closure but we'd like to 
// provide some exception support
class ButtonCommand implements Command<Button> {
    static Logger log = LoggerFactory.getLogger("scripts.ButtonCommand");
    Closure exec;
    
    public ButtonCommand( Closure exec ) {
        this.exec = exec;
    }
        
    public void execute( Button source ) {
        try {
            exec(source);
        } catch( Exception e ) {
            log.error("Error running button onClick", e);
        }
    }
}

Button.metaClass {
    onClick { Closure exec ->
        delegate.addClickCommands(new ButtonCommand(exec));
    }
}


// Setup the window management... making it easier to
// retrieve and manage windows by name
windowMap = [:]

import com.simsilica.lemur.component.*;

// Some stuff to add child elements to any container
Container.metaClass {

    label { String text ->
        return label(text, null); 
    }

    label { String text, Closure config ->
        def result = new Label(text, "glass");
        if( config != null ) {
            result.with(config);
        } 
        delegate.addChild(result);
        return result;
    }
    
    button { String name, List constraints, Closure config ->
        def result = getChild(name);
        if( result == null ) {
            //result = new Button(name, new ElementId("window.button"), "glass");
            result = new Button(name, new ElementId("button"), "glass");
            result.setName(name);
            if( constraints != null ) {
                addChild(result, constraints.toArray());
            } else { 
                addChild(result);
            }
        }
        if( config != null ) {
            result.with(config);
        }
        return result;
    }

    button { String name, Closure config ->
        return button(name, null, config);
    }
    
    button { String name ->
        return button(name, null, null);
    }
 
    container { Axis axis ->
        return container(axis, null);
    }
    
    container { Axis axis, Closure config ->
        Axis alt = axis == Axis.X ? Axis.Y : Axis.X;
        def layout = new SpringGridLayout(axis, alt, FillMode.Even, FillMode.Even);
        def result = new Container(layout, "glass");
        if( config != null ) {
            result.with(config);
        }
        addChild(result);
        return result;
    }
}

// Create a simple container-based Window with some nice
// properties and methods for managing the children
class Window extends Container {
    Label title;
    
    public Window( String name ) {
        //super(new ElementId("window.container"), "glass");
        super(new ElementId("container"), "glass");
        this.name = name;
        //this.title = new Label(name, new ElementId("window.title.label"), "glass");
        this.title = new Label(name, new ElementId("title"), "glass");
        addChild(title);  
    } 
}

Container window( String name, Closure config ) {
    def result = windowMap.get(name);
    if( result == null ) {
        result = new Window(name);
        windowMap.put(name, result);
    }
    if( config != null ) {
        result.with(config);
    }
    return result;
}

Container window( String name ) {
    return window(name, null);
}


