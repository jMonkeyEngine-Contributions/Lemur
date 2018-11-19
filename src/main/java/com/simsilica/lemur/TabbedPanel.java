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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jme3.math.ColorRGBA;

import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleAttribute;
import com.simsilica.lemur.style.Styles;


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
    
    private VersionedHolder<Tab> selectionModel = new VersionedHolder<>();
    private VersionedReference<Tab> selectionRef = selectionModel.createReference();  
    private Tab displayedTab;
    
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
        if( selectionModel.getObject() == null ) {
            setSelectedTab(tab);
        }
        return contents;
    }

    /**
     *  Inserts the specified contents as a new tab using the specified
     *  title inserted at the specified index.
     */
    public <T extends Panel> T insertTab( int index, String title, T contents ) {
        Tab tab = new Tab(title, contents);
        tabs.add(index, tab);
        refreshTabs();
        if( selectionModel.getObject() == null ) {
            setSelectedTab(tab);
        }
        return contents;
    }
 
    /**
     *  Removes the specified tab from this tabbed panel.  Returns
     *  the tab that was removed or null if the tab is not a member
     *  of this tabbed panel.
     *  Note: if the specified tab is the currently selected tab
     *  then the selection will be reset to the next available tab.
     */
    public Tab removeTab( Tab tab ) {    
        int index = tabs.indexOf(tab);
        if( index < 0 ) {
            return null;
        }
        if( selectionModel.getObject() == tab ) {
            // Clear the current selection
            selectionModel.setObject(null);
        }
        
        tabs.remove(tab);
        refreshTabs();
 
        // See if we need to set the selection again
        if( selectionModel.getObject() == null && !tabs.isEmpty() ) {
            // Do the best we can
            setSelectedTab(tabs.get(Math.min(index, tabs.size()-1)));
        }
        
        return tab;
    }
 
    /**
     *  Returns a read-only list of the Tabs contained in this tabbed panel.
     */
    public List<Tab> getTabs() {
        return Collections.unmodifiableList(tabs);
    }
 
    /** 
     *  Returns a versioned view of the currently selected tab.
     *  Callers can create VersionedReferences to watch for changes.
     */
    public VersionedObject<Tab> getSelectionModel() {
        return selectionModel;
    }
 
    /*
    No reconfigurable selection model... at least for now.  There is no
    real 'model' class and VersionedObject has no setters (for good reason).
    public void setSelectionModel( VersionedHolder<Tab> selectionModel ) {
        this.selectionModel = selectionModel;
        setSelectedTab(displayedTab);
    }*/
    
 
    /**
     *  Sets the text color that will be used for activated tabs. 
     */
    @StyleAttribute(value="activationColor", lookupDefault=false)
    public void setActivationColor( ColorRGBA color ) {
        this.activationColor = color;
        if( displayedTab != null ) {
            displayedTab.title.setColor(color);
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
 
    /**
     *  Sets the currently selected tab to the tab specified.
     */
    public void setSelectedTab( Tab tab ) {
        // This should take care of ignoring nulls also
        if( !tabs.contains(tab) ) {
            return;
        }
        
        selectionModel.updateObject(tab);
        
        // Short-circuit just to make sure we change right away... no 
        // reason to wait until the next frame in this case.
        setDisplayedTab(selectionModel.getObject());
    }
 
    /**
     *  Returns the currently selected tab.
     */   
    public Tab getSelectedTab() {
        return selectionModel.getObject();
    }
    
    protected void setDisplayedTab( Tab tab ) {
        if( displayedTab == tab ) {
            // Already displaying it
            return;
        }
        if( displayedTab != null ) {
            displayedTab.removeContents();
        }
        displayedTab = tab;
        if( displayedTab != null ) {
            displayedTab.addContents();
        }
        for( Tab t : tabs ) {
            t.title.setChecked(t == tab);
        }        
    }
 
    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);

        if( selectionRef != null && selectionRef.update() ) {
            setDisplayedTab(selectionRef.get());
        }
    }
 
    /**
     *  Represents a Tab in the TabbedPanel.
     */   
    public class Tab {
        private Checkbox title;
        private Panel contents;
        private ColorRGBA originalColor;

        @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics        
        public Tab( String title, Panel contents ) {
            this.title = new Checkbox(title, getElementId().child("tab.button"), getStyle());
            this.title.addClickCommands(new SwitchToTab(this));
            this.contents = contents;
        }
        
        public String getTitle() {
            return title.getText();
        }
        
        public Checkbox getTitleButton() {
            return title;
        }
        
        public Panel getContents() {
            return contents;
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
            setSelectedTab(tab);
        }
    }
}
