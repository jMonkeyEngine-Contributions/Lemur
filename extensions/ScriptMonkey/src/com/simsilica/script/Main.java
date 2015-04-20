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
 
package com.simsilica.script;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.StyleLoader;
import com.simsilica.lemur.style.Styles;
import java.io.File;

/**
 * 
 *  @author PSpeed
 */
public class Main extends SimpleApplication {
    
    private boolean hasFocus;
    private long lastFrameTime;
    private long offFocusFrameTime = 1000000000L / 60;  // ~60 FPS 

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    public Main() {
        super( new StatsAppState(), 
               new HudState(), 
               new CameraMovementState(),
               new SelectionState(),
               AppMode.getInstance(),
               new ScreenshotAppState("ScriptMonkey", System.currentTimeMillis()) );
    }

    @Override
    public void simpleInitApp() {
 
        // Initialize Lemur and setup some default key/input mappings.
        GuiGlobals.initialize(this);
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        MainFunctions.initializeDefaultMappings( inputMapper );
        CameraMovementFunctions.initializeDefaultMappings( inputMapper );

        inputMapper.activateGroup(MainFunctions.GROUP);
 
        AppMode.setMode(SelectionState.MODE_SELECTION);
 
        // Load up a custom style for the UI related stuff
        Styles styles = GuiGlobals.getInstance().getStyles();
        //new StyleLoader(styles).loadStyleResource( "/com/simsilica/script/glass-style.groovy" );       

        BaseStyles.loadGlassStyle();
 
        DirectionalLight sun = new DirectionalLight();
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        rootNode.addLight(ambient);
     
        // Setup the script state
        GroovyConsoleState scripts = new GroovyConsoleState();
        scripts.setInitBinding("app", this);
        scripts.setInitBinding("rootNode", this.getRootNode());
        scripts.setInitBinding("scripts", scripts);
        scripts.setInitBinding("sun", sun);
        scripts.setInitBinding("ambient", ambient);
        scripts.addInitializationScript(getClass().getResource("MainApi.groovy"));
        scripts.addInitializationScript(getClass().getResource("MathApi.groovy"));
        scripts.addInitializationScript(getClass().getResource("CameraApi.groovy"));
        scripts.addInitializationScript(getClass().getResource("AssetsApi.groovy"));
        scripts.addInitializationScript(getClass().getResource("MaterialApi.groovy"));
        scripts.addInitializationScript(getClass().getResource("SceneApi.groovy"));
        scripts.addInitializationScript(getClass().getResource("ModelApi.groovy"));
        scripts.addInitializationScript(getClass().getResource("FileApi.groovy"));
        scripts.addInitializationScript(getClass().getResource("InterfaceApi.groovy"));
        
        // Load any scripts in the "scripts" directory
        File dir = new File("scripts");
        if( dir.exists() ) {
            for( File f : dir.listFiles() ) {
                if( f.getName().toLowerCase().endsWith(".groovy") ) {
                    scripts.addInitializationScript(f);
                }
            }
        }
        
        // And finally attach the script managing state
        stateManager.attach(scripts);
    
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box1", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);

        b = new Box(1, 1, 1);
        geom = new Geometry("Box2", b);
        geom.setLocalTranslation(10, 0, 10 );

        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        
        b = new Box(1, 1, 1);
        geom = new Geometry("Box3", b);
        geom.setLocalTranslation(0, 10, 10 );

        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Cyan);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        
        b = new Box(1, 1, 1);
        geom = new Geometry("Box4", b);
        geom.setLocalTranslation(0, -10, 10 );

        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
 
        /*assetManager.registerLocator("localAssets", FileLocator.class);       
        Spatial model = assetManager.loadModel("rat/rat.mesh.j3o");
        rootNode.attachChild(model);*/
    }

    @Override
    public void gainFocus() {
        hasFocus = true;
    }

    @Override
    public void loseFocus() {
        hasFocus = false;
    }

    protected void sleep( long nanos ) {
        try {
            long ms = nanos/1000000L;
            Thread.sleep(ms);
        } catch( InterruptedException e ) {
            // checked exceptions are lame
            throw new RuntimeException("Sleep interrupted", e);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if( !hasFocus ) {
            // Then force limit the FPS to around 60
            
            // Sleep until the appropriate frame time has passed
            while( true ) {
                long time = System.nanoTime();
                long delta = time - lastFrameTime;
                if( delta < offFocusFrameTime ) {
                    sleep(offFocusFrameTime - delta);
                } else {
                    lastFrameTime = time;
                    break;
                }
            }      
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
