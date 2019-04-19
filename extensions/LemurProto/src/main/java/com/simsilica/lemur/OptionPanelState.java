/*
 * $Id: OptionPanelState.java 1536 2014-12-03 05:20:21Z PSpeed42@gmail.com $
 * 
 * Copyright (c) 2014, Simsilica, LLC
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

package com.simsilica.lemur;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import com.simsilica.lemur.event.ConsumingMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;


/**
 *  Provides modal option panel support where the option
 *  panel is the only thing that can receive mouse/touch input 
 *  until closed.
 *  
 *  Note: requires PopupState to have also been attached, which
 *  is done by GuiGlobals by default.  This class is now just a thin
 *  wrapper around the standard PopupState.
 *
 *  @author    Paul Speed
 */
public class OptionPanelState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(OptionPanelState.class);

    private OptionPanel current;
    private String style;
    private ElementId defaultElementId = new ElementId(OptionPanel.ELEMENT_ID);
    
    public OptionPanelState() {
    }

    public OptionPanelState( String style ) {
        this.style = style;
    }

    public OptionPanelState( ElementId defaultElementId, String style ) {
        this.defaultElementId = defaultElementId;
        this.style = style;
    }
    
    public OptionPanelState( Node guiNode ) {
        if( guiNode != null ) {
            log.warn("guiNode constructor now ignores the guiNode parameter, see: PopupState");
        }
    }
    
    /** 
     *  Creates and displays a modal OptionPanel with the specified 
     *  settings.  The option panel will be visible until the user
     *  clicks a response or until close() is called.
     */
    public void show( String title, String message, Action... options ) {
        show(title, message, defaultElementId, options);
    }               
 
    /** 
     *  Creates and displays a modal OptionPanel with the specified 
     *  settings.  The option panel will be visible until the user
     *  clicks a response or until close() is called.
     */
    public void show( String title, String message, ElementId elementId, Action... options ) {
        show(new OptionPanel(title, message, elementId, style, options));
    }               
 
    protected String getName( Throwable t ) {
        String name = t.getClass().getSimpleName();
        StringBuilder sb = new StringBuilder();
        boolean last = true;
        sb.append(Character.toUpperCase(name.charAt(0)));
        for( int i = 1; i < name.length(); i++ ) {
            char c = name.charAt(i);
            boolean upper = Character.isUpperCase(c);
            if( upper && !last ) {
                sb.append(" ");
                last = true;
            }
            sb.append(c);
            last = upper;
        }
        return sb.toString();   
    }
 
    /**
     *  Creates and displays a model OptionPanel with the specified
     *  error information.  An attempt is made to construct a useful
     *  message for the specified Throwable.
     *  The option panel will be visible until the user
     *  clicks a response or until close() is called.
     */
    public void showError( String title, Throwable t ) {
        show(title, getName(t) + "\n" + t.getMessage(), defaultElementId);   
    }     
 
    /**
     *  Creates and displays a model OptionPanel with the specified
     *  error information.  An attempt is made to construct a useful
     *  message for the specified Throwable.
     *  The option panel will be visible until the user
     *  clicks a response or until close() is called.
     */
    public void showError( String title, Throwable t, ElementId elementId ) {
        show(title, getName(t) + "\n" + t.getMessage(), elementId);   
    }     
 
    /**
     *  Modally shows the specified OptionPanel in the guiNode as defined by
     *  getGuiNode().  An invisible blocker geometry is placed behind it
     *  to catch all mouse events until the panel is closed.  The option 
     *  panel will be visible until the user clicks a response or until 
     *  close() is called. 
     */
    public void show( OptionPanel panel ) {
        if( this.current != null ) {
            current.close();
        }
        
        this.current = panel;
 
        Vector2f screen = getState(PopupState.class).getGuiSize();
        Vector3f pref = current.getPreferredSize();
        
        Vector3f pos = new Vector3f(screen.x, screen.y, 0).multLocal(0.5f);
        pos.x -= pref.x * 0.5f;
        pos.y += pref.y * 0.5f;
        current.setLocalTranslation(pos);
        
        getState(PopupState.class).showModalPopup(current);
    }
 
    /**
     *  Closes an open OptionPanel if one is currently open.  Does
     *  nothing otherwise.
     */
    public void close() {
        if( current != null ) {
            //current.close();
            getState(PopupState.class).closePopup(current);
            current = null;
        }
    }     
 
    /**
     *  Returns the currently displayed OptionPanel or null if
     *  no option panel is visible.
     */
    public OptionPanel getCurrent() {
        return current;
    }
 
    /**
     *  Sets the style that will be used for created OptionPanels.
     */
    public void setStyle( String style ) {
        this.style = style;
    }
    
    /**
     *  Returns the style that will be used for created OptionPanels.
     */
    public String getStyle() {
        return style;
    }
    
    /**
     *  Sets the GUI node that will be used to display the option
     *  panel.  By default, this is SimpleApplication.getGuiNode().
     */
    public void setGuiNode( Node guiNode ) {
        if( guiNode != null ) {
            log.warn("guiNode parameter ignored, uses PopupState's guiNode instead, see: PopupState");
        }
    }
    
    /**
     *  Returns the GUI node that will be used to display the option
     *  panel.  This is now always PopupState.getGuiNode()
     */
    public Node getGuiNode() {
        return getState(PopupState.class).getGuiNode();
    }

    @Override
    protected void initialize( Application app ) {
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    public void update( float tpf ) {
    }

    @Override
    protected void onDisable() {
    }
}
