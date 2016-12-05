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
import com.simsilica.lemur.text.DocumentModelFilter;
import com.simsilica.lemur.text.TextFilters;

/**
 *  A demo of a Textfield that allows direct entry as well as provides
 *  some buttons for manipulating the DocumentModel separately.
 *
 *  @author    Paul Speed
 */
public class FormattedTextEntryDemoState extends BaseAppState {
 
    private Container window;
 
    /**
     *  A command we'll pass to the label pop-up to let
     *  us know when the user clicks away.
     */
    private CloseCommand closeCommand = new CloseCommand();
 
    public FormattedTextEntryDemoState() {
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
 
        window.addChild(new Label("Filtered input:"));
        Container examples = window.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Last)));

        TextField textField;
        DocumentModelFilter doc; 
 
        // Add an alpha example       
        doc = new DocumentModelFilter();        
        doc.setInputTransform(TextFilters.alpha());
        examples.addChild(new Label("Alpha only:")); 
        textField = examples.addChild(new TextField(doc), 1);
        textField.setPreferredWidth(300);

        // Add a numeric example
        doc = new DocumentModelFilter();        
        doc.setInputTransform(TextFilters.numeric());
        examples.addChild(new Label("Numeric only:")); 
        textField = examples.addChild(new TextField(doc), 1);
 
        // A new subsection for the output filters
        window.addChild(new Label("Filtered Output:"));
        examples = window.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Last)));

        // Add an all-caps example
        doc = new DocumentModelFilter();
        doc.setOutputTransform(TextFilters.upperCaseTransform());        
        examples.addChild(new Label("All Caps:")); 
        textField = examples.addChild(new TextField(doc), 1);
        examples.addChild(new Label("-> unfiltered:"));
        textField = examples.addChild(new TextField(doc.getDelegate()), 1);

        // Add a constant-char example
        doc = new DocumentModelFilter();
        doc.setOutputTransform(TextFilters.constantTransform('*'));        
        examples.addChild(new Label("Obscured:")); 
        textField = examples.addChild(new TextField(doc), 1);
        examples.addChild(new Label("-> unfiltered:"));
        textField = examples.addChild(new TextField(doc.getDelegate()), 1);


        // A new subsection for the built in filtered text support like PasswordField
        window.addChild(new Label("Standard Elements:"));
        examples = window.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Last)));
 
        PasswordField pword;
        
        // Add a PasswordField example
        examples.addChild(new Label("Password:"));
        pword = examples.addChild(new PasswordField(""), 1);
        examples.addChild(new Label("-> unfiltered:"));
        textField = examples.addChild(new TextField(pword.getDocumentModel()), 1);         

        // Add a PasswordField example
        examples.addChild(new Label("Password Alt:"));
        pword = examples.addChild(new PasswordField(""), 1);
        pword.setOutputCharacter('#');
        examples.addChild(new Label("-> unfiltered:"));
        textField = examples.addChild(new TextField(pword.getDocumentModel()), 1);         

        // Add a PasswordField example
        // Note: don't do this in real life unless you want easily hackable passwords
        examples.addChild(new Label("Alpha-numeric Password:"));
        pword = examples.addChild(new PasswordField(""), 1);
        pword.setAllowedCharacters(TextFilters.isLetterOrDigit());
        examples.addChild(new Label("-> unfiltered:"));
        textField = examples.addChild(new TextField(pword.getDocumentModel()), 1);         
        
        // Add a close button to both show that the layout is working and
        // also because it's better UX... even if the popup will close if
        // you click outside of it.
        window.addChild(new ActionButton(new CallMethodAction("Close", 
                                                              window, "removeFromParent")));
 
        // Position the window and pop it up                                                             
        window.setLocalTranslation(400, 600, 100);                 
        getState(PopupState.class).showPopup(window, closeCommand);    
    }
    
    @Override   
    protected void onDisable() {
        window.removeFromParent();
    } 
    
    private class CloseCommand implements Command<Object> {
        
        public void execute( Object src ) {
            getState(MainMenuState.class).closeChild(FormattedTextEntryDemoState.this);
        }
    }
}



