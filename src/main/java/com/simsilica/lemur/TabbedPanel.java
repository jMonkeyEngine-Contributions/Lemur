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

package com.simsilica.lemur;


import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleAttribute;
import com.simsilica.lemur.style.Styles;
import java.util.ArrayList;
import java.util.List;


/**
 *  A very simple tabbed panel element that presents a set
 *  of button "tabs" at the top that can select different child
 *  content.
 *
 *  @author    Paul Speed
 */ 
public class TabbedPanel extends Panel {
 
    public static final ElementId ELEMENT_ID = new ElementId("tabbedPanel");
    
    private BorderLayout layout;
    private Container tabButtons;
    private Container container;
    private List<Tab> tabs = new ArrayList<Tab>();
    private Tab selected;
    
    private ColorRGBA activationColor = ColorRGBA.Cyan;    
    
    public TabbedPanel() {
        this(true, ELEMENT_ID, null);
    }

    public TabbedPanel( String style ) {
        this(true, ELEMENT_ID, style);
    }
    
    public TabbedPanel( ElementId elementId, String style ) {
        this(true, elementId, style);
    }  
 
    protected TabbedPanel( boolean applyStyles, ElementId elementId, String style )
    {
        super(false, elementId, style);

        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);
 
        this.tabButtons = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.None, FillMode.Even),
                                        elementId.child("tabButtons"), style);
        layout.addChild(tabButtons, BorderLayout.Position.North);
 
        this.container = new Container(new BorderLayout(), elementId.child("container"), style);
        layout.addChild(container, BorderLayout.Position.Center);
 
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
        }
    }
 
    /**
     *  Adds the specified contents as a new tab using the specified
     *  title.
     */
    public <T extends Panel> T addTab( String title, T contents ) {
        Tab tab = new Tab(title, contents);
        tabs.add(tab);
        refreshTabs();
        if( selected == null ) {
            setSelected(tab);
        }
        return contents;
    }
 
    // TODO: implement tab retrieval and tab removal
 
    /**
     *  Sets the text color that will be used for activated tabs. 
     */
    @StyleAttribute(value="activationColor", lookupDefault=false)
    public void setActivationColor( ColorRGBA color ) {
        this.activationColor = color;
        if( selected != null ) {
            selected.title.setColor(color);
        }                
    }
 
    /**
     *  Returns the text color used for activated tabs.
     */   
    public ColorRGBA getActivationColor() {
        return activationColor;
    }
        
    protected void refreshTabs() {
        // Clean out any existing buttons
        tabButtons.getLayout().clearChildren();
        
        for( Tab tab : tabs ) {
            tabButtons.addChild(tab.title);
        }       
    }
 
    protected void setSelected( Tab tab ) {
        if( this.selected == tab) {
            return;
        }
        if( this.selected != null ) {
            selected.removeContents();
        }
        this.selected = tab;
        if( this.selected != null ) {
            selected.addContents();
        }
        for( Tab t : tabs ) {
            t.title.setChecked(t == tab);
        }
    }
    
    public class Tab {
        private Checkbox title;
        private Panel contents;
        private ColorRGBA originalColor;
        
        public Tab( String title, Panel contents ) {
            this.title = new Checkbox(title, getElementId().child("tab.button"), getStyle());
            this.title.addClickCommands(new SwitchToTab(this));
            this.contents = contents;
        }
        
        protected void removeContents() {
            if( contents == null ) {
                return;
            }
            if( contents.getParent() != null ) {
                container.removeChild(contents);
                title.setColor(originalColor);
            }
        }
        
        protected void addContents() {
            if( contents == null ) {
                return;
            }
            if( contents.getParent() == null ) {
                container.addChild(contents, BorderLayout.Position.Center);
                originalColor = title.getColor();
                title.setColor(activationColor);
            }            
        }
    }
    
    protected class SwitchToTab implements Command<Button> {
        private Tab tab;
        
        public SwitchToTab( Tab tab ) {
            this.tab = tab;
        }

        public void execute( Button source ) {
            setSelected(tab);
        }
    }
}
