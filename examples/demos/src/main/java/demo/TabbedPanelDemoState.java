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

import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

import com.simsilica.lemur.*;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.ConsumingMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModel;

/**
 *  A demo of the TabbedPanel element.
 *
 *  @author    Paul Speed
 */
public class TabbedPanelDemoState extends BaseAppState {
 
    private Container window;
    
    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    private TabbedPanel tabs;
    private int nextTabNumber = 1;
    
    private Label statusLabel;
    private VersionedReference<TabbedPanel.Tab> selectionRef; 
    
    public TabbedPanelDemoState() {
    }
     
    @Override   
    protected void initialize( Application app ) {
    }
    
    @Override   
    protected void cleanup( Application app ) {
    }

    @Override   
    protected void onEnable() {
 
        // Put the demo in a nice window... that also consumes the
        // mouse events over it so it doesn't auto-close when clicked
        // outside of interactive child elements.
        window = new Container();
        window.addChild(new Label("Tabbed Panel Demo", new ElementId("window.title.label")));
        MouseEventControl.addListenersToSpatial(window, ConsumingMouseListener.INSTANCE);
 
        tabs = window.addChild(new TabbedPanel());
        tabs.setInsets(new Insets3f(5, 5, 5, 5));
        selectionRef = tabs.getSelectionModel().createReference();

        for( int i = 0; i < 3; i++ ) {
            add();
        }
        
        statusLabel = window.addChild(new Label("Status"));
        statusLabel.setInsets(new Insets3f(2, 5, 2, 5)); 
 
        // Add some actions that will manipulate the document model independently
        // of the text field
        Container buttons = window.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)));
        buttons.setInsets(new Insets3f(5, 5, 5, 5));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "first")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "add")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "insert")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "remove")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "last")));
 
        // Add a close button to both show that the layout is working and
        // also because it's better UX... even if the popup will close if
        // you click outside of it.
        window.addChild(new ActionButton(new CallMethodAction("Close", 
                                                              window, "removeFromParent")));
        
        // Position the window and pop it up                                                             
        window.setLocalTranslation(400, 400, 100);                 
        getState(PopupState.class).showPopup(window, closeCommand);    
    }
    
    @Override   
    protected void onDisable() {
        window.removeFromParent();
    }
 
    @Override
    public void update( float tpf ) {
        if( selectionRef.update() ) {
            statusLabel.setText("Selected " + selectionRef.get().getTitle());
        }
    }
 
    protected Container createTabContents( String name ) {
        Container contents = new Container();
        Label label = contents.addChild(new Label("A test label for tab:" 
                                    + name + ".\nThere are others like it.\nBut this one is mine."));
        label.setInsets(new Insets3f(5, 5, 5, 5));
        return contents;
    }
    
    protected void first() {
        if( !tabs.getTabs().isEmpty() ) {
            tabs.setSelectedTab(tabs.getTabs().get(0));
        }
    }    
 
    protected void add() {
        String name = "Tab " + nextTabNumber;
        tabs.addTab(name, createTabContents(name));
        nextTabNumber++;
    }

    protected void insert() {
        int index = tabs.getTabs().indexOf(tabs.getSelectedTab());
        String name = "Tab " + nextTabNumber;
        tabs.insertTab(index, name, createTabContents(name));
        nextTabNumber++;
    }
    
    protected void remove() {
        tabs.removeTab(tabs.getSelectedTab());
    }

    protected void last() {
        List<TabbedPanel.Tab> list = tabs.getTabs();
        if( !list.isEmpty() ) {        
            tabs.setSelectedTab(list.get(list.size()-1));
        }
    }
    
    private class CloseCommand implements Command<Object> {
        
        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(TabbedPanelDemoState.this);
        }
    }
}



