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

package com.simsilica.lemur.list;

import com.google.common.base.Function;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;


/**
 *
 *  @author    Paul Speed
 */
public class DefaultCellRenderer<T> implements CellRenderer<T>, Cloneable {

    private String style;
    private ElementId elementId;
    private Function<T, String> transform;
    
    public DefaultCellRenderer() {
        this(new ElementId(Button.ELEMENT_ID), Styles.ROOT_STYLE, null);
    }
    
    public DefaultCellRenderer( String style ) {
        this(new ElementId(Button.ELEMENT_ID), style, null);
    }
    
    public DefaultCellRenderer( ElementId elementId, String style ) {
        this(elementId, style, null);
    }
    
    public DefaultCellRenderer( ElementId elementId, String style, Function<T, String> transform ) {
        this.style = style;
        this.elementId = elementId;
        this.transform = transform;
    }
    
    @Override
    @SuppressWarnings("unchecked") 
    public DefaultCellRenderer<T> clone() {
        try {
            return (DefaultCellRenderer<T>)super.clone();
        } catch( CloneNotSupportedException e ) {
            throw new RuntimeException("Error cloning", e);
        }
    }
    
    public void setTransform( Function<T, String> transform ) {
        this.transform = transform;
    }
    
    public Function<T, String> getTransform() {
        return transform;
    } 
 
    public ElementId getElement() {
        return elementId;
    }
    
    public String getStyle() {
        return style;
    }
    
    protected String valueToString( T value ) {
        if( transform != null ) {
            return transform.apply(value);
        }
        return String.valueOf(value);
    }

    @Override
    public Panel getView( T value, boolean selected, Panel existing ) {
        if( existing == null ) {
            existing = new Button(valueToString(value), elementId, style);
        } else {
            ((Button)existing).setText(valueToString(value));
        }
        return existing;
    }
}


