/**
 *  ${Id}
 *
 *  Implements the custom style loading API
 *  used by the style loader.
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

import com.jme3.font.*;
import com.jme3.math.*;
import com.jme3.texture.*;
import com.simsilica.lemur.style.*;

// Add some custom behavior to the Attributes class
Attributes.metaClass {
    configure { Closure c ->
 
        if( c != null ) {
            // If we don't set things up this way then
            // we end up with statements like:
            //         foo=3
            // Setting a higher up property instead of the
            // current object we are configuring.
            c.setResolveStrategy(Closure.DELEGATE_FIRST);
            c.setDelegate(delegate);
            c();
        }
        return delegate;        
    }    
}

Attributes selector( String style, Closure c ) {
    def attrs = styles.getSelector(style);
    attrs.configure(c)
    return attrs;   
}

Attributes selector( String id, String style, Closure c ) {
    def attrs = styles.getSelector(id, style);
    attrs.configure(c)
    return attrs;   
}

Attributes selector( String parent, String child, String style, Closure c ) {
    def attrs = styles.getSelector(parent, child, style);
    attrs.configure(c)
    return attrs;   
}

BitmapFont font( String name ) {
    return gui.loadFont(name)
}

ColorRGBA color( Number r, Number g, Number b, Number a ) {
    return new ColorRGBA( r.floatValue(), g.floatValue(), b.floatValue(), a.floatValue() )
}

Texture texture( String name ) {
    return gui.loadTexture( name, true, true );
}

Texture texture( Map args ) {
    String name = args.name;
    if( name == null ) {
        throw new IllegalArgumentException( "Texture name not specified." ); 
    }
    
    boolean generateMips = args.generateMips != Boolean.FALSE
    Texture t = gui.loadTexture(name, true, generateMips)
    for( Map.Entry e : args ) {
        if( e.key == "name" || e.key == "generateMips" )
            continue;
        t[e.key] = e.value 
    }
    
    return t;
}

Vector3f vec3( Number x, Number y, Number z ) {
    return new Vector3f(x.floatValue(), y.floatValue(), z.floatValue());
}

Vector3f vec2( Number x, Number y ) {
    return new Vector2f(x.floatValue(), y.floatValue());
}



