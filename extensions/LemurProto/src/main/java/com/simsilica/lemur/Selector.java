/*
 * $Id$
 *
 * Copyright (c) 2020, Simsilica, LLC
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

import java.util.List;
import java.util.Objects;

import org.slf4j.*;

import com.google.common.base.Function;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.core.*;
import com.simsilica.lemur.component.*;
import com.simsilica.lemur.event.*;
import com.simsilica.lemur.list.*;
import com.simsilica.lemur.style.*;
import com.simsilica.lemur.value.DefaultValueRenderer;


/**
 *  A GUI element that presents a value and a drop down for selecting
 *  a different value.
 *
 *  @author    Paul Speed
 */
public class Selector<T> extends Panel {

    static Logger log = LoggerFactory.getLogger(Selector.class);

    public static final String ELEMENT_ID = "selector";
    public static final String CONTAINER_ID = "container";
    public static final String EXPANDER_ID = "down.button";

    private BorderLayout layout;

    private ValueRenderer<T> valueRenderer;
    private ListBox<T> listBox;
    private Panel view;
    private Button expander;
    private Container popup; // because it won't look good with the default list background

    private ClickListener clickListener = new ClickListener();
    private SelectListener selectListener = new SelectListener();
    private ReshapeListener reshapeListener = new ReshapeListener();
    private AutoCloseListener autoCloseListener = new AutoCloseListener(); 

    private boolean expanded;
    private int maximumVisibleItems;
    private long expandedFrames;

    private VersionedReference<Integer> selectionRef;
    private VersionedHolder<T> selectedItem = new VersionedHolder<>();
    private VersionedReference<List<T>> modelRef;

    public Selector() {
        this(true, new VersionedList<T>(), null,
             new SelectionModel(),
             new ElementId(ELEMENT_ID), null);
    }

    public Selector( VersionedList<T> model ) {
        this(true, model, null,
                new SelectionModel(), new ElementId(ELEMENT_ID), null);
    }

    public Selector( VersionedList<T> model, Function<? super T, String> stringTransform ) {
        this(true, model,
                new DefaultValueRenderer<T>(new ElementId(ELEMENT_ID).child("item"), null, stringTransform),
                new SelectionModel(), new ElementId(ELEMENT_ID), null);
    }

    public Selector( VersionedList<T> model, ValueRenderer<T> renderer ) {
        this(true, model, renderer, new SelectionModel(), new ElementId(ELEMENT_ID), null);
    }

    public Selector( VersionedList<T> model, ValueRenderer<T> renderer, String style ) {
        this(true, model, renderer, new SelectionModel(), new ElementId(ELEMENT_ID), style);
    }

    public Selector( VersionedList<T> model, String style ) {
        this(true, model, null, new SelectionModel(), new ElementId(ELEMENT_ID), style);
    }

    public Selector( VersionedList<T> model, ElementId elementId ) {
        this(true, model, null, new SelectionModel(), elementId, null);
    }

    public Selector( VersionedList<T> model, ElementId elementId, String style ) {
        this(true, model, null, new SelectionModel(), elementId, style);
    }

    public Selector( VersionedList<T> model, ValueRenderer<T> renderer, ElementId elementId, String style ) {
        this(true, model, renderer, new SelectionModel(), elementId, style);
    }

    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics
    protected Selector( boolean applyStyles, VersionedList<T> model, ValueRenderer<T> valueRenderer,
                        SelectionModel selection, ElementId elementId, String style ) {
        super(false, elementId.child(CONTAINER_ID), style);

        // For now we will internally use a ListBox for the drop down part.
        // I think any problems we have with this are actually ListBox problems
        // and so may work themselves out in the end.  For example, it would be
        // nice to have a scroll bar only when required.

        // We create it here to share it with the ListBox because we will
        // need to render the element for display.
        if( valueRenderer == null ) {
            // Create a default one
            valueRenderer = new DefaultValueRenderer<>(elementId.child("item"), style);
        } else  {
            valueRenderer.configureStyle(elementId.child("item"), style);
        }
        this.valueRenderer = valueRenderer;

        this.listBox = new ListBox<>(model, valueRenderer, elementId.child("list"), style);
        listBox.setSelectionModel(selection);
        listBox.addClickCommands(selectListener);
        this.modelRef = listBox.getModel().createReference();
        this.selectionRef = listBox.getSelectionModel().createSelectionReference();
        boundSelection();
        selectedItem.setObject(getSelectedListValue());

        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);

