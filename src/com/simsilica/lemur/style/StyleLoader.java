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

import java.io.*;
import java.util.*;
import javax.script.*;
import com.simsilica.lemur.GuiGlobals;


/**
 *
 *  @author    Paul Speed
 */
public class StyleLoader 
{
    private List<CompiledScript> api = new ArrayList<CompiledScript>();
    private Map<CompiledScript, Object> sources = new HashMap<CompiledScript, Object>();
    private ScriptEngine engine;
    private Compilable compiler;
    private Bindings bindings;
    private Styles styles;
    private boolean initialized = false; 

    public StyleLoader()
    {
        this( GuiGlobals.getInstance(), GuiGlobals.getInstance().getStyles(), 
              "/com/simsilica/lemur/style/StyleApi.groovy" );
    }

    public StyleLoader( Styles styles )
    {
        this( GuiGlobals.getInstance(), styles, "/com/simsilica/lemur/style/StyleApi.groovy" );
    }

    public StyleLoader( GuiGlobals globals, Styles styles, Object... apiScripts )
    {
        this.styles = styles;
        ScriptEngineManager factory = new ScriptEngineManager();        
        this.engine = factory.getEngineByName("groovy");
        this.compiler = (Compilable)engine;
        this.bindings = engine.createBindings();
        bindings.put( "styles", styles );
        bindings.put( "gui", globals );
 
        compileApi( apiScripts );
    }
    
    protected void compileApi( Object... apiScripts )
    {
        for( Object o : apiScripts )
            {
            try
                {
                if( o instanceof String )
                    compileApiResource( (String)o );
                else if( o instanceof File )
                    compileApiFile( (File)o );
                }
            catch( ScriptException e )
                {
                throw new RuntimeException( "Error compiling script:" + o, e );
                }
            }
    }

    protected void addApiScript( CompiledScript script, String source )
    {
        api.add(script);
        sources.put( script, source );
    } 
     
    protected void compileApiResource( String s ) throws ScriptException
    {
        InputStream rawIn = getClass().getResourceAsStream(s);
        if( rawIn == null )
            throw new ScriptException( "Script resource not found for:" + s ); 
        Reader in = new InputStreamReader( rawIn );
        addApiScript( compiler.compile(in), "resource:" + s );       
    }
    
    protected void compileApiFile( File f ) throws ScriptException
    {
        try
            {            
            Reader in = new FileReader( f );
            addApiScript( compiler.compile(in), "file:" + f );
            }
        catch( IOException e )
            {
            throw new ScriptException(e);
            }       
    }    
        
    public void setBinding( String name, Object value )
    {
        bindings.put( name, value );
    }
    
    public Object getBinding( String name )
    {
        return bindings.get(name);
    }
 
    public void initializeApi()
    {        
        for( CompiledScript script : api )
            {
            try
                {
                script.eval(bindings);
                }
            catch( ScriptException e )
                {
                throw new RuntimeException( "Error running:" + script + " from:" + sources.get(script), e );
                }
            }
        initialized = true;
    }
    
    public void loadStyleResource( String s )
    {
        if( !initialized )
            initializeApi();
            
        try
            {
            Reader in = new InputStreamReader( getClass().getResourceAsStream(s) );
            CompiledScript script = compiler.compile(in);

            int before = bindings.size();
            Object result = script.eval(bindings);
            
            if( before != bindings.size() )
                {
                //log.warn( "Binding count increased executing:" + s + "  keys:" + bindings.keySet() );
                }
            }
        catch( ScriptException e )
            {
            throw new RuntimeException( "Error running resource:" + s, e );
            }        
    }
}


