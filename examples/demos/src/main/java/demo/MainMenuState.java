/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
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

package demo;

import java.util.*;

import org.slf4j.*;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;
import com.jme3.scene.*;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.ElementId;

/**
 *
 *
 *  @author    Paul Speed
 */
public class MainMenuState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(MainMenuState.class);

    public static Class[] DEMOS = {
        OptionPanelDemoState.class,
        PopupPanelDemoState.class,
        DragAndDropDemoState.class,
        WordWrapDemoState.class,
        TextEntryDemoState.class,
        FormattedTextEntryDemoState.class,
        ListBoxDemoState.class
    };

    private Container mainWindow;
    private List<ToggleChild> toggles = new ArrayList<>();
    
    public MainMenuState() {
    }
 
    /**
     *  For states that close themselves, this lets the master list know that the
     *  particular child demo is no longer open.  Basically, this lets the checkbox
     *  update.
     */   
    public void closeChild( AppState child ) {
        for( ToggleChild toggle : toggles ) {
            if( toggle.child == child ) {
                toggle.close();
            }
        }   
    }    
    
    public float getStandardScale() {
        int height = getApplication().getCamera().getHeight();        
        return height / 720f;
    }
    
    protected void showError( String title, String error ) {
        getState(OptionPanelState.class).show(title, error);    
    }
     
    @Override   
    protected void initialize( Application app ) {
        mainWindow = new Container();
 
        Label title = mainWindow.addChild(new Label("Lemur Demos"));
        title.setFontSize(32);
        title.setInsets(new Insets3f(10, 10, 0, 10));
        
        Container actions = mainWindow.addChild(new Container());
        actions.setInsets(new Insets3f(10, 10, 0, 10));
        
        for( Class demo : DEMOS ) {
            ToggleChild toggle = new ToggleChild(demo);
            toggles.add(toggle);
            Checkbox cb = actions.addChild(new Checkbox(toggle.getName()));
            cb.addClickCommands(toggle);
            cb.setInsets(new Insets3f(2, 2, 2, 2));
        }
               
 
        ActionButton exit = mainWindow.addChild(new ActionButton(new CallMethodAction("Exit Demo", app, "stop")));
        exit.setInsets(new Insets3f(10, 10, 10, 10)); 
           
        // Calculate a standard scale and position from the app's camera
        // height
        int height = app.getCamera().getHeight();        
        Vector3f pref = mainWindow.getPreferredSize().clone();
        
        float standardScale = getStandardScale();
        pref.multLocal(standardScale);
 
        // With a slight bias toward the top        
        float y = height * 0.9f;
                                     
        mainWindow.setLocalTranslation(100 * standardScale, y, 0);
        mainWindow.setLocalScale(standardScale);
    }
 
    @Override   
    protected void cleanup( Application app ) {
    }
    
    @Override   
    protected void onEnable() {
        Node gui = ((DemoLauncher)getApplication()).getGuiNode();
        gui.attachChild(mainWindow);
        GuiGlobals.getInstance().requestFocus(mainWindow);
    }
    
    @Override   
    protected void onDisable() {
        mainWindow.removeFromParent();
    }
 
    private static String classToName( Class type ) {
        String n = type.getSimpleName();
        if( n.endsWith("DemoState") ) {
            n = n.substring(0, n.length() - "DemoState".length());
        } else if( n.endsWith("State") ) {
            n = n.substring(0, n.length() - "State".length());
        }
         
        boolean lastLower = false;
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < n.length(); i++ ) {
            char c = n.charAt(i);
            if( lastLower && Character.isUpperCase(c) ) {
                sb.append(" ");
            } else if( Character.isLowerCase(c) ) {
                lastLower = true;
            }
            sb.append(c); 
        }
        return sb.toString();
    }
    
    private class ToggleChild implements Command<Button> {
        private String name;
        private Checkbox check;
        private Class type; 
        private AppState child;
    
        public ToggleChild( Class type ) {
            this.type = type;
            this.name = classToName(type);
        }
        
        public String getName() {
            return name;
        }
        
        public void execute( Button button ) {
            this.check = (Checkbox)button;
            System.out.println("Click:" + check);
            if( check.isChecked() ) {
                open();
            } else {
                close();
            }
        }
 
        public void open() {
            if( child != null ) {
                // Already open
                return;
            }
            try {
                child = (AppState)type.newInstance();
                getStateManager().attach(child);
            } catch( Exception e ) {
                showError("Error for demo:" + type.getSimpleName(), e.toString());
            }
        }
        
        public void close() {
            if( check != null ) {
                check.setChecked(false);
            }
            if( child != null ) {
                getStateManager().detach(child);
                child = null;
            }
        }
    }
}
