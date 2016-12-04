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

package com.simsilica.lemur;

import com.google.common.base.Predicate;

import com.jme3.font.BitmapFont;

import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleAttribute;
import com.simsilica.lemur.style.Styles;
import com.simsilica.lemur.text.*;

/**
 *  A special TextField implementation that displays an obscured version
 *  of the password the user has entered.  In all other ways, it acts
 *  exactyl like a TextField.
 *
 *  @author    Paul Speed
 */
public class PasswordField extends TextField {

    public static final String ELEMENT_ID = "password.textField";

    private DocumentModel delegate;
    private DocumentModelFilter wrapper;
    private Predicate<Character> allowed;
    private char outputChar;

    public PasswordField( String text ) {
        this(new DefaultDocumentModel(text), true, new ElementId(ELEMENT_ID), null);
    }

    public PasswordField( DocumentModel model ) {
        this(model, true, new ElementId(ELEMENT_ID), null);
    }

    public PasswordField( String text, String style ) {
        this(new DefaultDocumentModel(text), true, new ElementId(ELEMENT_ID), style);
    }

    public PasswordField( String text, ElementId elementId ) {
        this(new DefaultDocumentModel(text), true, elementId, null);
    }
    
    public PasswordField( String text, ElementId elementId, String style ) {
        this(new DefaultDocumentModel(text), true, elementId, style);
    }

    public PasswordField( DocumentModel model, String style ) {
        this(model, true, new ElementId(ELEMENT_ID), style);
    }

    protected PasswordField( DocumentModel model, boolean applyStyles, ElementId elementId, String style ) {
        super(model, applyStyles, elementId, style);
    }

    @Override
    protected void setDocumentModel( DocumentModel model ) {
 
        // Grab the delegate because this is what getText() and setText() will call
        this.delegate = model;
        
        // Wrap the real model into something we can filter
        this.wrapper = new DocumentModelFilter(model);
      
        // Wire in the standard filters
        wrapper.setOutputTransform(TextFilters.constantTransform('*'));
 
        super.setDocumentModel(wrapper);       
    }

    @Override
    public DocumentModel getDocumentModel() {
        return delegate;
    }
 
    /**
     *  Presets the password text to some value.
     *  
     *  <p>Note: if you find yourself calling this with a user's actual 
     *  password text then you are probably doing something wrong with 
     *  password management.</p>
     *
     *  <p>Note: setting text this way will bypass the input filtering.
     *  This is so that it's possible to use special marker characters
     *  to tell the difference between a real user-entered password and
     *  a token inserted to represent a hash of a previously entered
     *  password.  This token can then purposely have text that is not
     *  allowed in a normal password so as to be distinguishable from
     *  a real user-entered password.</p> 
     */   
    @Override
    @StyleAttribute(value="text", lookupDefault=false)
    public void setText( String s ) {
        delegate.setText(s);
    }
    
    /**
     *  Returns the raw hidden password text.
     */
    @Override
    public String getText() {
        return delegate == null ? null : delegate.getText();
    }

    /**
     *  Returns the formatted text as the user will see it.
     */
    public String getDisplayText() {
        return wrapper == null ? null : wrapper.getText();
    }

    /**
     *  Sets the character used to obscure output.  If set to null then
     *  the default '*' will be used.
     */
    @StyleAttribute(value="outputCharacter", lookupDefault=false)
    public void setOutputCharacter( Character c ) {
        this.outputChar = c == null ? '*' : c;
        wrapper.setOutputTransform(TextFilters.constantTransform(c));
    }
    
    public char getOutputCharacter() {
        return outputChar;        
    }

    /**
     *  Sets a predicate that returns true for characters that are allowed in
     *  the password field.  All other input will be skipped.
     */
    @StyleAttribute(value="allowedCharacters", lookupDefault=false)
    public void setAllowedCharacters( Predicate<Character> allowed ) {
        this.allowed = allowed;
        wrapper.setInputTransform(TextFilters.charFilter(allowed));   
    }
    
    public Predicate<Character> getAllowedCharacters() {
        return allowed;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[text=" + getDisplayText() + ", color=" + getColor() + ", elementId=" + getElementId() + "]";
    }

}
