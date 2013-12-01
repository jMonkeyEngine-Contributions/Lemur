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


// Adding some additional features to Material


import com.jme3.material.*;
import com.jme3.shader.VarType;

/**
 *  Wraps a JME MatParam and makes it look like a Map.Entry.
 */
class ParameterWrapper implements Map.Entry {
    MatParam param;
    Material material;
 
    public ParameterWrapper( Material material, MatParam param ) {
        if( material == null ) {
            throw new IllegalArgumentException( "Material cannot be null." );
        }
        this.param = param;
        this.material = material;
    }
 
    public String getKey() {
        return param.name;
    }
 
    public VarType getVarType() {
        return param.varType;       
    }
       
    public Object getValue() {
        def mp = material.getParam(param.name);
        return mp?.value
    }
    
    public Object setValue( Object val ) {
        
        println( "Need to set" + param.name + " to:" + val );
        def result = param.value;       
        if( val == null ) {
            material.clearParam(param.name);
        } else {
            println( "calling setParam(" + param.name + ", " + param.varType + ", " + val + ") on :" + material );
            material.setParam(param.name, param.varType, val);
        }
        return result;
    }
    
    public String toString() {
        return param.name + "=" + getValue();
    }
}

/**
 *  Expose the complete material parameters (including unset ones)
 *  as a map.
 */
class ParamsWrapper extends AbstractMap {
    Material material;
    
    public ParamsWrapper( Material material ) {
        this.material = material;
    }
     
    public Set<String> getNames() {
        return material.materialDef.materialParams.collect{ it.name };
    }
 
    public Object getProperties() {
        def mat = material // weird... but whatever.
        return material.materialDef.materialParams.collect { new ParameterWrapper(mat, it) }; 
    }
 
    public Set entrySet() {
        def set = new HashSet(getProperties());
        return set;
    }
 
    public Object put( String key, Object value ) {
        def mp = material.materialDef.getMaterialParam(key);
        if( mp == null ) {
            throw new IllegalArgumentException("Material parameter:" + key + " not available on:" + material.matDef.assetName);
        }
        return new ParameterWrapper(material, mp).setValue(value);
    }
 
    public void setProperty( String key, Object value ) {
        def mp = material.materialDef.getMaterialParam(key);
        if( mp != null ) {
            new ParameterWrapper(material, mp).setValue(value);
        } else {
            throw new groovy.lang.MissingPropertyException(key, getClass());
        } 
    }
    
    public Object getProperty( String key ) {
        if( key == "names" ) {
            return getNames();
        } else if( key == "properties" ) {
            return getProperties();
        }
        def mp = material.getParam(key);
        return mp?.value
    } 
}

Material.metaClass {
    
    getParams {
        return new ParamsWrapper(delegate);
    }
}

