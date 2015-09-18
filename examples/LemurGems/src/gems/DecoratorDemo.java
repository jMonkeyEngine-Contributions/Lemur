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
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ProgressBar;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.ElementId;

/**
 *  The DecoratorDemo application for demonstrating 'decorating'
 *  an object with Lemur GUI elements.
 *
 *  @author pspeed
 */
public class DecoratorDemo extends SimpleApplication {

    public static void main(String[] args) {
        DecoratorDemo app = new DecoratorDemo();
        app.start();
    }

    public DecoratorDemo() {
        super(new StatsAppState(), new CameraMovementState(), new CameraToggleState());
    }

    @Override
    public void simpleInitApp() {
 
        // Initialize Lemur subsystems and setup the default
        // camera controls.   
        GuiGlobals.initialize(this);
        CameraMovementFunctions.initializeDefaultMappings(GuiGlobals.getInstance().getInputMapper());

        stateManager.getState(CameraMovementState.class).setEnabled(false);

        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(0.15f, -2, -0.5f).normalizeLocal());
        light.setColor(ColorRGBA.White.mult(2));
        rootNode.addLight(light);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.Gray);
        rootNode.addLight(ambient);

        GuiGlobals globals = GuiGlobals.getInstance();
        
        // Load the glass style and make it the default because it will look 
        // nicer for our GUI elements.  Note: this requires groovy-all for the
        // style language support.
        BaseStyles.loadGlassStyle();
        globals.getStyles().setDefaultStyle(BaseStyles.GLASS);
                
                
        // Add some instructions to the HUD
        Container window = new Container();
        window.addChild(new Label("Instructions", new ElementId("window.title")));
        window.addChild(new Label("Right-click a ball to increase progress."));
        window.addChild(new Label("Left-click a ball to decrease."));
        window.addChild(new Label("Press space to toggle camera movement mode."));
        guiNode.attachChild(window);
        window.move(0, cam.getHeight(), 0);
 
        // Now create the simple test scene
        for( int i = 0; i < 5; i++ ) {    
            Sphere mesh = new Sphere(9, 36, 1);
            Node ball = new Node("ball");
            ball.setLocalTranslation(-8 + i * 4, 0, -4);
            
            Geometry geom = new Geometry("BallSphere", mesh);
            ball.attachChild(geom);

            Material mat = globals.createMaterial(ColorRGBA.Blue, true).getMaterial();
            mat.setFloat("Shininess", 96);
            mat.setColor("Ambient", ColorRGBA.Blue);
            mat.setColor("Specular", ColorRGBA.White);
            geom.setMaterial(mat);

            // Add a progress bar to float above the ball.
            Node decorator = new Node("decorator");
            ball.attachChild(decorator);
            decorator.move(0, 1, 0);
            
            // We will create and attach just a progress bar but we could have
            // just as easily made a container with a label, a progress bar, a button,
            // etc..  The rest of the code would be roughly the same.
            final ProgressBar progress = new ProgressBar(); // defaults to glass style
            decorator.attachChild(progress);
 
            // GUI elements are used to being transparent by default so it's usually
            // best to stick them there.
            progress.setQueueBucket(Bucket.Transparent);
            
            // Progress bars are normally put inside of other UI containers and
            // grow/shrink to fit.  On their own, they have no preferred size and
            // so must be given one.
            progress.setPreferredSize(new Vector3f(200, 20, 1));

            // The progress bar defaults to UI size where 1 unit = 1 pixel.
            // In the 3D world space, we want this to be much smaller so we'll
            // arbitrarily pick 1 pixel = 0.01 world units.  So a 100 unit UI
            // element becomes 1 meter.
            // We set the scale on the decorator node parent because it would be
            // easier to add additional UI elements later and because setting the relative
            // position of the progress bar will be easier in its own 'space'.
            decorator.setLocalScale(0.01f);

            // Speaking of which, Lemur GUI elements grow down from their upper
            // left corner so let's nudge the progress bar up a little... it's
            // actual height.  And we'll center it by nudging it back half of the
            // width.  We could put these values in by hand but I want to show how
            // to calculate them from the GUI element so that it is easier to change
            // things above as needed.
            Vector3f pref = progress.getPreferredSize();
            
            // For GUI elements outside of containers, their calculated preferred size becomes
            // their size.
            progress.move(-pref.x * 0.5f, pref.y, -pref.z * 0.5f);

            // Set some progress onto the geometry (the geometry will be clickable
            // but not the node in general so as to avoid accepting clicks on the 
            // progress bar... just to show how that's avoided.)
            geom.setUserData("progress", 50);
            progress.setProgressValue(50);

            // Give the decorator node a billboard control so that it always faces
            // the user
            decorator.addControl(new BillboardControl());

            // Add a click handler to change the value of the progress bar
            MouseEventControl.addListenersToSpatial(geom,
                    new DefaultMouseListener() {
                        @Override
                        protected void click( MouseButtonEvent event, Spatial target, Spatial capture ) {
                            Material m = ((Geometry)target).getMaterial();
                            m.setColor("Diffuse", ColorRGBA.Red);
                            m.setColor("Ambient", ColorRGBA.Red);
                            if( event.getButtonIndex() == MouseInput.BUTTON_LEFT ) {
                                progress.setProgressValue(progress.getProgressValue() + 1);                            
                            } else {
                                progress.setProgressValue(progress.getProgressValue() - 1);                            
                            }
                        }
                    
                        @Override
                        public void mouseEntered( MouseMotionEvent event, Spatial target, Spatial capture ) {
                            Material m = ((Geometry)target).getMaterial();
                            m.setColor("Diffuse", ColorRGBA.Yellow);
                            m.setColor("Ambient", ColorRGBA.Yellow);
                        }

                        @Override
                        public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture ) {
                            Material m = ((Geometry)target).getMaterial();
                            m.setColor("Diffuse", ColorRGBA.Blue);
                            m.setColor("Ambient", ColorRGBA.Blue);
                        }                        
                    });
            

            rootNode.attachChild(ball);
        }
        
        
        // Make a simple "world" box
        Box b = new Box(20, 5, 20);
        Geometry world = new Geometry("world", b);
        world.move(0, 4, 0);
        Texture gradient = globals.loadTexture("/com/simsilica/lemur/icons/bordered-gradient.png", false, true);
        Material mat = globals.createMaterial(gradient, false).getMaterial();
        mat.setColor("Color", ColorRGBA.Cyan);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Front);
        world.setMaterial(mat);
        rootNode.attachChild(world);
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
