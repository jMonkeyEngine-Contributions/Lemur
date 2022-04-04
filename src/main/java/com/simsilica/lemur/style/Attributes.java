/*
 * $Id$
 *
 * Copyright (c) 2012-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur.style;

import java.util.*;



/**
 *  The attribute settings for a particular style selector.
 *
 *  @author    Paul Speed
 */
public class Attributes {

    private Styles parent;
    private Map<String, Object> values = new HashMap<String, Object>();

    public Attributes( Styles parent ) {
        this.parent = parent;
    }

    protected Map<String, Object> getValues() {
        return values;
    }

    protected void applyNew( Attributes atts ) {

        // Note: applyNew is called in highest to lowest priority.
        //       Things that fill the values map now should override
        //       later things.

        for( Map.Entry<String,Object> e : atts.values.entrySet() ) {
            Object existing = values.get(e.getKey());
            if( existing instanceof Map && e.getValue() instanceof Map ) {
                // Need to merge the old and the new
                values.put(e.getKey(), mergeMap((Map)existing, (Map)e.getValue()));
            } else if( existing == null && !values.containsKey(e.getKey()) ) {
                // Null check above is not enough because the map might have a
                // null value that should override subsequent attempts to
                // set the value.
                values.put(e.getKey(), e.getValue());
            }
            // Else we ignore it
        }
    }

    @SuppressWarnings("unchecked")
    protected Map mergeMap( Map high, Map low ) {
        Map result = new HashMap();
        result.putAll(high);
        for( Object o : low.entrySet() ) {
            Map.Entry e = (Map.Entry)o;
            if( !result.containsKey(e.getKey()) ) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    /**
     *  Like applyNew except that it returns a new Attributes object
     *  and leaves the original intact if a merge is necessary.
     *  If the specified attributes to merge are empty then this
     *  attributes object is returned.
     */
    protected Attributes merge( Attributes atts ) {
        if( atts.isEmpty() ) {
            return this;
        }
        Attributes result = new Attributes(parent);
        result.values.putAll(this.values);
        result.applyNew(atts);
        return result;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean hasAttribute( String key ) {
        return values.containsKey(key);
    }

    public void set( String attribute, Object value ) {
        set( attribute, value, true );
    }

    public void set( String attribute, Object value, boolean overwrite ) {
        if( !overwrite && values.containsKey(attribute) )
            return;
        values.put( attribute, value );
    }

    @SuppressWarnings("unchecked")
    public <T> T get( String attribute ) {
        return (T)values.get(attribute);
    }

    public <T> T get( String attribute, Class<T> type ) {
        return get(attribute, type, true);
    }

    @SuppressWarnings("unchecked")
    public <T> T get( String attribute, Class<T> type, boolean lookupDefault ) {
        Object result = values.get(attribute);
        if( result == null && lookupDefault ) {
            result = parent.getDefault(type);
        }
        // Normally type.cast(result) would work here and allow us to get
        // rid of the generics warning suppression but in this case there are
        // times when it's ok to cast the result.  For whatever reason, if
        // we return an Integer the calling code is fine with it even if the
        // type is float.  Probably there is some additional coercion to be
        // done here if we wanted to be properly type safe but things are seemingly
        // working ok at the moment and that's a pretty deep time-sink.
        return (T)result;
    }

    @Override
    public String toString() {
        return "Attributes[" + values + "]";
    }
}
