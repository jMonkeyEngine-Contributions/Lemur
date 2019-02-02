/*
 * $Id$
 * 
 * Copyright (c) 2019, Simsilica, LLC
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

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.DynamicInsetsComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;

/**
 *
 *
 *  @author    Paul Speed
 */
public class DynamicInsetsState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(DynamicInsetsState.class);

    private Container window;
    
    private IconComponent icon;
    
    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    public DynamicInsetsState() {
    }
     
    @Override   
    protected void initialize( Application app ) {
    }
    
    @Override   
    protected void cleanup( Application app ) {
    }
 
    protected Button createButton( String text ) {
        Button b = new Button(text);
        b.setIcon(icon.clone());
        b.setTextHAlignment(HAlignment.Center);
        b.setTextVAlignment(VAlignment.Center);
        return b;
    }
 
    @Override   
    protected void onEnable() {
 
        // We'll wrap the text in a window to make sure the layout is working
        window = new Container();
        window.addChild(new Label("Dynamic Insets Demo", new ElementId("window.title.label")));

        Container content = window.addChild(new Container());

        icon = new IconComponent("test24.png");
         
        Button b;
        Container buttons;
        
        buttons = content.addChild(new Container());
        b = buttons.addChild(createButton("Centered 1"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0.5f, 0.5f, 0.5f));
        b = buttons.addChild(createButton("Centered 2"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0.5f, 0.5f, 0.5f));
        b = buttons.addChild(createButton("Centered 3"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0.5f, 0.5f, 0.5f));


        buttons = content.addChild(new Container());
        b = buttons.addChild(createButton("Left V-Centered 1"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 1));
        b = buttons.addChild(createButton("Left V-Centered 2"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 1));
        b = buttons.addChild(createButton("Left V-Centered 3"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 1));


        buttons = content.addChild(new Container());
        b = buttons.addChild(createButton("Stretched V-Centered 1"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 0));
        b = buttons.addChild(createButton("Stretched V-Centered 2"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 0));
        b = buttons.addChild(createButton("Stretched V-Centered 3"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 0));

        buttons = content.addChild(new Container());
        b = buttons.addChild(createButton("Stretched Interborder-centered 1"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 0));
        b.setBorder(b.getBackground());
        b.setBackground(new DynamicInsetsComponent(0.5f, 0.5f, 0.5f, 0.5f));
        b = buttons.addChild(createButton("Stretched Interborder-centered 2"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 0));
        b.setBorder(b.getBackground());
        b.setBackground(new DynamicInsetsComponent(0.5f, 0.5f, 0.5f, 0.5f));
        b = buttons.addChild(createButton("Stretched Interborder-centered 3"));
        b.setInsetsComponent(new DynamicInsetsComponent(0.5f, 0, 0.5f, 0));
        b.setBorder(b.getBackground());
        b.setBackground(new DynamicInsetsComponent(0.5f, 0.5f, 0.5f, 0.5f));


        Vector3f pref = content.getPreferredSize();
        System.out.println("pref:" + pref);
        content.setPreferredSize(new Vector3f(600, 600, pref.z));                 

 
        // Add a close button to both show that the layout is working and
        // also because it's better UX... even if the popup will close if
        // you click outside of it.
        window.addChild(new ActionButton(new CallMethodAction("Close", 
                                                              window, "removeFromParent")));
        
        // Position the window and pop it up                                                             
        window.setLocalTranslation(400, getApplication().getCamera().getHeight() * 0.95f, 50);
        getState(PopupState.class).showPopup(window, closeCommand);    
    }
    
    @Override   
    protected void onDisable() {
        window.removeFromParent();
    }
     
    private class CloseCommand implements Command<Object> {
        
        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(DynamicInsetsState.this);
        }
    }
}



