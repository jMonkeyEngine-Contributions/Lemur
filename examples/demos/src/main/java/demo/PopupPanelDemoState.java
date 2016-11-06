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

package demo;

import java.util.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;

import com.simsilica.lemur.*;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;

/**
 *
 *
 *  @author    Paul Speed
 */
public class PopupPanelDemoState extends BaseAppState {
 
    /**
     *  A command we'll pass when we create the popups so that
     *  we're notified when they close... whether we closed them
     *  or the user clicked in a way that closed them.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    public PopupPanelDemoState() {
    }
    
    @Override
    protected void initialize( Application app ) {
    }
    
    @Override
    protected void cleanup( Application app ) {
    }
    
    @Override
    protected void onEnable() {
        //createModalPopup();   
        createTransientPopup();   
    }
    
    @Override
    protected void onDisable() {
    }
    
    protected void createTransientPopup() {
        Panel popup = createPopup(true);
        randomizeLocation(popup);
        
        getState(PopupState.class).showPopup(popup, closeCommand);
    }

    protected void createModalPopup() {
        Panel popup = createPopup(true);
        randomizeLocation(popup);
        
        getState(PopupState.class).showModalPopup(popup, closeCommand);
    }
    
    protected void createColoredModalPopup() {        
        Panel popup = createPopup(true);
        randomizeLocation(popup);
        
        // Create a random color.
        float r = (float)(Math.random() * 0.75 + 0.25);
        float g = (float)(Math.random() * 0.75 + 0.25);
        float b = (float)(Math.random() * 0.75 + 0.25);
        ColorRGBA c = new ColorRGBA(r, g, b, 0.5f);
        
        getState(PopupState.class).showPopup(popup, PopupState.ClickMode.Consume, closeCommand, c);
    }
 
    protected void randomizeLocation( Panel p ) {
        // Calculate a random location that will also leave the popup
        // fully on screen.
        Vector3f pref = p.getPreferredSize();
        double xRange = getApplication().getCamera().getWidth() - pref.x; 
        double yRange = getApplication().getCamera().getHeight() - pref.y; 
 
        double x = Math.random() * xRange;
        double y = Math.random() * yRange + pref.y;
        
        p.setLocalTranslation((float)x, (float)y, 0);        
    }
    
    private Panel createPopup( boolean includeClose ) {
        Container window = new Container();
        window.addChild(new Label("Popup Window", new ElementId("window.title.label")));
        
        window.addChild(new ActionButton(new CallMethodAction("Transient Popup", 
                                                              this, "createTransientPopup")));
        window.addChild(new ActionButton(new CallMethodAction("Modal Popup", 
                                                              this, "createModalPopup")));
        window.addChild(new ActionButton(new CallMethodAction("Colored Modal Popup", 
                                                              this, "createColoredModalPopup")));
        if( includeClose ) {
            window.addChild(new ActionButton(new CallMethodAction("Close", 
                                                                  window, "removeFromParent")));
        }
        return window;                                                              
    }
    
    private class CloseCommand implements Command<PopupState> {
        
        public void execute( PopupState state ) {
            // If the state has no active popups then we'll remove this
            // state also
            if( !state.hasActivePopups() ) {
                getState(MainMenuState.class).closeChild(PopupPanelDemoState.this);
            }
        }
    }
}
