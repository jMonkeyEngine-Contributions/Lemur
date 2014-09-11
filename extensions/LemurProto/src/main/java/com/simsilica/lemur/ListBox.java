/*
 * $Id$
 * 
 * Copyright (c) 2014, Simsilica, LLC
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
 
package com.simsilica.lemur;

import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.component.AbstractGuiComponent;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DefaultCursorListener;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.list.CellRenderer;
import com.simsilica.lemur.list.DefaultCellRenderer;
import com.simsilica.lemur.list.SelectionModel;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Styles;
import java.util.Set;


/**
 *
 *  @author    Paul Speed
 */
public class ListBox<T> extends Panel {
    
    public static final String ELEMENT_ID = "list";
    public static final String CONTAINER_ID = "container";
    public static final String ITEMS_ID = "items";
    public static final String SLIDER_ID = "slider";
    public static final String SELECTOR_ID = "selector";

    private BorderLayout layout;
    //private ListModel<T> model;
    private VersionedList<T> model;
    private CellRenderer<T> cellRenderer;
    
    private SelectionModel selection;
    private VersionedReference<Set<Integer>> selectionRef;
    
    private ClickListener clickListener = new ClickListener();
    
    private GridPanel grid;
    private Slider slider;
    private Node selectorArea;
    private Panel selector;
    private Vector3f selectorAreaOrigin = new Vector3f();
    private Vector3f selectorAreaSize = new Vector3f();  
    private RangedValueModel baseIndex;  // upside down actually
    private VersionedReference<Double> indexRef;
    private int maxIndex;
    
    public ListBox() {
        this(true, new VersionedList<T>(), null,
             new SelectionModel(),
             new ElementId(ELEMENT_ID), null);             
    }

    public ListBox( VersionedList<T> model ) {
        this(true, model, null, 
                new SelectionModel(), new ElementId(ELEMENT_ID), null);             
    }

    public ListBox( VersionedList<T> model, CellRenderer<T> renderer, String style ) {
        this(true, model, renderer, new SelectionModel(), new ElementId(ELEMENT_ID), style);             
    }

    public ListBox( VersionedList<T> model, String style ) {
        this(true, model, null, 
                new SelectionModel(), new ElementId(ELEMENT_ID), style);             
    }
 
    public ListBox( VersionedList<T> model, ElementId elementId, String style ) {
        this(true, model, null, 
                new SelectionModel(), elementId, null);             
    }

    public ListBox( VersionedList<T> model, CellRenderer<T> renderer, ElementId elementId, String style ) {
        this(true, model, renderer, new SelectionModel(), elementId, null);             
    }
    
    protected ListBox( boolean applyStyles, VersionedList<T> model, CellRenderer<T> cellRenderer, 
                       SelectionModel selection,  
                       ElementId elementId, String style ) {
        super(false, elementId.child(CONTAINER_ID), style);
        
        if( cellRenderer == null ) {
            // Create a default one
            cellRenderer = new DefaultCellRenderer(elementId.child("item"), style);
        }
        this.cellRenderer = cellRenderer;
 
        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);
 
        grid = new GridPanel(new GridModelDelegate(), elementId.child(ITEMS_ID), style);
        //CursorEventControl.addListenersToSpatial(grid, new GridListener());
        grid.setVisibleColumns(1);
        layout.addChild(grid, BorderLayout.Position.Center);
 
        // Add a special component to the grid so that we get resize
        // events.  Kind of a hack but I'm not sure I want to create
        // a whole event framework just for this.  
        int sizerIndex = grid.getControl(GuiControl.class).getComponentIndex(KEY_INSETS) + 1; 
        grid.getControl(GuiControl.class).addComponent( sizerIndex, "sizer", 
                    new AbstractGuiComponent() {                       
                        @Override
                        public void calculatePreferredSize(Vector3f size) {
                        }

                        @Override
                        public void reshape(Vector3f pos, Vector3f size) {
                            gridResized(pos, size);
                        }
                    });
 
