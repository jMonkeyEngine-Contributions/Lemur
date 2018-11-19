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

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.GuiComparator;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;

import com.simsilica.lemur.*;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.ConsumingMouseListener;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DragHandler;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.event.PickState;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModel;

/**
 *  
 *
 *  @author    Paul Speed
 */
public class ViewPortDemoState extends BaseAppState {
 
    static Logger log = LoggerFactory.getLogger(ViewPortDemoState.class);
 
    private Container window;
    
    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    private ViewPort vp;
    private Node vpRoot;
    private Container vpWindow;
    
    public ViewPortDemoState() {
    }
     
    @Override   
    protected void initialize( Application app ) {
    }
    
    @Override   
    protected void cleanup( Application app ) {
    }

    /**
     *  Creates an ortho viewport in the specified section of the screen.
     *  The viewport will be scaled such that adding things to its root
     *  scene will be in 1:1 pixel space.
     */
    protected void createGuiViewPort( int x1, int y1, int x2, int y2, ColorRGBA bgColor ) {
 
        if( vp != null ) {
            disposeViewPort();
        }

        log.info("Creating demo GUI ViewPort"); 
    
        // Setup the viewport for a cloned camera
        Camera cam = getApplication().getCamera().clone();        
        float width = cam.getWidth();
        float height = cam.getHeight();
        cam.setViewPort(x1/width, x2/width, y1/height, y2/height);

        // Setup the ortho project.  I've found it doesn't play
        // nice unless the range spans 0.  We'll move its root node
        // so that 0,0 is the lower left corner like the regular gui bucket.
        float near = -1000;
        float far = 1000;
        float w = x2 - x1;
        float h = y2 - y1;        
        cam.setParallelProjection(true);
        cam.setFrustum(near, far, -w/2, w/2, h/2, -h/2);
        
        // Create the actual viewport
        vp = getApplication().getRenderManager().createPostView("ViewPort Demo", cam);
        vp.setClearFlags(true, true, true);
        vp.setBackgroundColor(bgColor);

        // We want the transparent bucket to act exactly like the GUI
        // bucket with respect to back-to-front sorting.
        vp.getQueue().setGeometryComparator(Bucket.Transparent, new GuiComparator());
 
        // Give it a root node that we can use to attach things.
        // Translate it so that 0,0 is the lower left corner.
        // Note: the is NOT in the Gui Bucket... but the transparent bucket.
        vpRoot = new Node("VP Root");
        vpRoot.setQueueBucket(Bucket.Transparent);
        vpRoot.setLocalTranslation(-w/2, -h/2, 0);
        vp.attachScene(vpRoot);
 
        // Let Lemur know to do picking in this viewport       
        getState(PickState.class).addCollisionRoot(vp, PickState.PICK_LAYER_GUI);
    }

    protected void disposeViewPort() {
        if( vp == null ) {
            return;
        }
        log.info("Disposing of demo GUI ViewPort"); 
        getApplication().getRenderManager().removePostView(vp);
        getState(PickState.class).removeCollisionRoot(vp);
        vp = null;
    }

    protected void setupViewPortTest() {        
        createGuiViewPort(500, 200, 1000, 500, ColorRGBA.DarkGray);
        
        // Create a window in the viewport
        vpWindow = new Container();
        CursorEventControl.addListenersToSpatial(vpWindow, new DragHandler());
        
        vpWindow.addChild(new Label("ViewPort Child", new ElementId("window.title.label")));
        vpWindow.addChild(new Button("Click Me 1"));            
        vpWindow.addChild(new Button("Click Me 2"));            
        vpWindow.addChild(new Button("Click Me 3"));
 
        // Position the window such that it sits with its lower left
        // corner exactly at the origin.  It helps prove that the ViewPort
        // root node is setup correctly.
        Vector3f pref = vpWindow.getPreferredSize();
        vpWindow.setLocalTranslation(0, pref.y, 0);              
        vpRoot.attachChild(vpWindow);        
    }

    @Override   
    protected void onEnable() {
        window = new Container();
        window.addChild(new Label("ViewPort Demos", new ElementId("window.title.label")));
        
        window.addChild(new ActionButton(new CallMethodAction("Simple Test", 
                                                              this, "setupViewPortTest")));
        window.addChild(new ActionButton(new CallMethodAction("Close", 
                                                              window, "removeFromParent")));
                                                              
        window.setLocalTranslation(300, 300, 100);                 
        getState(PopupState.class).showModalPopup(window, closeCommand);    
    }
    
    @Override   
    protected void onDisable() {    
        window.removeFromParent();
        disposeViewPort();
    }
 
    @Override
    public void update( float tpf ) {
        if( vpRoot != null ) {
            vpRoot.updateLogicalState(tpf);
        }
    }
    
    @Override
    public void render( RenderManager rm ) {
        if( vpRoot != null ) {
            vpRoot.updateGeometricState();
        }
    }
    
    @Override
    public void stateDetached( AppStateManager stateManager ) {
        // Here we have a legitimate use for stateDetached(). 
        // (the only one I've found to date)
        // The issue here is that when this AppState is detached, it
        // won't actually get onDisable()/cleanup() called until the
        // start of the next frame.  That's potentially enough time for the
        // ViewPort to be rendered again.  If some aspect of the node's
        // state has changed since then (say because you had focused on
        // a button but then focused on a different button to close it...)
        // then you will randomly get the error about "scene not updated for
        // rendering... blah blah".
        // So, on stateDetached() we do the safest thing we can and simply
        // remove the root from the ViewPort.  onDisable() will come along
        // later and remove the ViewPort itself.
        // Technically we could probably get away with disposing of the ViewPort
        // here but it's better to limit what we do here.  stateDetached() is
        // called from the thread that detached the state... which may or may
        // not be the render thread.  detachScene() is only updating a list
        // so we might consider it is the least dangerous of all options.   
        if( vp != null ) {
            vp.detachScene(vpRoot);
        }
    }
 
    private class CloseCommand implements Command<Object> {        
        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(ViewPortDemoState.this);
        }
    }
}



