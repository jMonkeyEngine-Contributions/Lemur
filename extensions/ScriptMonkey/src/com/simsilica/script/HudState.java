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
 * SOFTWARE, Even IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.script;


import com.jme3.app.*;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.*;
import com.simsilica.lemur.component.BorderLayout.Position;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.event.MouseAppState;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;



/**
 *  Provides some basic gui layout support for HUD elements. 
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class HudState extends BaseAppState
{
    private ViewPort view;
    private Vector3f size;
    private boolean lit;
    private Node main;
    private Container container;
    private Container east;
    private Container west;
    private Container north;
    private Container south;

    public HudState()
    {
    }

    public Node getRoot()
    {
        return main;
    }

    public void toggleHud()
    {
        setEnabled( !isEnabled() );
    }

    public Container getEast()
    {
        if( east == null )
            {
            east = new Container();
            east.setLayout( new SpringGridLayout( Axis.Y, Axis.X, FillMode.None, FillMode.Even ) );
            east.setInsets( new Insets3f( 5, 5, 5, 5 ) );            
            container.addChild( east, Position.East );            
            }
        return east;
    }
    
    public Container getWest()
    {
        if( west == null )
            {
            west = new Container();
            west.setLayout( new SpringGridLayout( Axis.Y, Axis.X, FillMode.None, FillMode.Even ) );
            west.setInsets( new Insets3f( 5, 5, 5, 5 ) );            
            container.addChild( west, Position.West );            
            }
        return west;
    }
    
    public Container getNorth()
    {
        if( north == null )
            {
            north = new Container();
            north.setLayout( new BorderLayout() );
            container.addChild( north, Position.North );            
            }
        return north;
    }
    
    public Container getSouth()
    {
        if( south == null )
            {
            south = new Container();
            south.setLayout( new BorderLayout() );
            container.addChild( south, Position.South );            
            }
        return south;
    }

    @Override
    protected void initialize( Application app ) 
    {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addDelegate( MainFunctions.F_HUD, this, "toggleHud" );
        
        Camera cam = app.getCamera().clone();
        cam.setParallelProjection(true);
        size = new Vector3f( cam.getWidth(), cam.getHeight(), 0 );
        
        main = new Node( "HUD" ); 
        main.setQueueBucket(Bucket.Gui);
 
        view = app.getRenderManager().createPostView( "Hud ViewPort", cam);
        view.setEnabled(isEnabled());
        view.setClearFlags(false, true, true);
        view.attachScene( main );
 
        // Make sure our viewport is setup properly
        GuiGlobals.getInstance().setupGuiComparators(view);

        // Make sure this viewport gets mouse events
        getState(MouseAppState.class).addCollisionRoot( main, view );
 
        // Setup a basic container for standard layout... for anything
        // that cares to use it.       
        container = new Container( new BorderLayout() );
        container.setPreferredSize( new Vector3f(cam.getWidth(), cam.getHeight(), 0) );
        container.setLocalTranslation( 0, cam.getHeight(), 0 );
        main.attachChild(container);
 
        if( lit )
            {
            // Add some lighting 
            DirectionalLight light = new DirectionalLight();
            light.setDirection( new Vector3f( 1, -0.5f, -1.5f ).normalizeLocal() );
            main.addLight(light);

            AmbientLight ambient = new AmbientLight();
            ambient.setColor( ColorRGBA.Gray );
            main.addLight(ambient);
            }
        
        // Have to add an empty geometry to the HUD because JME has
        // a bug in the online versions and I'd rather not go directly to
        // source.
        Label temp = new Label("");
        getNorth().addChild(temp);        
 
 
        /*
        Just a test container... putting the real stuff somewhere else in a sec.
        Container test = new Container(new ElementId("window.container"), "glass"); 
        System.out.println( "Container layout:" + test.getLayout() );
        test.addChild(new Label("Test Title", new ElementId("window.title.label"), "glass"));
        test.addChild(new Button("Test Button 1", new ElementId("window.button"), "glass"));
        test.addChild(new Button("Test Button 2", new ElementId("window.button"), "glass"));
        test.addChild(new Button("Test Button 3", new ElementId("window.button"), "glass"));
        test.addChild(new Button("Test Button 4", new ElementId("window.button"), "glass"));
        getWest().addChild(test);
        */
        
        main.updateLogicalState(1);
        main.updateGeometricState();
    }

    @Override
    protected void cleanup( Application app ) 
    {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate( MainFunctions.F_HUD, this, "toggleHud" );
        
        app.getRenderManager().removePostView( view );
    }

    @Override
    protected void enable()
    {
        view.setEnabled(true);
    }

    @Override
    protected void disable()
    {
        view.setEnabled(false);
        main.updateGeometricState();
    }

    @Override
    public void update( float tpf )
    {
        main.updateLogicalState(tpf);
    }

    @Override
    public void render(RenderManager rm)
    {
        main.updateGeometricState();
    }

}