        baseIndex = new DefaultRangedValueModel();
        indexRef = baseIndex.createReference();
        slider = new Slider(baseIndex, Axis.Y, elementId.child(SLIDER_ID), style);
        layout.addChild(slider, BorderLayout.Position.East);
 
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId.getId(), style);
        }

        // Need a spacer so that the 'selector' panel doesn't think
        // it's being managed by this panel.
        // Have to set this up after applying styles so that the default
        // styles are properly initialized the first time.
        selectorArea = new Node("selectorArea");
        attachChild(selectorArea);
        selector = new Panel(elementId.child(SELECTOR_ID), style);
        
        setModel(model);                
        resetModelRange();
        setSelectionModel(selection);        
    }
    
    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Styles styles, Attributes attrs ) {
 
        ElementId parent = new ElementId(ELEMENT_ID);
        //QuadBackgroundComponent quad = new QuadBackgroundComponent(new ColorRGBA(0.5f, 0.5f, 0.5f, 1));
        QuadBackgroundComponent quad = new QuadBackgroundComponent(new ColorRGBA(0.8f, 0.9f, 0.1f, 1));
        quad.getMaterial().getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Exclusion);
        styles.getSelector(parent.child(SELECTOR_ID), null).set("background", quad, false);        
    }
    
    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);
        
        if( indexRef.update() ) {
            int index = (int)(maxIndex - baseIndex.getValue());
            grid.setRow(index);
            refreshSelector();
        }
        
        if( selectionRef.update() ) {
            refreshSelector();
        }
    }

    protected void gridResized( Vector3f pos, Vector3f size ) {
        if( pos.equals(selectorAreaOrigin) && size.equals(selectorAreaSize) ) {
            return;
        }
        
        selectorAreaOrigin.set(pos);
        selectorAreaSize.set(size);
        
        refreshSelector();        
    }
    
    public void setModel( VersionedList<T> model ) {
        if( this.model == model ) {
            return;
        }
        
        if( this.model != null ) {
            // Clean up the old one
        }

        if( model == null ) {
            // Easier to create a default one than to handle a null model
            // everywhere
            model = new VersionedList<T>();
        }  
        
        this.model = model;
        
        grid.setLocation(0,0);
        grid.setModel(new GridModelDelegate());  // need a new one for a new version
        resetModelRange();
        baseIndex.setValue(maxIndex);
        refreshSelector();    
    }        

    public VersionedList<T> getModel() {
        return model;
    }
 
    public void setSelectionModel( SelectionModel selection ) {
        if( this.selection == selection ) {
            return;
        }
        this.selection = selection;
        this.selectionRef = selection.createReference();
        refreshSelector();
    }
    
    public SelectionModel getSelectionModel() {
        return selection;
    }
    
    public void setVisibleItems( int count ) {
        grid.setVisibleRows(count);
        resetModelRange();
        refreshSelector();
    }
    
    public int getVisibleItems() {
        return grid.getVisibleRows();
    }

    protected void refreshSelector() {    
        
        Panel selectedCell = null;
        if( selection != null && !selection.isEmpty() ) {
            // For now just one item... otherwise we have to loop
            // over visible items
            int selected = selection.iterator().next();
            selectedCell = grid.getCell(selected, 0); 
        }
                
        if( selectedCell == null ) {
            selectorArea.detachChild(selector);            
        } else {
            Vector3f size = selectedCell.getSize().clone();
            Vector3f loc = selectedCell.getLocalTranslation();
            Vector3f pos = selectorAreaOrigin.add(loc.x, loc.y, loc.z + size.z);
            selector.setLocalTranslation(pos);
            selector.setSize(size);
            
            selectorArea.attachChild(selector);
            selectorArea.setLocalTranslation(grid.getLocalTranslation());            
        }
    }

    protected void resetModelRange() {
        int count = model.size();
        int visible = grid.getVisibleRows();
        maxIndex = Math.max(0, count - visible);
        baseIndex.setMinimum(0);
        baseIndex.setMaximum(maxIndex);        
    }

    protected Panel getListCell( int row, int col, Panel existing ) {
        T value = model.get(row);
        Panel cell = cellRenderer.getView(value, false, existing);
 
        if( cell != existing ) {
            // Transfer the click listener                  
            CursorEventControl.addListenersToSpatial(cell, clickListener);            
            CursorEventControl.removeListenersFromSpatial(existing, clickListener);
        }         
        return cell;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[elementId=" + getElementId() + "]";
    }
    
    private class ClickListener extends DefaultCursorListener {
    
        @Override
        public void cursorButtonEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
 
System.out.println( "List button event:" + event + "  target:" + target + "  capture:" + capture );        
            // Find the element we clicked on
            int base = grid.getRow();
            for( int i = 0; i < grid.getVisibleRows(); i++ ) {
                Panel cell = grid.getCell( base + i, 0 );
                if( cell == target ) {
System.out.println( "Clicked on:" + cell );
                    selection.add(base + i);                    
                }
            }            
        }
    
    }

    protected class GridModelDelegate implements GridModel<Panel> {
        
        @Override
        public int getRowCount() {
            if( model == null ) {
                return 0;
            }
            return model.size();        
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Panel getCell( int row, int col, Panel existing ) {
            return getListCell(row, col, existing);
        }
                
        @Override
        public void setCell( int row, int col, Panel value ) {
            throw new UnsupportedOperationException("ListModel is read only.");
        }

        @Override
        public long getVersion() {
            return model == null ? 0 : model.getVersion();
        }

        @Override
        public GridModel<Panel> getObject() { 
            return this;
        }

        @Override
        public VersionedReference<GridModel<Panel>> createReference() { 
            return new VersionedReference<GridModel<Panel>>(this);
        }
    }
}
