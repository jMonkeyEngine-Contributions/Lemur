/*
 * $Id$
 *
 * Copyright (c) 2012-2012 jMonkeyEngine
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

package com.simsilica.lemur.demo;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.LayerComparator;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.DynamicInsetsComponent;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.event.DefaultCursorListener;
import com.simsilica.lemur.event.DragHandler;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class BasicDemo extends SimpleApplication {

    // Define some model references we will use in
    // update.
    private VersionedReference<Double> redRef;
    private VersionedReference<Double> greenRef;
    private VersionedReference<Double> blueRef;
    private VersionedReference<Double> alphaRef;
    private VersionedReference<Boolean> showStatsRef;
    private VersionedReference<Boolean> showFpsRef;

    private ColorRGBA boxColor = ColorRGBA.Blue.clone();

    private Panel test;
    private TextField tf;
    private String strInsertText = "Inserted ";

    public static void main(String[] args) {
        BasicDemo app = new BasicDemo();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        // Initialize the globals access so that the defualt
        // components can find what they need.
        GuiGlobals.initialize(this);

        // Remove the flycam because we don't want it in this
        // demo
        stateManager.detach( stateManager.getState(FlyCamAppState.class) );

        // Now, let's create some styles in code.
        // For this demo, we'll just give some of the elements
        // different backgrounds as we define a "glass" style.
        // We also define a custom element type called "spacer" which
        // picks up a specific style.
        Styles styles = GuiGlobals.getInstance().getStyles();
        styles.getSelector( Slider.THUMB_ID, "glass" ).set( "text", "[]", false );
        styles.getSelector( Panel.ELEMENT_ID, "glass" ).set( "background",
                                new QuadBackgroundComponent(new ColorRGBA(0, 0.25f, 0.25f, 0.5f)) );
        styles.getSelector( Checkbox.ELEMENT_ID, "glass" ).set( "background",
                                new QuadBackgroundComponent(new ColorRGBA(0, 0.5f, 0.5f, 0.5f)) );
        styles.getSelector( "spacer", "glass" ).set( "background",
                                new QuadBackgroundComponent(new ColorRGBA(1, 0.0f, 0.0f, 0.0f)) );
        styles.getSelector( "header", "glass" ).set( "background",
                                new QuadBackgroundComponent(new ColorRGBA(0, 0.75f, 0.75f, 0.5f)) );
        styles.getSelector( "header", "glass" ).set( "shadowColor",
                                                     new ColorRGBA(1, 0f, 0f, 1) );
/*        styles.getSelector( "header", "glass" ).set( "shadowOffset", 
                                                     new Vector3f(3, -3, 3) );*/

        // Now construct some HUD panels in the "glass" style that
        // we just configured above.
        Container hudPanel = new Container("glass");
        hudPanel.setLocalTranslation( 5, cam.getHeight() - 50, 0 );
        guiNode.attachChild(hudPanel);

        // Create a top panel for some stats toggles.
        Container panel = new Container("glass");
        hudPanel.addChild(panel);

        panel.setBackground(new QuadBackgroundComponent(new ColorRGBA(0,0.5f,0.5f,0.5f),5,5, 0.02f, false));
        panel.addChild( new Label( "Stats Settings", new ElementId("header"), "glass" ) );
        panel.addChild( new Panel( 2, 2, ColorRGBA.Cyan, "glass" ) ).setUserData( LayerComparator.LAYER, 2 );

        // Adding components returns the component so we can set other things
        // if we want.
        Checkbox temp = panel.addChild( new Checkbox( "Show Stats" ) );
        temp.setChecked(true);
        showStatsRef = temp.getModel().createReference();

        temp = panel.addChild( new Checkbox( "Show FPS" ) );
        temp.setChecked(true);
        showFpsRef = temp.getModel().createReference();


        // Custom "spacer" element type
        hudPanel.addChild( new Panel( 10f, 10f, new ElementId("spacer"), "glass" ) );

        // Create a second panel in the same overall HUD panel
        // that lets us tweak things about the cube.
        panel = new Container("glass");
        panel.setBackground(new QuadBackgroundComponent(new ColorRGBA(0,0.5f,0.5f,0.5f),5,5, 0.02f, false));
        // Custom "header" element type.
        panel.addChild( new Label( "Cube Settings", new ElementId("header"), "glass" ) );
        panel.addChild( new Panel( 2, 2, ColorRGBA.Cyan, "glass" ) ).setUserData( LayerComparator.LAYER, 2 );
        panel.addChild( new Label( "Red:" ) );
        final Slider redSlider = new Slider("glass");
        redSlider.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.5f,0.1f,0.1f,0.5f),5,5, 0.02f, false));
        redRef = panel.addChild( redSlider ).getModel().createReference();
        CursorEventControl.addListenersToSpatial(redSlider, new DefaultCursorListener() {
                @Override
                public void cursorMoved( CursorMotionEvent event, Spatial target, Spatial capture ) {
                    System.out.println( "event:" + event );
                    Vector3f cp = event.getCollision().getContactPoint();
                    cp = redSlider.worldToLocal(cp, null);
                    System.out.println( "Range value:" + redSlider.getValueForLocation(cp) );      
                }

            });
        
        panel.addChild( new Label( "Green:" ) );
        greenRef = panel.addChild( new Slider("glass") ).getModel().createReference();
        panel.addChild( new Label( "Blue:" ) );
        blueRef = panel.addChild( new Slider(new DefaultRangedValueModel(0,100,100), "glass") ).getModel().createReference();
        panel.addChild( new Label( "Alpha:" ) );
        alphaRef = panel.addChild( new Slider(new DefaultRangedValueModel(0,100,100), "glass") ).getModel().createReference();
        hudPanel.addChild(panel);

        // Custom "spacer" element type
        hudPanel.addChild( new Panel( 10f, 10f, new ElementId("spacer"), "glass" ) );
        
        // Test text entry
        panel = new Container("glass");
        panel.addChild( new Label( "Test entry:", "glass" ) );
        hudPanel.addChild(panel);
        
        guiNode.attachChild(hudPanel);
        
        // Increase the default size of the hud to be a little wider
        // if it would otherwise be smaller.  Height is unaffected.
        Vector3f hudSize = new Vector3f(200,0,0);
        hudSize.maxLocal(hudPanel.getPreferredSize());
        hudPanel.setPreferredSize( hudSize );

        // Note: after next nightly, this will also work:
        // hudPanel.setPreferredSize( new Vector3f(200,0,0).maxLocal(hudPanel.getPreferredSize()) );

        // Something in scene
        Box box = new Box(1, 1, 1);
        Geometry geom = new Geometry( "Box", box );
        Material mat = new Material( assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor( "Color", boxColor );
        mat.getAdditionalRenderState().setBlendMode( BlendMode.Alpha );
        geom.setMaterial(mat);
        rootNode.attachChild( geom );

        // A draggable bordered panel
        Container testPanel = new Container();
        testPanel.setPreferredSize( new Vector3f(200, 200,0) );
        testPanel.setBackground( TbtQuadBackgroundComponent.create( "/com/simsilica/lemur/icons/border.png",
                                                                    1, 2, 2, 3, 3, 0, false ) );
        Label test = testPanel.addChild( new Label( "Border Test" ) );
        test.setShadowColor(ColorRGBA.Red);

        // Center the text in the box.
        test.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0.5f, 0.5f, 0.5f));
        testPanel.setLocalTranslation( 400, 400, 0 );

        CursorEventControl.addListenersToSpatial(testPanel, new DragHandler());
        guiNode.attachChild( testPanel );
    }

    @Override
    public void simpleUpdate(float tpf) {
    
        if( showStatsRef.update() ) {
            setDisplayStatView( showStatsRef.get() );
        }
        if( showFpsRef.update() ) {
            setDisplayFps( showFpsRef.get() );
        }

        boolean updateColor = false;
        if( redRef.update() )
            updateColor = true;
        if( greenRef.update() )
            updateColor = true;
        if( blueRef.update() )
            updateColor = true;
        if( alphaRef.update() )
            updateColor = true;
        if( updateColor ) {
            boxColor.set( (float)(redRef.get()/100.0),
                          (float)(greenRef.get()/100.0),
                          (float)(blueRef.get()/100.0),
                          (float)(alphaRef.get()/100.0) );
        }
    }
}
