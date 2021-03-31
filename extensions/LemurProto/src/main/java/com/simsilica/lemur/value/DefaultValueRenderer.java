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

import com.google.common.base.Function;
import com.google.common.base.Functions;

import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.ValueRenderer;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;

/**
 *
 *
 *  @author    Paul Speed
 */
public class DefaultValueRenderer<T> implements ValueRenderer<T>, Cloneable {
 
    public static final ElementId DEFAULT_ID = new ElementId(Label.ELEMENT_ID);
    
    private ElementId elementId;
    private String style;
    private Function<? super T, String> toString;
 
    /**
     *  Creates a value renderer with no preconfigured style, element ID, or
     *  string function.  The renderer will pick up the style information that the using
     *  GUI element prefers. 
     */   
    public DefaultValueRenderer() {
        this(null, null, null);
    }

    /**
     *  Creates a value renderer with the specified string function but no
     *  configured element ID or style.  The renderer will pick up the style information 
     *  that the using GUI element prefers. 
     */
    public DefaultValueRenderer( Function<? super T, String> toString ) {
        this(null, null, toString);
    }
    
    /**
     *  Creates a value renderer with the preconfigured elementID and style.  For non-null
     *  elementId and style, this will ignore any style set by the using GUI element.  Values 
     *  will be converted to Strings using String.valueOf().
     */
    public DefaultValueRenderer( ElementId elementId, String style ) {
        this(elementId, style, null);
    }
    
    /**
     *  Creates a value renderer with a custom string function and the preconfigured 
     *  elementID and style.  This will ignore any style set by the using GUI element if
     *  elementId or style are non-null.  
     */
    public DefaultValueRenderer( ElementId elementId, String style, Function<? super T, String> toString ) {
        this.style = style;
        this.elementId = elementId;
        this.toString = toString;
    }
 
    @Override
    @SuppressWarnings("unchecked") 
    public DefaultValueRenderer<T> clone() {
        try {
            return (DefaultValueRenderer<T>)super.clone();
        } catch( CloneNotSupportedException e ) {
            throw new RuntimeException("Error cloning", e);
        }
    }
 
    /**
     *  Default implementation uses the specified style unless the renderer
     *  already has an elementId and style set. 
     */
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
     *  Sets the function that will convert values to Strings.  Set to null
     *  to use the default String.valueOf() behavior.
     */
    public void setStringTransform( Function<? super T, String> toString ) {
        this.toString = toString;
    }
    
    public Function<? super T, String> getStringTransform() {
        return toString;
    } 
 
    /**
     *  Sets a preconfigured ElementId for created Panels.  If this is 
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
     *  Sets a preconfigured style for created Panels.  If this is 
     *  non-null then the default implementation of configureStyle() will 
     *  ignore the configureStyle() style argument.
     */
    public void setStyle( String style ) {
        this.style = style;
    }
    
    public String getStyle() {
        return style;
    }

    /**
     *  Called by getView() and createView() to conver the value
     *  to a string.  Default implementation passes the value to the
     *  current string transform.
     */
    protected String valueToString( T value ) {
        return toString != null ? toString.apply(value) : String.valueOf(value);
    }

    /**
     *  Called by getView() to create the new view when the existing
     *  view doesn't exist.  Subclasses can override this to provide
     *  custom Label subclasses (like Button) to present the view.
     *  The default implementation creates a regular Label.
     */
    protected Label createView( T value, boolean selected ) {
        return new Label(valueToString(value), elementId, style);
    } 
    
    @Override
    public Panel getView( T value, boolean selected, Panel existing ) {
        if( existing == null ) {
            existing = createView(value, selected);
        } else {
            ((Label)existing).setText(valueToString(value));
        }
        return existing;
    }
}

 

