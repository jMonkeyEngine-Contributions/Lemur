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


// Some general helper functions.

void help() {
    println "Default imports:";
    println "    " + scripts.defaultImports.join("\n    ");
    println "Default bindings:";
    println "    " + scripts.initBindings.collect{ it }.join("\n    ");
    println "";
    println "Type: help ClassName";
    println "   to get information about a class.";
    println "Type: help someObject";
    println "   to get information about an object's properties.";
}

Class help( Class type ) {
    println "Info for:" + type;
    println "    super class:" + type.superclass;
    println "    properties:";     
    println "        " + type.metaClass.properties.findAll{it.getter.declaringClass.name == type.name}.collect{it.name}.join("\n        ");
    return type     
}

Object help( Object o, boolean all ) {
    println "Info for:" + o ;
    if( o == null ) {
        return;
    }
    def type = o.class;
    def local = type.metaClass.properties.findAll{it.getter.declaringClass.name == type.name}.collect{it.name}; 
    println "    class:" + o.class;
    println "    properties:" + (all ? " (including supertype properties)":"");
    println "        " + o.properties.findAll{ all || local.contains(it.key) }.collect{it}.join("\n        ");  
}

Object help( Object o ) {
    help( o, false );
}

Object getState( Class type ) {
    return app.stateManager.getState(type);
}
