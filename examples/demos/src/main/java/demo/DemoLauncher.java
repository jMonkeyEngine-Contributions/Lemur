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

package demo;

import java.util.prefs.Preferences;

import com.jme3.app.*;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.style.BaseStyles;


/**
 *  The main bootstrap class for the SimEthereal networking example
 *  game. 
 *
 *  @author    Paul Speed
 */
public class DemoLauncher extends SimpleApplication {

    private Node logo;

    public static void main( String... args ) throws Exception {
        
        DemoLauncher main = new DemoLauncher();        
        AppSettings settings = new AppSettings(true);
 
        // Set some defaults that will get overwritten if
        // there were previously saved settings from the last time the user
        // ran.       
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setVSync(true);
        
        settings.load("Lemur Demos");
        settings.setTitle("Lemur Demos");
        //settings.setSettingsDialogImage("/sim-eth-es-splash-512.png");
        //settings.setUseJoysticks(true);
        /*
        try {
            BufferedImage[] icons = new BufferedImage[] {
                    ImageIO.read( TreeEditor.class.getResource( "/-icon-128.png" ) ),
                    ImageIO.read( TreeEditor.class.getResource( "/-icon-32.png" ) ),
                    ImageIO.read( TreeEditor.class.getResource( "/-icon-16.png" ) )
                };
            settings.setIcons(icons);
        } catch( IOException e ) {
            log.warn( "Error loading globe icons", e );
        }*/        
        
        main.setSettings(settings);
        
        main.start();
    }

    public DemoLauncher() {
        super(new StatsAppState(), new DebugKeysAppState(), new BasicProfilerState(false),
              new OptionPanelState(), // from Lemur
              new MainMenuState(),
              new ScreenshotAppState("", System.currentTimeMillis())); 
    }
        
    public void simpleInitApp() {        
        
        setPauseOnLostFocus(false);
        setDisplayFps(false);
        setDisplayStatView(false);
        
        GuiGlobals.initialize(this);
 
        GuiGlobals globals = GuiGlobals.getInstance();
        BaseStyles.loadGlassStyle();
        globals.getStyles().setDefaultStyle("glass");
 
                       
    }

}


