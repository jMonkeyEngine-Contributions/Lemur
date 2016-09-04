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

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.event.ConsumingMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.style.ElementId;


/**
 *  Provides modal option panel support where the option
 *  panel is the only thing that can receive mouse/touch input 
 *  until closed.
 *
 *  @author    Paul Speed
 */
public class OptionPanelState extends BaseAppState {

    private OptionPanel current;
    private Geometry blocker;
    private Node guiNode;
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
        this.guiNode = guiNode;
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
 
        Vector3f size = current.getPreferredSize();
        
        Camera cam = getApplication().getCamera();
        Vector3f camSize = new Vector3f(cam.getWidth(), cam.getHeight(), 0);        
        Vector3f pos = camSize.mult(0.5f);
        pos.x -= size.x * 0.5f;
        pos.y += size.y * 0.5f;
        
        BoundingBox bb = (BoundingBox)getGuiNode().getWorldBound();
        if( bb != null ) {        
            pos.z = bb.getCenter().z + bb.getZExtent() * 2;
        }

        setupBlocker(pos.z, camSize);

        // Move it forward just a little more to be safe
        pos.z++;
 
        panel.setLocalTranslation(pos);
        getGuiNode().attachChild(panel);
        panel.runEffect(OptionPanel.EFFECT_OPEN);
        GuiGlobals.getInstance().requestFocus(panel);
    }
 
    /**
     *  Closes an open OptionPanel if one is currently open.  Does
     *  nothing otherwise.
     */
    public void close() {
        if( current != null ) {
            current.close();
        }
    }     
 
    /**
     *  Returns the currently displayed OptionPanel or null if
     *  no option panel is visible.
     */
    public OptionPanel getCurrent() {
        return current;
    }
 
    protected void setupBlocker( float z, Vector3f screenSize ) {
        if( blocker == null ) {
            Quad quad = new Quad(screenSize.x, screenSize.y);
            blocker = new Geometry("blocker", quad);
            ColorRGBA transparent = new ColorRGBA(0, 0, 0, 0);
            Material mat = GuiGlobals.getInstance().createMaterial(transparent, false).getMaterial();
            mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            blocker.setMaterial(mat);
            blocker.setQueueBucket(Bucket.Transparent);
 
            // Make sure that this consumes all mouse events
            // ie: it 'blocks'           
            MouseEventControl.addListenersToSpatial(blocker, ConsumingMouseListener.INSTANCE);
        } else {
            // Check to see if it's the same size
            Quad quad = (Quad)blocker.getMesh();
            if( quad.getWidth() != screenSize.x || quad.getHeight() != screenSize.y ) {
                quad.updateGeometry(screenSize.x, screenSize.y);
            }
        }
        blocker.setLocalTranslation(0, 0, z);
        getGuiNode().attachChild(blocker);
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
        this.guiNode = guiNode;
    }
    
    /**
     *  Returns the GUI node that will be used to display the option
     *  panel.  By default, this is SimpleApplication.getGuiNode().  
     */
    public Node getGuiNode() {
        if( guiNode != null ) {
            return guiNode;
        }
        Application app = getApplication();
        if( app instanceof SimpleApplication ) {
            this.guiNode = ((SimpleApplication)app).getGuiNode();
        }
        return guiNode;
    }

    @Override
    protected void initialize( Application app ) {
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
    }

    @Override
    public void update( float tpf ) {
        if( current != null ) {
            if( !current.isVisible() ) {
                current = null;
                blocker.removeFromParent();
            }
        }
    }

    @Override
    protected void disable() {
    }
}
