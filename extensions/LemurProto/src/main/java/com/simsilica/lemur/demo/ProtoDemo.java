/*
 * $Id$
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

package com.simsilica.lemur.demo;

import com.jme3.app.BasicProfilerState;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Action;
import com.simsilica.lemur.ActionButton;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.ColorChooser;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.OptionPanelState;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.ElementId;


/**
 *
 *
 *  @author    Paul Speed
 */
public class ProtoDemo extends SimpleApplication {

    private ListBox<String> listBox;
    private VersionedList<String> testList = new VersionedList<String>();

    public static void main( String... args ) {
        ProtoDemo main = new ProtoDemo();
        main.start();
    }
 
    public ProtoDemo() {
        super(new StatsAppState(), new DebugKeysAppState(), new BasicProfilerState(false),
              new OptionPanelState("glass"),
              new ScreenshotAppState("", System.currentTimeMillis())); 
    }
    
    @Override
    public void simpleInitApp() {
    
        // Initialize the globals access so that the defualt
        // components can find what they need.
        GuiGlobals.initialize(this);

        BaseStyles.loadGlassStyle();
 
        // Create a window to hold our demo elements and add a title label
        Container window = new Container("glass");
        window.addChild(new Label("Test List", new ElementId("title"), "glass"));

        // Make some test data for the list. 
        for( int i = 0; i < 10; i++ ) {
            testList.add("Item " + (i+1));
        }

        // Create a list box for the test data and add it to the window         
        listBox = new ListBox<>(testList, "glass");
        window.addChild(listBox);          
 
        // Create some actions
        final Action add = new Action("Add") {
                @Override
                public void execute( Button b ) {
                    testList.add("New Item " + (testList.size() + 1));
                }
            };
        final Action delete = new Action("Delete") {
                @Override
                public void execute( Button b ) {
                    Integer selected = listBox.getSelectionModel().getSelection();
                    if( selected != null && selected < testList.size() ) {
                        testList.remove((int)selected);
                    }
                }
            };
        final Action cancel = new Action("Cancel") {
                @Override
                public void execute( Button b ) {
                }
            };
 
        // Safe delete is a special action because it will pop open 
        // the option panel and delegate to the other delete action.           
        final Action safeDelete = new Action("Safe Delete") {
                @Override
                public void execute( Button b ) {
                    Integer selected = listBox.getSelectionModel().getSelection();
                    if( selected == null || selected >= testList.size() ) {
                        return;
                    }
                    String val = testList.get(selected);
                    OptionPanelState ops = stateManager.getState(OptionPanelState.class);
                    ops.show("Delete", "Really delete '" + val + "'?", delete, cancel);                        
                }
            };            
 
        // Create the button panel at the bottom of the window
        Container buttons = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.Even, FillMode.Even));
        window.addChild(buttons);       
        buttons.addChild(new ActionButton(add, "glass"));
        buttons.addChild(new ActionButton(safeDelete, "glass"));
        buttons.addChild(new ActionButton(delete, "glass"));
        
 
        // And stick the window somewhere we will see it       
        window.setLocalTranslation(300, 600, 0);
        guiNode.attachChild(window);
        
        
        window = new Container("glass");
        window.addChild(new Label("Test Color Chooser", new ElementId("title"), "glass"));
        ColorChooser colors = window.addChild(new ColorChooser("glass"));
        colors.setPreferredSize(new Vector3f(300, 90, 0)); 
        
        // And stick the window somewhere we will see it       
        window.setLocalTranslation(100, 400, 0);
        guiNode.attachChild(window);
    } 
}



