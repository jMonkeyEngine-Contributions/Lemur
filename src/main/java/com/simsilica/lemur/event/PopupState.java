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

import java.util.*;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bounding.*;
import com.jme3.input.event.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.*;
import com.jme3.scene.shape.Quad;

import com.simsilica.lemur.Command;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.style.ElementId;


/**
 *  Provides modal-style popup support where a single UI element can
 *  essentially 'take over' the screen.  The single pop-up will be the
 *  only thing that can receive events.  Outside mouse clicks will either
 *  close the panel or be ignored depending on how the popup was configured
 *  when opened.
 *
 *  @author    Paul Speed
 */
public class PopupState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(PopupState.class);

    /**
     *  Controls the behavior for clicks outside the specified popup.
     */
    public enum ClickMode {
        /**
         *  All clicks outside of the current popup will be consumed
         *  and ignored.  Use this if the user must specifically close
         *  the popup in another way (for example error or warning popups).
         */
        Consume,
 
        /**
         *  A click outside of the current popup will close the current popup and
         *  the event will otherwise propagate to whatever was below.  This is
         *  useful for things like popup menus or popup selectors where a click
         *  outside should simply pass through to the real UI.  In this case
         *  the 'modal' behavior is only to catch the outside click so that the
         *  popup can be closed. 
         */       
        Close,
                    
        /**
         *  A click outside of the current popup will be consumed but
         *  will also close the popup.  This could be used for certain types
         *  of message popups where clicking outside of the message should
         *  close the window but not activate underlying UI components.
         *  Especially if the 'blocker' geometry has been tinted in some
         *  way indicating that the underlying UI is unclickable.
         */
        ConsumeAndClose
    }; 

    private Node guiNode;
    
    private ColorRGBA defaultBackgroundColor = new ColorRGBA(0, 0, 0, 0);
    private List<PopupEntry> stack = new ArrayList<>();
    private PopupEntry current;
    
    public PopupState() {
    }
    
    public PopupState( Node guiNode ) {
        this.guiNode = guiNode;
    }
    
    public boolean hasActivePopups() {
        return isEnabled() && !stack.isEmpty();
    }

    /**
     *  Shows the specified spatial on the GUI node with a background blocker
     *  geometry that will automatically close the spatial when clicked.
     */
    public void showPopup( Spatial popup ) {
        showPopup(popup, ClickMode.Close, null, null);
    }

    /**
     *  Shows the specified spatial on the GUI node with a background blocker
     *  geometry that will automatically close the spatial when clicked.
     */
    public void showPopup( Spatial popup, Command<? super PopupState> closeCommand ) {
        showPopup(popup, ClickMode.Close, closeCommand, null);
    }

    /**
     *  Shows the specified spatial on the GUI node with a background blocker
     *  geometry that will consume all mouse events until the popup has been
     *  closed.
     */
    public void showModalPopup( Spatial popup ) {
        showPopup(popup, ClickMode.Consume, null, null);
    }

    /**
     *  Shows the specified spatial on the GUI node with a background blocker
     *  geometry that will consume all mouse events until the popup has been
     *  closed.
     */
    public void showModalPopup( Spatial popup, Command<? super PopupState> closeCommand ) {
        showPopup(popup, ClickMode.Consume, closeCommand, null);
    }
    
    /**
     *  Shows the specified spatial on the GUI node with a background blocker
     *  geometry that will consume all mouse events until the popup has been
     *  closed.
     */
    public void showModalPopup( Spatial popup, ColorRGBA backgroundColor ) {
        showPopup(popup, ClickMode.Consume, null, backgroundColor);
    }

    /**
     *  Shows the specified spatial on the GUI node with a background blocker
     *  geometry that will consume all mouse events until the popup has been
     *  closed.
     */
    public void showModalPopup( Spatial popup, Command<? super PopupState> closeCommand,
                                ColorRGBA backgroundColor ) {
        showPopup(popup, ClickMode.Consume, closeCommand, backgroundColor);
    }
    
    /**
     *  Shows the specified popup on the GUI node with the specified click mode
     *  determining how background mouse events will be handled.  An optional
     *  closeCommand will be called when the popup is closed.  An optional background
     *  color will be used for the background 'blocker' geometry.
     */
    public void showPopup( Spatial popup, ClickMode clickMode, Command<? super PopupState> closeCommand,
                           ColorRGBA backgroundColor ) {
                           
        PopupEntry entry = new PopupEntry(popup, clickMode, closeCommand, backgroundColor);
        stack.add(entry);
        current = entry;       
        current.show();
    }
    
    /**
     *  Returns true if the specified Spatial is still an active popup.
     */
    public boolean isPopup( Spatial s ) {
        return getEntry(s) != null;
    }
    
    /**
     *  Closes a previously opened popup.  Throws IllegalArgumentException if the
     *  specified popup is not open.
     */
    public void closePopup( Spatial popup ) {
        PopupEntry entry = getEntry(popup);
        if( entry == null ) {
            throw new IllegalArgumentException("Popup entry not found for:" + popup); 
        }
        close(entry); 
    }

    protected void close( PopupEntry entry ) {
        if( !stack.remove(entry) ) {
            return;
        }        
        entry.release();
        
        if( !stack.isEmpty() ) {
            current = stack.get(stack.size()-1);
        } else {
            current = null;
        }
    }

    protected PopupEntry getEntry( Spatial popup ) {
        for( PopupEntry entry : stack ) {
            if( entry.popup == popup ) {
                return entry;
            }
        }
        return null;
    }
 
    /**
     *  Calcules that maximum Z value given the current contents of
     *  the GUI node.
     */       
    protected float getMaxGuiZ() {
        BoundingVolume bv = getGuiNode().getWorldBound();
        return getMaxZ(bv);
    }
    
    protected float getMaxZ( BoundingVolume bv ) {
        if( bv instanceof BoundingBox ) {
            BoundingBox bb = (BoundingBox)bv;
            return bb.getCenter().z + bb.getZExtent();
        } else if( bv instanceof BoundingSphere ) {
            BoundingSphere bs = (BoundingSphere)bv;
            return bs.getCenter().z + bs.getRadius();
        } else if( bv == null ) {
            // Apparently this can happen for empty nodes...
            return 0;
        }
        Vector3f offset = bv.getCenter().add(0, 0, 1000); 
        return offset.z - bv.distanceTo(offset);
    } 

    protected float getMinZ( BoundingVolume bv ) {
        if( bv instanceof BoundingBox ) {
            BoundingBox bb = (BoundingBox)bv;
            return bb.getCenter().z - bb.getZExtent();
        } else if( bv instanceof BoundingSphere ) {
            BoundingSphere bs = (BoundingSphere)bv;
            return bs.getCenter().z - bs.getRadius();
        }
        Vector3f offset = bv.getCenter().add(0, 0, -1000); 
        return offset.z + bv.distanceTo(offset);  // untested
    } 

    protected GuiMaterial createBlockerMaterial( ColorRGBA color ) {
        GuiMaterial result = GuiGlobals.getInstance().createMaterial(color, false);
        Material mat = result.getMaterial();
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        return result; 
    }
    
    protected Geometry createBlocker( float z, ColorRGBA backgroundColor ) {
        Camera cam = getApplication().getCamera();
 
        // Get the inverse scale of whatever the current guiNode is so that
        // we can find a proper screen size
        float width = cam.getWidth() / guiNode.getLocalScale().x;
        float height = cam.getHeight() / guiNode.getLocalScale().y;
        
        Quad quad = new Quad(width, height);
        Geometry result = new Geometry("blocker", quad);
        GuiMaterial guiMat = createBlockerMaterial(backgroundColor);
        result.setMaterial(guiMat.getMaterial());
        //result.setQueueBucket(Bucket.Transparent); // no, it goes in the gui bucket.
        result.setLocalTranslation(0, 0, z);
        return result;
    }
 
    /**
     *  Returns the size of the screen based on the app's main camera size
     *  and the current scale of the guiNode.
     */   
    public Vector2f getGuiSize() {
        Camera cam = getApplication().getCamera();
        float width = cam.getWidth() / getGuiNode().getLocalScale().x;
        float height = cam.getHeight() / getGuiNode().getLocalScale().y;
        return new Vector2f(width, height);        
    }
 
    /**
     *  Positions the specified spatial so that it is in the center of 
     *  the GUI.
     */
    public void centerInGui( Spatial s ) {
        GuiControl control = s.getControl(GuiControl.class);
        if( control == null ) {
            // We could support this but I don't have the time today
            throw new UnsupportedOperationException("Only spatials with GuiControls are supported");
        }
        
        Vector2f guiSize = getGuiSize();
        Vector3f size = control.getSize();
        if( size.length() == 0 ) {
            // It may not have been fully realized yet so we'll make
            // a best guess
            size = control.getPreferredSize();
        }
        size = size.mult(s.getLocalScale());
        Vector3f pos = s.getWorldTranslation();
        Vector3f target = new Vector3f();
 
        target.x = guiSize.x * 0.5f - size.x * 0.5f;       
        target.y = guiSize.y * 0.5f + size.y * 0.5f;
        target.z = pos.z;
        
        s.move(target.subtract(pos));               
    }
 
    /**
     *  Moves the specified GUI element so that it is the most on the
     *  screen that it can be based on the current GUI size.  Returns true
     *  if the spatial was actually moved.
     */
    public boolean clampToGui( Spatial s ) {

        GuiControl control = s.getControl(GuiControl.class);
        if( control == null ) {
            // We could support this but I don't have the time today
            throw new UnsupportedOperationException("Only spatials with GuiControls are supported");
        }
        
        Vector2f guiSize = getGuiSize();
        Vector3f size = control.getSize();
        if( size.length() == 0 ) {
            // It may not have been fully realized yet so we'll make
            // a best guess
            size = control.getPreferredSize();
        }
        Vector3f pos = s.getWorldTranslation();
        Vector3f delta = new Vector3f();
        
        // Calculate a delta as necessary
        if( size.x > guiSize.x ) {
            // Center it
            float x = guiSize.x * 0.5f - size.x * 0.5f;
            delta.x = x - pos.x;
        } else if( pos.x < 0 ) {
            // Slide it right
            delta.x = -pos.x;
        } else if( pos.x + size.x > guiSize.x ) {
            // Slide it left
            float x = guiSize.x - size.x;
            delta.x = x - pos.x;
        }           
 
        // Y grows down so it's slightly different
        if( size.y > guiSize.y ) {
            float y = guiSize.y * 0.5f - size.y * 0.5f;
            delta.y = y - pos.y;
        } else if( pos.y > guiSize.y ) {
            // Shift it back down
            delta.y = guiSize.y - pos.y;
        } else if( pos.y - size.y < 0 ) {
            // Shift it up
            float y = size.y;
            delta.y = y - pos.y;
        }
 
        // We move based on a delta in case this is a child of some
        // other thing.  We calculate where we want it to be in world
        // space and the delta necessary to get us there and then move it.
        // All of that works fine as long as nothing is scaled.           
        s.move(delta);            
        
        return delta.length() != 0;    
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
    protected void onEnable() {
    }

    @Override
    public void update( float tpf ) {
        
        if( current != null ) {
            if( !current.isVisible() ) {
                close(current);
            }
        }
    }

    @Override
    protected void onDisable() {
    }
 
    private class PopupEntry {
        private Spatial popup;
        private ClickMode clickMode;
        private Command<? super PopupState> closeCommand;
        private ColorRGBA backgroundColor;
        private float zBase;
        private Geometry blocker;
        private GuiMaterial blockerMaterial;
        private BlockerListener blockerListener;
        
        public PopupEntry( Spatial popup, ClickMode clickMode, Command<? super PopupState> closeCommand,
                           ColorRGBA backgroundColor ) {
            this.popup = popup;
            this.clickMode = clickMode;
            this.closeCommand = closeCommand;
            this.backgroundColor = backgroundColor != null ? backgroundColor : defaultBackgroundColor;
            this.zBase = getMaxGuiZ() + 1;
            this.blocker = createBlocker(zBase, this.backgroundColor);
            MouseEventControl.addListenersToSpatial(blocker, new BlockerListener(this));
        }
 
        public boolean isVisible() {
            if( popup.getParent() == null ) {
                return false;
            }
            return true;
        }
        
        public void show() {
            float zOffset = getMinZ(popup.getWorldBound());
                            
            getGuiNode().attachChild(blocker);                        
            getGuiNode().attachChild(popup);
 
            float zPopup = zBase + 1;
            if( zOffset < 0 ) {
                // Move it out more for the negative zOffset.
                // If there is a positive zOffset then we let it stay
                zPopup = zPopup - zOffset;
            }
            
            // Make sure the popup spatial is above the blocker
            popup.move(0, 0, zPopup);

            if( popup instanceof Panel ) {
                // Play any open effects that it has
                ((Panel)popup).runEffect(Panel.EFFECT_OPEN);  
            }
            
            // Request access to the cursor
            GuiGlobals.getInstance().requestCursorEnabled(this);
            GuiGlobals.getInstance().requestFocus(popup);
        }
        
        public void release() {
            // Up to the effect to remove the popup... we'll do it if the
            // popup doesn't exist. 
            if( popup instanceof Panel && ((Panel)popup).hasEffect(Panel.EFFECT_CLOSE) ) {
                ((Panel)popup).runEffect(Panel.EFFECT_CLOSE);
                // Would be nice if there was a way to run something at the end 
                // of an effect just to be sure.
            } else {
                popup.removeFromParent();
            }
            blocker.removeFromParent();
            if( closeCommand != null ) {
                closeCommand.execute(PopupState.this);
            }
            
            // Release our cursor request
            GuiGlobals.getInstance().releaseCursorEnabled(this);
                       
            // And clear the focus if the popup is still in the focus chain
            GuiGlobals.getInstance().releaseFocus(popup);
        }        
    }
    
    private class BlockerListener implements MouseListener {

        private PopupEntry entry;
        
        public BlockerListener( PopupEntry entry ) {
            this.entry = entry;
        }
        
        public boolean isPassive() {
            switch(  entry.clickMode ) {
                case ConsumeAndClose:
                case Consume:
                    return false;
            }
            return true;      
        } 

        protected void handle( InputEvent event, boolean closeableEvent ) {
            switch( entry.clickMode ) {
                case Close:
                    if( closeableEvent ) {
                        close(entry);
                    }            
                    break;
                case ConsumeAndClose:
                    if( closeableEvent ) {
                        close(entry);
                    }
                    event.setConsumed();
                    break;
                case Consume:
                    event.setConsumed();
                    break;
            }           
        }

        public void mouseButtonEvent(MouseButtonEvent event, Spatial target, Spatial capture) {
            handle(event, true);
        }
    
        public void mouseEntered(MouseMotionEvent event, Spatial target, Spatial capture) {
            handle(event, false);
        }
    
        public void mouseExited(MouseMotionEvent event, Spatial target, Spatial capture) {
            handle(event, false);
        }
    
        public void mouseMoved(MouseMotionEvent event, Spatial target, Spatial capture) {
            handle(event, false);
        }
    }
    
}
