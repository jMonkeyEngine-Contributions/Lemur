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
package gems;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.input.MouseInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;

/**
 *  The PickDemo application for demonstrating Lemur-based scene picking.
 *
 *  @author pspeed
 */
public class PickDemo extends SimpleApplication {

    public static void main(String[] args) {
        PickDemo app = new PickDemo();
        app.start();
    }

    public PickDemo() {
        super(new StatsAppState(), new CameraMovementState(), new CameraToggleState());
    }

    @Override
    public void simpleInitApp() {
 
        // Initialize Lemur subsystems and setup the default
        // camera controls.   
        GuiGlobals.initialize(this);
        CameraMovementFunctions.initializeDefaultMappings(GuiGlobals.getInstance().getInputMapper());

        stateManager.getState(CameraMovementState.class).setEnabled(false);

        // Now create the simple test scene
        for( int i = 0; i < 5; i++ ) {    
            Box b = new Box(1, 1, 1);
            Geometry geom = new Geometry("Box", b);

            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Blue);
            geom.setMaterial(mat);
            geom.setLocalTranslation(-8 + i * 4, 0, -4);


            MouseEventControl.addListenersToSpatial(geom,
                    new DefaultMouseListener() {
                        @Override
                        protected void click( MouseButtonEvent event, Spatial target, Spatial capture ) {
                            Material m = ((Geometry)target).getMaterial();
                            m.setColor("Color", ColorRGBA.Red);
                            if( event.getButtonIndex() == MouseInput.BUTTON_LEFT ) {
                                target.move(0, 0.1f, 0);
                            } else {
                                target.move(0, -0.1f, 0);
                            }                            
                        }
                    
                        @Override
                        public void mouseEntered( MouseMotionEvent event, Spatial target, Spatial capture ) {
                            Material m = ((Geometry)target).getMaterial();
                            m.setColor("Color", ColorRGBA.Yellow);
                        }

                        @Override
                        public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture ) {
                            Material m = ((Geometry)target).getMaterial();
                            m.setColor("Color", ColorRGBA.Blue);
                        }                        
                    });
            

            rootNode.attachChild(geom);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
