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
 *  A demo of a Textfield that allows direct entry as well as provides
 *  some buttons for manipulating the DocumentModel separately.
 *
 *  @author    Paul Speed
 */
public class TextEntryDemoState extends BaseAppState {
 
    private Container window;
    
    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    private TextField textField;
    private DocumentModel document;
    
    public TextEntryDemoState() {
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
        window.addChild(new Label("Word Wrapped Text", new ElementId("window.title.label")));
 
        // Create a multiline text field with our document model
        textField = window.addChild(new TextField("Initial text."));
        textField.setSingleLine(false);
        document = textField.getDocumentModel();
        
        // Setup some preferred sizing since this will be the primary
        // element in our GUI
        textField.setPreferredWidth(500);
        textField.setPreferredLineCount(10);
 
        // Add some actions that will manipulate the document model independently
        // of the text field
        Container buttons = window.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "home")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "end")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "forward")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "back")));
        buttons.addChild(new ActionButton(new CallMethodAction(this, "insert"))); 
        buttons.addChild(new ActionButton(new CallMethodAction(this, "delete"))); 
 
        // Add a close button to both show that the layout is working and
        // also because it's better UX... even if the popup will close if
        // you click outside of it.
        //window.addChild(new ActionButton(new CallMethodAction("Close", 
        //                                                      window, "removeFromParent")));
        // Creating a modified close button to more easily test that we really
        // do lose focus and don't keep accepting 'space' to click this button.
        window.addChild(new ActionButton(new CallMethodAction(this, "close")));
 
        // Position the window and pop it up                                                             
        window.setLocalTranslation(400, 400, 100);                 
        getState(PopupState.class).showPopup(window, closeCommand);    
    }
    
    @Override   
    protected void onDisable() {
        window.removeFromParent();
    }
 
    protected void home() {
        document.home(false);
    }
    
    protected void end() {
        document.end(false);
    }
    
    protected void forward() {
        document.right();
    }
    
    protected void back() {
        document.left();
    }
 
    protected void insert() {
        document.insert("add");
    }
    
    protected void delete() {
        document.delete();
    }
 
    /**
     *  Added this to test the bug where elements removed from the 
     *  scene graph would still retain focus... thus their focus actions
     *  like 'space' to activate buttons would still be active.
     */
    protected void close() {
        System.out.println("close");
        getState(MainMenuState.class).closeChild(this);
    }
    
    private class CloseCommand implements Command<Object> {
        
        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(TextEntryDemoState.this);
        }
    }
}



