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

package com.simsilica.lemur;

import com.google.common.base.Function;

import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.value.DefaultValueRenderer;

/**
 *  Factory methods for creating standard/common ValueRenderers. 
 *  
 *  @author    Paul Speed
 */
public class ValueRenderers {
    
    public static <T> DefaultValueRenderer<T> toStringRenderer( String nullValue ) {
        return new DefaultValueRenderer<>(toString(nullValue));
    }
    
    public static <T> DefaultValueRenderer<T> toStringRenderer( String nullValue, ElementId elementId, String style ) {
        return new DefaultValueRenderer<>(elementId, style, toString(nullValue));
    } 

    public static <T> DefaultValueRenderer<T> formattedRenderer( String format, String nullValue ) {
        return new DefaultValueRenderer<>(formatString(format, nullValue));
    }
    
    public static <T> DefaultValueRenderer<T> formattedRenderer( String format, String nullValue, ElementId elementId, String style ) {
        return new DefaultValueRenderer<>(elementId, style, formatString(format, nullValue));
    } 
 
    // Some useful toString functions
    public static Function<Object, String> toString( String nullValue ) {
        return new SafeToString(nullValue);
    }
    
    public static Function<Object, String> formatString( String format ) {
        return formatString(format, null);
    }
    
    public static Function<Object, String> formatString( String format, String nullValue ) {
        return new FormatString(format, nullValue);
    } 
    
    /**
     *  Similar to Functions.toStringFunction() except this will use
     *  a provided string in the case of a null value.
     */
    public static class SafeToString implements Function<Object, String> {
        private String nullString;
        
        public SafeToString( String nullString ) {
            this.nullString = nullString;
        }
        
        public String apply( Object value ) {
            return value == null ? nullString : String.valueOf(value);
        }
    }
 
    /**
     *  Converts a value to a String by passing it into String.format()
     *  using the supplied format string.  Similar to SafeToString, a nullString
     *  value can be provided that will be used for null values.  If nullString
     *  is not specified then the value is passed to String.format() as is.  
     */   
    public static class FormatString implements Function<Object, String> {
        private String format;
        private String nullString;
        
        public FormatString( String format, String nullString ) {
            this.format = format;
            this.nullString = nullString;
        }
        
        public String apply( Object value ) {
            if( value == null && nullString != null ) {
                return nullString;
            }
            return String.format(format, value);
        }
    }     
}
