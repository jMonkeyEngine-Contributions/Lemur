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

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModel;

/**
 *  A demo of the IconComponent.
 *
 *  @author    Paul Speed
 */
public class PanelAlphaDemoState extends BaseAppState {
 
    private Container window;
    
    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    private Label description;
    private VersionedReference<Double> alpha;
    
    public PanelAlphaDemoState() {
    }
     
    @Override   
    protected void initialize( Application app ) {
    }
    
    @Override   
    protected void cleanup( Application app ) {
    }
 
    @Override   
    protected void onEnable() {
 
        // We'll wrap the text in a window to make sure the layout is working
        window = new Container();
        window.addChild(new Label("Panel Alpha Demo", new ElementId("window.title.label")));
         

        description = window.addChild(new Label("Drag the slider to change the window alpha:" + 0.5));
        IconComponent icon = new IconComponent("spacebugs-titlecard.jpg");
        icon.setIconScale(0.5f);
        icon.setHAlignment(HAlignment.Center);
        icon.setVAlignment(VAlignment.Top);
        description.setIcon(icon);
        
        
        Slider slider = window.addChild(new Slider(new DefaultRangedValueModel(0.1, 1, 0.5)));
        alpha = slider.getModel().createReference();
 
        // Add a close button to both show that the layout is working and
        // also because it's better UX... even if the popup will close if
        // you click outside of it.
        window.addChild(new ActionButton(new CallMethodAction("Close", 
                                                              window, "removeFromParent")));
        
        // Position the window and pop it up                                                             
        window.setLocalTranslation(400, getApplication().getCamera().getHeight() * 0.9f, 50);                 
        getState(PopupState.class).showPopup(window, closeCommand);
        
        refreshAlpha();    
    }

    protected void refreshAlpha() {
        String s = String.format("Drag the slider to change the window alpha: %.2f", alpha.get());
        description.setText(s);
        window.setAlpha((float)(double)alpha.get());   
    }
 
    @Override
    public void update( float tpf ) {
        if( alpha.update() ) {
            refreshAlpha();
        }   
    }
    
    @Override   
    protected void onDisable() {
        window.removeFromParent();
    }
     
    private class CloseCommand implements Command<Object> {
        
        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(PanelAlphaDemoState.this);
        }
    }
}



