/*
 * $Id$
 * 
 * Copyright (c) 2018, Simsilica, LLC
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

import com.google.common.base.Function;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.*;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.ConsumingMouseListener;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DragHandler;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModel;

/**
 *  A demo of various DragHandler setups.
 *
 *  @author    Paul Speed
 */
public class DragDemoState extends BaseAppState {
 
    private Container window;
    private Container dragWindow;
    
    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    private TabbedPanel tabs;
    private int nextTabNumber = 1;
    
    private Label statusLabel;
    private VersionedReference<TabbedPanel.Tab> selectionRef; 
    
    public DragDemoState() {
    }
     
    @Override   
    protected void initialize( Application app ) {
    }
    
    @Override   
    protected void cleanup( Application app ) {
    }

    protected void createAllDragPopup() {
        // Make a window where the whole window is draggable
        dragWindow = new Container();
        
        dragWindow.addChild(new Label("Drag Demo: Whole Window", new ElementId("window.title.label")));
        CursorEventControl.addListenersToSpatial(dragWindow, new DragHandler());
        MouseEventControl.addListenersToSpatial(dragWindow, ConsumingMouseListener.INSTANCE);
 
        dragWindow.addChild(new Label("This is a window that can be dragged\nclicking anywhere in the window."));
         
        // Position the window and pop it up                                                             
        dragWindow.setLocalTranslation(400, 600, 100);                 
        getState(PopupState.class).showPopup(dragWindow);  
    }

    protected void createTitleDragPopup() {
        dragWindow = new Container();
        MouseEventControl.addListenersToSpatial(dragWindow, ConsumingMouseListener.INSTANCE);
        
        Label title = dragWindow.addChild(new Label("Drag Demo: Title Bar", 
                                          new ElementId("window.title.label")));
                                          
        DragHandler dragHandler = new DragHandler();
        dragHandler.setDraggableLocator(new Function<Spatial, Spatial>() {
                public Spatial apply( Spatial spatial ) {
                    return spatial.getParent();
                }
            });
        CursorEventControl.addListenersToSpatial(title, dragHandler);
 
        dragWindow.addChild(new Label("This is a window that can only be dragged\nby clicking in the title bar."));
         
        // Position the window and pop it up                                                             
        dragWindow.setLocalTranslation(400, 600, 100);                 
        getState(PopupState.class).showPopup(dragWindow);
    }
    
    protected void createDraggableChildren() {
        dragWindow = new Container();
        MouseEventControl.addListenersToSpatial(dragWindow, ConsumingMouseListener.INSTANCE);
        
        Label title = dragWindow.addChild(new Label("Drag Demo: Children", 
                                                    new ElementId("window.title.label")));
 
        dragWindow.addChild(new Label("This is a window has children\nthat can be dragged."));


        DragHandler dragHandler = new DragHandler();
        Label drag;
        drag = dragWindow.addChild(new Label("Drag Me", new ElementId("window.title.label")));
        CursorEventControl.addListenersToSpatial(drag, dragHandler);
        drag = dragWindow.addChild(new Label("Drag Me", new ElementId("window.title.label")));
        CursorEventControl.addListenersToSpatial(drag, dragHandler);
        drag = dragWindow.addChild(new Label("Drag Me", new ElementId("window.title.label")));
        CursorEventControl.addListenersToSpatial(drag, dragHandler);

         
        // Position the window and pop it up                                                             
        dragWindow.setLocalTranslation(400, 600, 100);                 
        getState(PopupState.class).showPopup(dragWindow);
    }

    protected void createRotatedDraggableChildren() {
        dragWindow = new Container();
        MouseEventControl.addListenersToSpatial(dragWindow, ConsumingMouseListener.INSTANCE);
        
        Label title = dragWindow.addChild(new Label("Drag Demo: Rotated Children", 
                                                    new ElementId("window.title.label")));
 
        dragWindow.addChild(new Label("This is a rotated window with children\nthat can be dragged."));


        DragHandler dragHandler = new DragHandler();
        Label drag;
        drag = dragWindow.addChild(new Label("Drag Me", new ElementId("window.title.label")));
        CursorEventControl.addListenersToSpatial(drag, dragHandler);
        drag = dragWindow.addChild(new Label("Drag Me", new ElementId("window.title.label")));
        CursorEventControl.addListenersToSpatial(drag, dragHandler);
        drag = dragWindow.addChild(new Label("Drag Me", new ElementId("window.title.label")));
        CursorEventControl.addListenersToSpatial(drag, dragHandler);

         
        // Position the window and pop it up                                                             
        dragWindow.setLocalTranslation(400, 600, 100);
        dragWindow.rotate(0, 0, FastMath.QUARTER_PI * 0.5f);                 
        getState(PopupState.class).showPopup(dragWindow);
    }

    @Override   
    protected void onEnable() {
 
        window = new Container();
        window.addChild(new Label("Drag Demo Popups", new ElementId("window.title.label")));
        
        window.addChild(new ActionButton(new CallMethodAction("Draggable Anywhere", 
                                                              this, "createAllDragPopup")));
        window.addChild(new ActionButton(new CallMethodAction("Draggable Titlebar", 
                                                              this, "createTitleDragPopup")));
        window.addChild(new ActionButton(new CallMethodAction("Draggable Detached Children", 
                                                              this, "createDraggableChildren")));
        window.addChild(new ActionButton(new CallMethodAction("Rotated Draggable Detached Children", 
                                                              this, "createRotatedDraggableChildren")));
        window.addChild(new ActionButton(new CallMethodAction("Close", 
                                                              window, "removeFromParent")));
                                                              
        window.setLocalTranslation(300, 300, 100);                 
        getState(PopupState.class).showModalPopup(window, closeCommand);    
    }
    
    @Override   
    protected void onDisable() {
        if( dragWindow != null ) {
            dragWindow.removeFromParent();
        }
        window.removeFromParent();
    }
 
    @Override
    public void update( float tpf ) {
    }
 
    private class CloseCommand implements Command<Object> {
        
        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(DragDemoState.this);
        }
    }
}



