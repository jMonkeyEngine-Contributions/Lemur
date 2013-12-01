
import com.jme3.math.*;


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
}


Vector2f vec3( Number x, Number y ) {
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
}


