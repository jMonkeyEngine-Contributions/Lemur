/*
 * $Id$
 *
 * Copyright (c) 2013-2013 jMonkeyEngine
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

import com.jme3.math.*;

scripts.addDefaultImports( "com.jme3.math.*" );

Vector3f vec3( Number x, Number y, Number z ) {
    return new Vector3f(x.floatValue(), y.floatValue(), z.floatValue());
}

Vector3f vec3( Number x ) {
    return vec3(x, x, x);
}

Vector3f vec3( Vector2f v, Number z ) {
    return vec3(v.x, v.y, z);
}

Vector3f.metaClass {
    plus { Vector3f v ->
        return delegate.add(v);
    }
    
    minus { Vector3f v ->
        return delegate.subtract(v);
    }
    
    multiply { Number scale ->
        return delegate.mult(scale.floatValue());
    }
    
    div { Number scale ->
        return delegate.divide(scale.floatValue());
    }
    
    getAt { int i ->
        return delegate.get(i);
    }
    
    putAt { int i, Number val ->
        delegate.set(i, val.floatValue());
    }
    
    negative {
        return delegate.negate();
    }

    // I can't get dynamic swizzling with propertyMissing to work
    // like I want because I can't figure out how to delegate to
    // the original behavior if the swizzle misses.  So I will hard-code
    // a few.
    getXy {
        return new Vector2f(delegate.x, delegate.y);
    }
    
    getXz {
        return new Vector2f(delegate.x, delegate.z);
    }
    
    getYz {
        return new Vector2f(delegate.y, delegate.z);
    }
    
    asType { Class type ->
        if( type == ColorRGBA ) {
            return new ColorRGBA(delegate.x, delegate.y, delegate.z, 1);
        } else {
            throw new org.codehaus.groovy.runtime.typehandling.GroovyCastException(delegate, type);
        }
    }    
}


Vector2f vec2( Number x, Number y ) {
    return new Vector2f(x.floatValue(), y.floatValue());
}

Vector2f vec2( Number x ) {
    return vec2(x, x);
}

Vector2f.metaClass {
    plus { Vector2f v ->
        return delegate.add(v);
    }
    
    minus { Vector2f v ->
        return delegate.subtract(v);
    }
    
    multiply { Number scale ->
        return delegate.mult(scale.floatValue());
    }
    
    div { Number scale ->
        return delegate.divide(scale.floatValue());
    }
    
    getAt { int i ->
        return delegate.get(i);
    }
    
    putAt { int i, Number val ->
        delegate.set(i, val.floatValue());
    }
    
    negative {
        return delegate.negate();
    }
}



Vector4f vec4( Number x, Number y, Number z, Number w ) {
    return new Vector4f(x.floatValue(), y.floatValue(), z.floatValue(), w.floatValue());
}

Vector4f vec4( Number x ) {
    return vec3(x, x, x);
}

Vector4f vec4( Vector3f v, Number w ) {
    return vec4(v.x, v.y, v.z, w);
}

Vector4f.metaClass {
    plus { Vector4f v ->
        return delegate.add(v);
    }
    
    minus { Vector4f v ->
        return delegate.subtract(v);
    }
    
    multiply { Number scale ->
        return delegate.mult(scale.floatValue());
    }
    
    div { Number scale ->
        return delegate.divide(scale.floatValue());
    }
    
    getAt { int i ->
        return delegate.get(i);
    }
    
    putAt { int i, Number val ->
        delegate.set(i, val.floatValue());
    }
    
    negative {
        return delegate.negate();
    }

    // I can't get dynamic swizzling with propertyMissing to work
    // like I want because I can't figure out how to delegate to
    // the original behavior if the swizzle misses.  So I will hard-code
    // a few.
    getXy {
        return new Vector2f(delegate.x, delegate.y);
    }
    
    getXz {
        return new Vector2f(delegate.x, delegate.z);
    }
    
    getYz {
        return new Vector2f(delegate.y, delegate.z);
    }
 
    // vec3 versions       
    getXyz {
        return new Vector3f(delegate.x, delegate.y, delegate.z);
    }
        
    asType( ColorRGBA ) {
        return new ColorRGBA(delegate.x, delegate.y, delegate.z, delegate.w);
    }    
}

ColorRGBA color( Number r, Number g, Number b, Number a ) {
    return new ColorRGBA(r.floatValue(), g.floatValue(), b.floatValue(), a.floatValue());
}

ColorRGBA color( Number r, Number g, Number b ) {
    return color(r, g, b, 1);
}  

ColorRGBA color( Vector3f v ) {
    return color(v.x, v.y, v.z, 1);
}

ColorRGBA color( Vector4f v ) {
    return color(v.x, v.y, v.z, v.w);
}

ColorRGBA.metaClass {

    plus { ColorRGBA v ->
        return delegate.add(v);
    }
    
    minus { ColorRGBA v ->
        return delegate.subtract(v);
    }
    
    multiply { Number scale ->
        return delegate.mult(scale.floatValue());
    }
    
    div { Number scale ->
        return delegate.divide(scale.floatValue());
    }
    
    getAt { int i ->
        switch( i ) {
            case 0:
                return delegate.r;
            case 1:
                return delegate.g;
            case 2:
                return delegate.b;
            case 3:
                return delegate.a;
            default:
                throw new IndexOutOfBoundsException(i);
        }
    }
    
    putAt { int i, Number val ->
        switch( i ) {
            case 0:
                delegate.r = val.floatValue();
                break;
            case 1:
                delegate.g = val.floatValue();
                break;
            case 2:
                delegate.b = val.floatValue();
                break;
            case 3:
                delegate.a = val.floatValue();
                break;
            default:
                throw new IndexOutOfBoundsException(i);
        }
        return val;
    }
    
    // I can't get dynamic swizzling with propertyMissing to work
    // like I want because I can't figure out how to delegate to
    // the original behavior if the swizzle misses.  So I will hard-code
    // a few.
    getRgb {
        return new Vector3f(delegate.r, delegate.g, delegate.b);
    }    

    asType( Vector4f ) {
        return new Vector4f(delegate.r, delegate.g, delegate.b, delegate.a);
    }
}
