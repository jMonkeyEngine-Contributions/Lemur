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

import java.util.*;

import org.slf4j.*;

import com.google.common.base.Objects;

import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.*;
import com.jme3.scene.*;

import com.simsilica.lemur.component.*;
import com.simsilica.lemur.core.*;
import com.simsilica.lemur.event.*;
import com.simsilica.lemur.grid.GridModel;
import com.simsilica.lemur.list.*;
import com.simsilica.lemur.style.*;


/**
 *
 *  @author    Paul Speed
 */
public class ListBox<T> extends Panel {
 
    static Logger log = LoggerFactory.getLogger(ListBox.class);
    
    public static final String ELEMENT_ID = "list";
    public static final String CONTAINER_ID = "container";
    public static final String ITEMS_ID = "items";
    public static final String SLIDER_ID = "slider";
    public static final String SELECTOR_ID = "selector";

    public static final String EFFECT_PRESS = "press";
    public static final String EFFECT_RELEASE = "release";
    public static final String EFFECT_CLICK = "click";
    public static final String EFFECT_ACTIVATE = "activate";
    public static final String EFFECT_DEACTIVATE = "deactivate";
    
    public enum ListAction { Down, Up, Click, Entered, Exited };


    private BorderLayout layout;
    private VersionedList<T> model;
    private VersionedReference<List<T>> modelRef;
    private CellRenderer<T> cellRenderer;
    
    private SelectionModel selection;
    private VersionedReference<Set<Integer>> selectionRef;
    
    private ClickListener clickListener = new ClickListener();
    private BackgroundListener backgroundListener = new BackgroundListener();
    private CommandMap<ListBox, ListAction> commandMap
                                    = new CommandMap<ListBox, ListAction>(this);

    private GridPanel grid;
    private Slider slider;
    private Node selectorArea;
    private Panel selector;
    private Vector3f selectorAreaOrigin = new Vector3f();
    private Vector3f selectorAreaSize = new Vector3f();  
    private RangedValueModel baseIndex;  // upside down actually
    private VersionedReference<Double> indexRef;
    private int maxIndex;
    
    /**
     *  Set to true the mouse wheel will scroll the list if the mouse
     *  is over the list.
     */
    private boolean scrollOnHover = true;
 
    /**
     *  Keeps track of if we've triggered 'activated' effects (and send entered events)
     */
    private boolean activated = false;
    
