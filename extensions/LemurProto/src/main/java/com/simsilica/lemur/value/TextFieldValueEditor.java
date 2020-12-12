/*
 * $Id$
 * 
 * Copyright (c) 2020, Simsilica, LLC
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

package com.simsilica.lemur.value;

import java.util.Objects;

import org.slf4j.*;

import com.google.common.base.Function;

import com.simsilica.lemur.*;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.focus.*;
import com.simsilica.lemur.text.*; 
import com.simsilica.lemur.style.ElementId; 

/**
 *  Uses a text field and a pair of object-string and string-object
 *  transform functions to implement a ValueEditor.
 *
 *  @author    Paul Speed
 */
public class TextFieldValueEditor<T> implements ValueEditor<T> {

    static Logger log = LoggerFactory.getLogger(TextFieldValueEditor.class);

    private T object;
    private long version;

    private Function<Object, String> toString;
    private Function<String, T> toObject;

    private ElementId elementId;
    private String style;
    private TextField textField;
    private DocumentModelFilter model;
    private VersionedReference<DocumentModel> modelRef;
    private FocusObserver focusObserver = new FocusObserver();
    
    private boolean active;

    public TextFieldValueEditor( Function<Object, String> toString, Function<String, T> toObject ) {
        this.toString = toString;
        this.toObject = toObject;
        this.model = new DocumentModelFilter();
        this.modelRef = model.createReference(); 
    }
    
    protected void incrementVersion() {
        version++;
    }

    @Override
    public VersionedReference<T> createReference() {
        return new VersionedReference<>(this);
    }
    
    @Override
    public long getVersion() {
        return version;
    }
    
    @Override
    public void setObject( T object ) {
        if( Objects.equals(this.object, object) ) {
            return;
        }
        this.object = object;
        resetText();
        incrementVersion();
    }
    
    @Override
    public T getObject() {
        return object;
    }

    @Override
    public boolean updateState( float tpf ) {
        if( modelRef != null && modelRef.update() ) {
            // We don't support live editing in this context
            // but if we did, here's where we could do it.
        }
        return active;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
 
    /**
     *  Sets a preconfigured ElementId for created editors.  If this is 
     *  non-null then the default implementation of configureStyle() will 
     *  ignore the configureStyle() elementId argument.
     */
    public void setElementId( ElementId elementId ) {
        this.elementId = elementId;
    }
 
    public ElementId getElementId() {
        return elementId;
    }
    
    /**
     *  Sets a preconfigured style for created editors.  If this is 
     *  non-null then the default implementation of configureStyle() will 
     *  ignore the configureStyle() style argument.
     */
    public void setStyle( String style ) {
        this.style = style;
    }
    
    public String getStyle() {
        return style;
    }
     
    @Override
    public void configureStyle( ElementId elementId, String style ) {
        if( this.elementId == null ) {
            this.elementId = elementId;
        }
        if( this.style == null ) {
            this.style = style;
        } 
    }
 
    /**
     *  Sets the document model for this editor.  Note: this should not
     *  be called while a value is being edited as it will cause the old
     *  editor to be invalidated.
     */
    public void setDocumentModelFilter( DocumentModelFilter model ) {
        if( Objects.equals(this.model, model) ) {
            return;
        }
        if( model == null ) {
            model = new DocumentModelFilter();
        }        
        this.model = model;
        if( textField != null ) {
            textField.getControl(GuiControl.class).removeFocusChangeListener(focusObserver);
            // Blow  it away and recreate it next time
            textField = null;            
        }
        modelRef = model.createReference();
    }
    
    public DocumentModelFilter getDocumentModelFilter() {
        return model;
    }
 
    protected TextField createTextField() {
        if( elementId != null ) {    
            return new TextField(model, elementId, style);
        } 
        return new TextField(model); 
    }
 
    @Override
    public Panel startEditing( T initialValue ) {
        if( textField == null ) {
            textField = createTextField();
            textField.getControl(GuiControl.class).addFocusChangeListener(focusObserver);            
        }
        // We don't call setObject() because we want to avoid 
        // incrementing the version until the value changes 'for real'.
        this.object = initialValue;
        resetText();
        active = true;
        return textField;
    }
    
    @Override
    public Panel getEditor() {
        return textField;
    }

    /** 
     *  Resets the text field to reflect the current model value.
     */
    protected void resetText() {
        model.setText(toString.apply(object));
    }
    
    protected void stopEditing( boolean canceled ) {
        if( !canceled ) {
            String value = model.getText();
            try {
                T object = toObject.apply(value);
                setObject(object);
            } catch( NumberFormatException e ) {
                log.warn("Error parsing:" + value, e);
                // then just leave it as the original value
                resetText();
            }
        }
        active = false;
    }
    
    protected class FocusObserver implements FocusChangeListener {        
        public void focusGained( FocusChangeEvent event ) {
        }
        
        public void focusLost( FocusChangeEvent event ) {
            stopEditing(false);        
        }
    }
}


