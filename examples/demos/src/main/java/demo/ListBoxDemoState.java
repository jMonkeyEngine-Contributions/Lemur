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

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModel;

/**
 *  A demo of the ListBox element.
 *
 *  @author    Paul Speed
 */
public class ListBoxDemoState extends BaseAppState {
 
    private Container window;
    
    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    private ListBox listBox;
 
    // Just to give added items a unique suffix
    private int nextItem = 1;
    
    public ListBoxDemoState() {
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
        window.addChild(new Label("List Box Demo", new ElementId("window.title.label")));
 
        // Create a multiline text field with our document model
        listBox = window.addChild(new ListBox());
        listBox.setVisibleItems(5);
 
        for( int i = 0; i < 10; i++ ) {        
            listBox.getModel().add("Item " + nextItem);
            nextItem++;
        }        
 
        // Add some actions that will manipulate the document model independently
        // of the text field
        Container buttons = window.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "add")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "insert")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "remove")));
 
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
 
    protected void add() {
        // Add it to the end
        listBox.getModel().add("Added item " + (nextItem++));
    }

    protected void insert() {
        Integer selection = listBox.getSelectionModel().getSelection();
        if( selection == null ) {
            return;
        }
        listBox.getModel().add(selection, "Inserted item " + (nextItem++));
    }
    
    protected void remove() {
        Integer selection = listBox.getSelectionModel().getSelection();
        if( selection == null ) {
            return;
        }
        listBox.getModel().remove((int)selection);
    }
    
    private class CloseCommand implements Command<Object> {
        
        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(ListBoxDemoState.this);
        }
    }
}



