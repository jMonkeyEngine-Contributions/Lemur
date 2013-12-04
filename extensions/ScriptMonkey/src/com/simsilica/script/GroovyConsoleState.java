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

package com.simsilica.script;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.jme3.app.Application;
import com.simsilica.lemur.event.BaseAppState;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.ui.Console;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author PSpeed
 */
public class GroovyConsoleState extends BaseAppState {
    static Logger log = LoggerFactory.getLogger(GroovyConsoleState.class);
 
    private static final String PREF_LAST_SCRIPT = "lastScript";
    
    private Console console;
    private JFrame frame;
    private Component outputWindow;

    /**
     *  The script engine to which the console will delegate its 
     *  evals.  This is setup upon state initialization and is kept
     *  intact across enable/disable.
     */
    private ScriptEngine engine;
 
    /**
     *  The bindings that will live as long as the console
     *  state itself.  If the script engine is reset these are
     *  the bindings that will be reapplied automatically to the
     *  new engine. 
     */
    private Map<String, Object> initialBindings = new HashMap<String, Object>();
    
    /**
     *  These are the live global bindings for the script engine
     *  session.  It will live across enable/disable until foreably 
     *  reset and these bindings will include anything that the scripts
     *  (API-level or otherwise) have dropped into the environment.
     */
    private Bindings globalBindings = null;
    
    /**
     *  The set of scripts to run on engine startup.  This sets up the
     *  base environment and API that the console will have available.
     */
    private Map<Object, String> initScripts = new LinkedHashMap<Object, String>();

    /**
     *  Default imports that will be added to every script run.
     */
    private List<String> imports = new ArrayList<String>();
    private String importString = null;

    public GroovyConsoleState() {
    }

    public void setInitBinding( String key, Object value ) {
        initialBindings.put(key, value);
    }

    public Map<String, Object> getInitBindings() {
        return initialBindings;
    }

    public void toFront() {
        if( frame != null ) {
            frame.toFront();
        }
    }

    public void toggleEnabled() {
        setEnabled(!isEnabled());
    }

    public void addDefaultImports( String... array ) {
        for( String s : array ) {
            imports.add(s);
        }
        importString = null;
    }

    public List<String> getDefaultImports() {
        return imports;
    }

    protected String getImportString() {
        if( importString == null ) {
            StringBuilder sb = new StringBuilder();
            for( String s : imports ) {
                sb.append( "import " + s + ";\n" );
            }
            importString = sb.toString();
        }
        return importString;
    }

    protected void resetScriptEngine() {
        
        ScriptEngineManager factory = new ScriptEngineManager();        
        this.engine = factory.getEngineByName("groovy");
        globalBindings = engine.createBindings();
        engine.setBindings(globalBindings, ScriptContext.ENGINE_SCOPE); 
 
        // Clear the old imports
        imports.clear();
        importString = null;
 
        // Provide direct access to the bindings as a binding.
        // This can be useful for debugging 'not found' errors
        // inside scripts.
        globalBindings.put( "bindings", globalBindings );
        
        // Put all of the caller provided preset bindings
        globalBindings.putAll(initialBindings);
        
        // Run the API Scripts
        for( Map.Entry<Object, String> e : initScripts.entrySet() ) {
            try {
                String script = e.getValue();
                script = getImportString() + script;
                engine.eval(script); 
            } catch( ScriptException ex ) {
                throw new GroovyRuntimeException("Error executing initialization script:" + e.getKey(), ex);
            }
        }                           
    }

    protected void initialize( Application app ) {
        resetScriptEngine();    
    }    

    @Override
    protected void cleanup( Application app ) {
        globalBindings = null;
        engine = null;
    }

    public void addInitializationScript( File f ) {
        try {
            String script = Files.toString(f, Charset.forName("UTF-8"));
            script = getImportString() + script;
            initScripts.put(f, script);
        } catch( IOException e ) {
            throw new RuntimeException("Error reading:" + f, e);
        }
    }

    public void addInitializationScript( URL resource ) {
        try {
            String script = Resources.toString( resource, Charset.forName("UTF-8"));
            initScripts.put(resource, script);
        } catch( IOException e ) {
            throw new RuntimeException("Error reading:" + resource, e);
        }
    }

    @Override   
    public void update( float tpf ) {
        if( frame != null && !frame.isDisplayable() ) {
            setEnabled(false);
        }
    } 
 
    protected void enable() {
    
        console = new Console();
        console.setShell(new EnhancedShell(console.getShell())); //, scriptList));
        console.run();        
 
        // See if we have any script text from last time
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        final String lastText = prefs.get(PREF_LAST_SCRIPT, null);
 
        if( lastText != null ) { 
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    console.getInputArea().setText(lastText);
                }
            });
        }            
 
        outputWindow = console.getOutputWindow();
        frame = (JFrame)console.getFrame();
        
        GroovyShell shell = console.getShell();
 
        // So now that the console has been "run" we need to set the script
        // engine's stdout to the latest stdout.  This is done through
        // jsr223's ScriptContext.  Many Bothans died to bring us this
        // information.
        ScriptContext context = engine.getContext();
        context.setWriter(new PrintWriter(System.out));
    }
    
    protected void disable() {
        
        // See if we can grab the text
        String text = console.getInputArea().getText();
        if( text.trim().length() > 0 ) {
            log.info("Saving for next time:\n" + text);
            
            // Save it for next time 
            Preferences prefs = Preferences.userNodeForPackage(getClass());
            prefs.put(PREF_LAST_SCRIPT, text);
            try {
                prefs.flush();
            } catch( BackingStoreException e ) {
                log.warn( "Error saving last script to preferences", e );
            }
        }        
    
        if( frame != null && frame.isDisplayable() ) {
            console.exit(null);
        }
    }           

    public class ScriptCallable implements Callable {
        private String scriptText;
        
        public ScriptCallable( String scriptText ) {
            this.scriptText = getImportString() + scriptText;
        }
        
        public Object call() throws ScriptException {
            return engine.eval(scriptText);
        }
    }

    public class EnhancedShell extends GroovyShell {
        
        public EnhancedShell( GroovyShell inherit ) {
            super( inherit.getClassLoader().getParent(), 
                   inherit.getContext(), 
                   new CompilerConfiguration() );
        }
        
        @Override
        public Object run(String scriptText, String fileName, List list) throws CompilationFailedException {
 
            // Evaluate the script on the render thread
            Future future = getApplication().enqueue(new ScriptCallable(scriptText));
            
            try {
                // And we wait for it.
                Object result = future.get();

//System.out.println( "   done-------" );
//System.out.println( "   globalBindings:" + globalBindings ); 
//System.out.println( "   contextVariables:" + getContext().getVariables() ); 
                return result;
            } catch( InterruptedException e ) {
                throw new GroovyRuntimeException("Interrupted executing script", e);
            } catch( ExecutionException e ) {
                throw new GroovyRuntimeException("Error executing script", e.getCause());
            }
        }
    }
}