        // Apply styles before creating children to make sure the
        // default styles are applied first.
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, getElementId(), style);
        }

        this.popup = new Container(new BorderLayout(), elementId.child("popup"), style);
        popup.addChild(listBox, BorderLayout.Position.Center);
        popup.getControl(GuiControl.class).addUpdateListener(autoCloseListener);

        this.expander = new Button(null, true, elementId.child(EXPANDER_ID), style);
        expander.addClickCommands(clickListener);

        layout.addChild(BorderLayout.Position.East, expander);

        resetView();
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Styles styles, Attributes attrs ) {
        ElementId parent = new ElementId(ELEMENT_ID);
        styles.getSelector(parent.child(EXPANDER_ID), null).set("text", "v", false);
    }

    public VersionedList<T> getModel() {
        return listBox.getModel();
    }

    public void setModel( VersionedList<T> model ) {
        listBox.setModel(model);
        this.modelRef = model.createReference();
        boundSelection();
    }

    public void setSelectionModel( SelectionModel selectionModel ) {
        listBox.setSelectionModel(selectionModel);
        this.selectionRef = listBox.getSelectionModel().createSelectionReference();
        boundSelection();
    }

    public SelectionModel getSelectionModel() {
        return listBox.getSelectionModel();
    }

    public void setValueRenderer( ValueRenderer<T> valueRenderer ) {
        if( this.valueRenderer == valueRenderer ) {
            return;
        }
        this.valueRenderer = valueRenderer;
        listBox.setCellRenderer(valueRenderer);
        resetView();
    }

    public ValueRenderer<T> getValueRenderer() {
        return valueRenderer;
    }

    public ListBox getListBox() {
        return listBox;
    }

    public Button getExpanderButton() {
        return expander;
    }

    public Container getPopupContainer() {
        return popup;
    }

    @StyleAttribute(value="maximumVisibleItems", lookupDefault=false)
    public void setMaximumVisibleItems( int count ) {
        if( this.maximumVisibleItems == count ) {
            return;
        }
        this.maximumVisibleItems = count;
    }

    public int getMaximumVisibleItems() {
        return maximumVisibleItems;
    }

    public void setSelectedItem( T item ) {
        int i = listBox.getModel().indexOf(item);
        if( i < 0 ) {
            log.warn("No item in list:" + item);
            listBox.getSelectionModel().clear();
        } else {
            listBox.getSelectionModel().setSelection(i);

            // Make sure that the selected item reference reflects
            // the value right away.
            updateSelection();
        }
    }

    public T getSelectedItem() {
        updateSelection();
        return selectedItem.getObject();
    }

    public VersionedReference<T> createSelectedItemReference() {
        return selectedItem.createReference();
    }

    /**
     *  Attempts to make sure that the selected item is always in range.
     */
    protected void boundSelection() {
        Integer i = listBox.getSelectionModel().getSelection();
        if( i == null ) {
            if( !listBox.getModel().isEmpty() ) {
                listBox.getSelectionModel().setSelection(0);
            }
        } else if( i >= listBox.getModel().size() ) {
            // clamp it
            listBox.getSelectionModel().setSelection(listBox.getModel().size()-1);
        }
    }

    protected void updateSelection() {
        // Even if our selection index hasn't moved the model may have
        // changed and invalidated our actual selection value.  This 
        // is extra work we need to do because of the keeping of codependent
        // state and this may not be the last of it.
        if( selectionRef.update() || modelRef.needsUpdate() ) {        
            Integer i = selectionRef.get();
            if( i == null ) {
                selectedItem.setObject(null);
            } else {
                // Clamp it in range
                i = Math.min(i, listBox.getModel().size()-1);
                i = Math.max(i, 0);
                selectedItem.setObject(listBox.getModel().get(i));
            }
            resetView();
        }
    }

    @Override
    public void updateLogicalState( float tpf ) {
        if( expanded ) {
            expandedFrames++;
        }
        super.updateLogicalState(tpf);
        if( modelRef.update() ) {
            boundSelection();

            // Don't try to fix the selection if the selectionRef is already
            // out of date.  It's quite possible that it's already accurate
            // with the latest list model and trying to move the selection
            // will end up reverting it.
            if( !selectionRef.needsUpdate() ) {
                // Make sure that the selected item is pointing to
                // something that exists or the proper item if it has moved.
                T item = null;
                Integer i = selectionRef.get();
                if( i != null ) {
                    item = listBox.getModel().get(i);
                }
                if( !Objects.equals(item, selectedItem.getObject()) ) {
                    // See whether it's gone or just moved
                    int newIndex = listBox.getModel().indexOf(selectedItem.getObject());
                    if( newIndex < 0 ) {
                        // It's gone... so we need to reassert the current
                        // selection
                        listBox.getSelectionModel().clear();
                        listBox.getSelectionModel().setSelection(i);
                    } else {
                        // Else it's just moved
                        listBox.getSelectionModel().setSelection(newIndex);
                    }
                }
            }
        }
        updateSelection();
    }

    public void setExpanded( boolean b ) {
        if( this.expanded == b ) {
            return;
        }
        if( b ) {
            expand();
        } else {
            collapse();
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    protected void resetView() {
        Panel newView = valueRenderer.getView(getSelectedListValue(), false, view);
        if( newView != view ) {
            // Transfer the click listener
            CursorEventControl.addListenersToSpatial(newView, clickListener);
            CursorEventControl.removeListenersFromSpatial(view, clickListener);

            this.view = newView;
            layout.addChild(view, BorderLayout.Position.Center);
        }
    }

    protected T getSelectedListValue() {
        Integer i = listBox.getSelectionModel().getSelection();
        if( i == null ) {
            return null;
        }
        if( i >= listBox.getModel().size() ) {
            i = 0;
        }
        return listBox.getModel().get(i);
    }

    protected int calculateListSize() {
        int size = listBox.getModel().size();
        if( maximumVisibleItems != 0 ) {
            size = Math.min(maximumVisibleItems, size);
        }
        // Either way, we should make sure that we don't fall off the
        // bottom or sides of the screen.
        // We'll guess the size from the view
        Vector2f guiSize = GuiGlobals.getInstance().getPopupState().getGuiSize();
        int maxSize = (int)(guiSize.y / view.getSize().y);
        maxSize = Math.max(maxSize, 3); // always show at least 3 items
        size = Math.min(size, maxSize);

        return size;
    }

    protected Vector3f calculatePopupLocation( Vector3f screen ) {
        Vector3f loc = GuiGlobals.getInstance().getPopupState().screenToGui(screen); 
        Vector3f pref = popup.getPreferredSize();
        Vector2f guiSize = GuiGlobals.getInstance().getPopupState().getGuiSize();
        loc.x = Math.min(loc.x, guiSize.x - pref.x);
        // y grows down in Lemur
        loc.y = Math.max(loc.y, pref.y);
        return loc;
    }

    protected void expand() {
        listBox.setVisibleItems(calculateListSize());
        popup.setLocalTranslation(calculatePopupLocation(getWorldTranslation().clone()));

        // Make sure we keep it on screen even if it resizes itself
        popup.getControl(GuiControl.class).addListener(reshapeListener);

        GuiGlobals.getInstance().getPopupState()
                .showPopup(popup, new Command<PopupState>() {
                        public void execute( PopupState state ) {
                            collapse();
                        }
                    });

        expandedFrames = 0;
        autoCloseListener.updatedFrames = 0;

        this.expanded = true;
    }

    protected void collapse() {
        PopupState state = GuiGlobals.getInstance().getPopupState();
        if( state.isPopup(popup) ) {
            state.closePopup(popup);
        }
        popup.getControl(GuiControl.class).removeListener(reshapeListener);
        this.expanded = false;
    }

    private class ClickListener extends DefaultCursorListener implements Command<Button> {

        @Override
        protected void click( CursorButtonEvent event, Spatial target, Spatial capture ) {
            expand();
        }

        public void execute( Button button ) {
            expand();
        }
    }

    private class SelectListener implements Command<ListBox> {
        public void execute( ListBox list ) {
            collapse();
        }
    }

    private class ReshapeListener extends AbstractGuiControlListener {

        @Override
        public void reshape( GuiControl source, Vector3f pos, Vector3f size ) {
            // Note: reshape() is about the layout within the container
            // and not its position on screen... so moving the popup isn't
            // really a recursive operation.
            //Vector3f world = popup.getLocalTranslation();
            // I'm pretty sure the above is a bug because we even called it 'world'
            // but there are times when the world and local translations will be
            // different, even for a popup directly in the GUI node.
            Vector3f world = popup.getWorldTranslation();
            Vector3f loc = calculatePopupLocation(world);
            popup.setLocalTranslation(loc);
        }
    }
 
    /**
     *  Listens to the update of the popup so we can count frames.  If
     *  the popup frames become greater than the selector frames then we
     *  guess that the selector has been removed from the scene and we
     *  are still popped up. 
     */   
    private class AutoCloseListener implements GuiUpdateListener {
        private long updatedFrames = 0;
            
        public void guiUpdate( GuiControl source, float tpf ) {
            updatedFrames++;
            if( updatedFrames > expandedFrames ) {
                log.warn("Auto-closing left-open selector.");
                // The selector was removed from the scene without anything
                // being selected in the popup... so we'll close.
                collapse();
            }
        }
    }
}