    /**
     *  Keeps track of whether some listener has detected enter/exit.  When this
     *  is different than activated then we need to trigger effects and fire events.
     */
    private boolean entered = false;
       
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
        this(true, model, null, new SelectionModel(), new ElementId(ELEMENT_ID), style);             
    }
 
    public ListBox( VersionedList<T> model, ElementId elementId, String style ) {
        this(true, model, null, new SelectionModel(), elementId, style);             
    }

    public ListBox( VersionedList<T> model, CellRenderer<T> renderer, ElementId elementId, String style ) {
        this(true, model, renderer, new SelectionModel(), elementId, style);             
    }
    
    protected ListBox( boolean applyStyles, VersionedList<T> model, CellRenderer<T> cellRenderer, 
                       SelectionModel selection,  
                       ElementId elementId, String style ) {
        super(false, elementId.child(CONTAINER_ID), style);
 
        if( cellRenderer == null ) {
            // Create a default one
            cellRenderer = new DefaultCellRenderer<>(elementId.child("item"), style);
        }
        this.cellRenderer = cellRenderer;
 
        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);
 
        grid = new GridPanel(new GridModelDelegate(), elementId.child(ITEMS_ID), style);
        grid.setVisibleColumns(1);
        grid.getControl(GuiControl.class).addListener(new GridListener());
        layout.addChild(grid, BorderLayout.Position.Center);
 
        baseIndex = new DefaultRangedValueModel();
        indexRef = baseIndex.createReference();
        slider = new Slider(baseIndex, Axis.Y, elementId.child(SLIDER_ID), style);
        layout.addChild(slider, BorderLayout.Position.East);
 
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, getElementId(), style);
        }

        // Listen to our own mouse events that don't hit something else
        CursorEventControl.addListenersToSpatial(this, backgroundListener);        

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
        
        if( modelRef.update() ) {
            resetModelRange();
        }
 
        boolean indexUpdate = indexRef.update();
        boolean selectionUpdate = selectionRef.update();         
        if( indexUpdate ) {
            int index = (int)(maxIndex - baseIndex.getValue());
            grid.setRow(index);
        }         
        if( selectionUpdate || indexUpdate ) {
            refreshSelector();
        }
        
        if( activated != entered ) {
            refreshActivation();
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
        if( this.model == model && model != null ) {
            return;
        }
        
        if( this.model != null ) {
            // Clean up the old one
            detachItemListeners();
        }

        if( model == null ) {
            // Easier to create a default one than to handle a null model
            // everywhere
            model = new VersionedList<T>();
        }  
        
        this.model = model;
        this.modelRef = model.createReference();
        
        grid.setLocation(0,0);
        grid.setModel(new GridModelDelegate());  // need a new one for a new version
        resetModelRange();
        baseIndex.setValue(maxIndex);
        refreshSelector();    
    }        

    public VersionedList<T> getModel() {
        return model;
    }

    public Slider getSlider() {
        return slider;
    }
    
    public GridPanel getGridPanel() {
        return grid;
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

    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics 
    public void addCommands( ListAction a, Command<? super ListBox>... commands ) {
        commandMap.addCommands(a, commands);
    }

    public List<Command<? super ListBox>> getCommands( ListAction a ) {
        return commandMap.get(a, false);
    }

    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics 
    public void addClickCommands( Command<? super ListBox>... commands ) {
        commandMap.addCommands(ListAction.Click, commands);
    }

    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics 
    public void removeClickCommands( Command<? super ListBox>... commands ) {
        getClickCommands().removeAll(Arrays.asList(commands));
    } 

    public List<Command<? super ListBox>> getClickCommands() {
        return commandMap.get(ListAction.Click, false);
    }

    @StyleAttribute("listCommands")
    public void setListCommands( Map<ListAction, List<Command<? super ListBox>>> map ) {
        commandMap.clear();
        // We don't use putAll() because (right now) it would potentially
        // put the wrong list implementations into the command map.
        for( Map.Entry<ListAction, List<Command<? super ListBox>>> e : map.entrySet() ) {
            commandMap.addCommands(e.getKey(), e.getValue());
        }
    } 
        
    @StyleAttribute(value="visibleItems", lookupDefault=false)
    public void setVisibleItems( int count ) {
        grid.setVisibleRows(count);
        resetModelRange();
        refreshSelector();
    }
    
    public int getVisibleItems() {
        return grid.getVisibleRows();
    }

    @StyleAttribute(value="cellRenderer", lookupDefault=false)
    public void setCellRenderer( CellRenderer<T> renderer ) {
        if( Objects.equal(this.cellRenderer, renderer) ) {
            return;
        }
        this.cellRenderer = renderer;
        grid.refreshGrid(); // cheating
    }
    
    public CellRenderer<T> getCellRenderer() {
        return cellRenderer;
    }    

    public void setAlpha( float alpha, boolean recursive ) {
        super.setAlpha(alpha, recursive);
        
        // Catch some of our intermediaries
        setChildAlpha(selector, alpha);
    }

    /**
     *  Set to true to enable mouse-wheel style scrolling when the
     *  mouse is hovering over the ListBox. (Versus only when the list
     *  has focus.)  Default is true.
     */
    @StyleAttribute(value="scrollOnHover", lookupDefault=false)
    public void setScrollOnHover( boolean f ) {
        this.scrollOnHover = f;
    }
    
    public boolean getScrollOnHover() {
        return scrollOnHover;
    }

    protected void refreshSelector() {    
        if( selectorArea == null ) {
            return;
        }
        Panel selectedCell = null;
        if( selection != null && !selection.isEmpty() ) {
            // For now just one item... otherwise we have to loop
            // over visible items
            int selected = selection.iterator().next();
            if( selected >= model.size() ) {
                selected = model.size() - 1;
                selection.setSelection(selected);      
            }
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
            selector.setPreferredSize(size);
            
            selectorArea.attachChild(selector);
            selectorArea.setLocalTranslation(grid.getLocalTranslation());            
        }
    }

    protected void resetModelRange() {    
        int count = model == null ? 0 : model.size();
        int visible = grid.getVisibleRows();
        maxIndex = Math.max(0, count - visible);
        
        // Because the slider is upside down, we have to
        // do some math if we want our base not to move as
        // items are added to the list after us
        double val = baseIndex.getMaximum() - baseIndex.getValue();
        
        baseIndex.setMinimum(0);
        baseIndex.setMaximum(maxIndex);
        baseIndex.setValue(maxIndex - val);        
    }

    protected void refreshActivation() {
        if( entered ) {
            activate();
        } else {
            deactivate();
        }
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

    /**
     *  Used when the list model is swapped out.
     */
    protected void detachItemListeners() {
        int base = grid.getRow();
        for( int i = 0; i < grid.getVisibleRows(); i++ ) {
            Panel cell = grid.getCell(base + i, 0);
            if( cell != null ) {
                CursorEventControl.removeListenersFromSpatial(cell, clickListener);
            }
        }
    }

    protected void scroll( int amount ) {
        double delta = getSlider().getDelta();
        double value = getSlider().getModel().getValue();
        getSlider().getModel().setValue(value + delta * amount);   
    }

    protected void activate() {
        if( activated ) {
            return;
        }
        activated = true;
        commandMap.runCommands(ListAction.Entered);
        runEffect(EFFECT_ACTIVATE);
    }
    
    protected void deactivate() {
        if( !activated ) {
            return;
        }
        activated = false;
        commandMap.runCommands(ListAction.Exited);
        runEffect(EFFECT_DEACTIVATE);
    }
    
    @Override
    public String toString() {
        return getClass().getName() + "[elementId=" + getElementId() + "]";
    }
    
    private class ClickListener extends DefaultCursorListener {
 
        // tracks whether we've sent entered events or not
        //private boolean entered = false;
        
        // Tracks whether we've sent pressed events or not
        private boolean pressed = false;
 
        @Override
        protected void click( CursorButtonEvent event, Spatial target, Spatial capture ) {
            //if( !isEnabled() )
            //    return;
            commandMap.runCommands(ListAction.Click);
            runEffect(EFFECT_CLICK);
        }
    
        @Override
        public void cursorButtonEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
 
            // Find the element we clicked on
            int base = grid.getRow();
            for( int i = 0; i < grid.getVisibleRows(); i++ ) {
                Panel cell = grid.getCell( base + i, 0 );
                if( cell == target ) {
                    selection.add(base + i);                    
                }
            }
            
            // List boxes always consume their click events
            event.setConsumed();
        
            // Do our own better handling of 'click' now
            //if( !isEnabled() )
            //    return;                                            
            if( event.isPressed() ) {
                pressed = true;            
                commandMap.runCommands(ListAction.Down);
                runEffect(EFFECT_PRESS);
            } else {
                if( target == capture ) {
                    // Then we are still over the list box and we should run the
                    // click
                    click(event, target, capture);
                }
                // If we run the up without checking properly then we
                // potentially get up events with no down event.  This messes
                // up listeners that are (correctly) expecting an up for every
                // down and no ups without downs.
                // So, any time the capture is us then we will run, else not.
                // ...but that's not right either because if we consume the
                // event (which we do) then the capture will be the item and not
                // the list.  Not sure how it ever worked like that... but I'm
                // leaving it here commented out just in case.
                //if( capture == ListBox.this ) {
                //    commandMap.runCommands(ListAction.Up);
                //    runEffect(EFFECT_RELEASE);
                //}
                if( pressed ) {
                    commandMap.runCommands(ListAction.Up);
                    runEffect(EFFECT_RELEASE);
                    pressed = false;            
                }
            }
        }
    
        @Override
        public void cursorEntered( CursorMotionEvent event, Spatial target, Spatial capture ) {
            entered = true;                        
            /*
            Not sure how this code ever worked but it looks like I meant it.  I can
            find no use-cases in my own codebase so I'm not sure what I was thinking that day.
            Leaving it just in case.
            TODO: may need to readdress if we refactor the mouse/cursor events processing.
            if( capture == ListBox.this || (target == ListBox.this && capture == null) ) {
                entered = true;
                commandMap.runCommands(ListAction.Entered);
                runEffect(EFFECT_ACTIVATE);
            }*/
        }

        @Override
        public void cursorExited( CursorMotionEvent event, Spatial target, Spatial capture ) {
            entered = false;            
            /*if( entered ) {
                commandMap.runCommands(ListAction.Exited);
                runEffect(EFFECT_DEACTIVATE);
                entered = false;
            }*/
        }
    }
 
    /**
     *  Listens to the whole list to intercept things like mouse wheel events
     *  and click to focus.  This should be all we need for hover scrolling as
     *  long as the cell renderers don't consume the motion events.
     */   
    private class BackgroundListener extends DefaultCursorListener {
    
        @Override
        public void cursorEntered( CursorMotionEvent event, Spatial target, Spatial capture ) {
            entered = true;
        }
        
        @Override
        public void cursorExited( CursorMotionEvent event, Spatial target, Spatial capture ) {
            entered = false;
        }
        
        @Override       
        public void cursorMoved( CursorMotionEvent event, Spatial target, Spatial capture ) {
            if( event.getScrollDelta() != 0 ) {
                if( log.isTraceEnabled() ) {
                    log.trace("Scroll delta:" + event.getScrollDelta() + "  value:" + event.getScrollValue());
                }  
                if( scrollOnHover ) {
                    // My wheel moves in multiples of 120... I don't know if that's
                    // universal so we'll at least always send some value. 
                    if( event.getScrollDelta() > 0 ) {
                        scroll(Math.max(1, event.getScrollDelta() / 120));
                    } else {
                        scroll(Math.min(-1, event.getScrollDelta() / 120));
                    }
                }
            }
        }
    } 

    private class GridListener extends AbstractGuiControlListener {
        public void reshape( GuiControl source, Vector3f pos, Vector3f size ) {
            gridResized(pos, size);
            
            // If the grid was re-laid out then we probably need
            // to refresh our selector
            refreshSelector();
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
