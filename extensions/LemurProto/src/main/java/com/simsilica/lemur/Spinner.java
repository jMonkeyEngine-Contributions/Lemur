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

import java.util.Objects;

import org.slf4j.*;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.core.*;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.FocusMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import com.simsilica.lemur.focus.*;
import com.simsilica.lemur.style.*;
import com.simsilica.lemur.value.DefaultValueRenderer;

/**
 *  A GUI element that allows 'spinning' through a potentially
 *  unbound sequence of values.  This includes a value display
 *  and optional editor for setting the value directly.
 *  The "up" button will proceed to the next value in the sequence
 *  and the "down" button will process to the previous value.
 *  It's up to the model itself why 'previous' and 'next' mean.
 *
 *  @author    Paul Speed
 */
public class Spinner<T> extends Panel {

    static Logger log = LoggerFactory.getLogger(Spinner.class);

    public static final String ELEMENT_ID = "spinner";
    public static final String VALUE_ID = "value";
    public static final String EDITOR_ID = "editor";
    public static final String BUTTON_PANEL_ID = "buttons.container";
    public static final String UP_ID = "up.button";
    public static final String DOWN_ID = "down.button";
 
    public static final String EFFECT_NEXT = "next";
    public static final String EFFECT_PREVIOUS = "previous";
    public static final String EFFECT_ACTIVATE = "activate";
    public static final String EFFECT_DEACTIVATE = "deactivate";
    public static final String EFFECT_START_EDIT = "startEdit";
    public static final String EFFECT_STOP_EDIT = "stopEdit";
    public static final String EFFECT_FOCUS = "focus";
    public static final String EFFECT_UNFOCUS = "unfocus";
    public static final String EFFECT_ENABLE = "enable";
    public static final String EFFECT_DISABLE = "disable";
    
    public enum SpinnerAction { PreviousValue, NextValue, 
                                StartEdit, StopEdit, 
                                HighlightOn, HighlightOff, 
                                FocusGained, FocusLost, 
                                Hover,
                                Enabled, Disabled };
 
 
    private SequenceModel<T> model;
    private VersionedReference<T> modelRef;
    
    private ValueRenderer<T> valueRenderer;
    private ValueEditor<T> valueEditor;
    
    private SpringGridLayout layout;
    private Panel view;
    private Panel edit;
    private Button previous;
    private Button next; 
    private FocusObserver focusObserver = new FocusObserver();
    private SpinnerMouseHandler mouseHandler = new SpinnerMouseHandler();

    private boolean enabled = true;    
    private CommandMap<Spinner, SpinnerAction> commandMap
                                                = new CommandMap<Spinner, SpinnerAction>(this);
    
    public Spinner( SequenceModel<T> model ) {
        this(true, model, null, new ElementId(ELEMENT_ID), null);
    }

    public Spinner( SequenceModel<T> model, ValueRenderer<T> valueRenderer ) {  
        this(true, model, valueRenderer, new ElementId(ELEMENT_ID), null);
    }

    public Spinner( SequenceModel<T> model, String style ) {
        this(true, model, null, new ElementId(ELEMENT_ID), style);
    }
    
    public Spinner( SequenceModel<T> model, ValueRenderer<T> valueRenderer, String style ) {
        this(true, model, valueRenderer, new ElementId(ELEMENT_ID), style);
    }
    
    public Spinner( SequenceModel<T> model, ValueRenderer<T> valueRenderer, 
                    ElementId elementId, String style ) {
        this(true, model, valueRenderer, elementId, style);
    }
    
    protected Spinner( boolean applyStyles, SequenceModel<T> model, ValueRenderer<T> valueRenderer,  
                       ElementId elementId, String style ) {
        super(false, elementId, style);

        this.layout = new SpringGridLayout(Axis.Y, Axis.X, 
                                           FillMode.ForcedEven,
                                           FillMode.First);         
        getControl(GuiControl.class).setLayout(layout);
 
        if( valueRenderer == null ) {
            // Create a default one
            valueRenderer = new DefaultValueRenderer<>(elementId.child(VALUE_ID), style);
        } else {
            valueRenderer.configureStyle(elementId.child(VALUE_ID), style);
        }
        this.valueRenderer = valueRenderer;
 
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
        }
 
        // Build the children after styles so that we pick up
        // defaults.  But note it means that styles setters can
        // never pass attributes through to these uncreated children.
        Container buttons = layout.addChild(new Container(elementId.child(BUTTON_PANEL_ID), style), 0, 1);
        this.next = buttons.addChild(new Button(null, elementId.child(UP_ID), style));
        this.previous = buttons.addChild(new Button(null, elementId.child(DOWN_ID), style));        
 
        next.getControl(GuiControl.class).setFocusable(false);        
        next.addClickCommands(new Command<Button>() {
                public void execute( Button source ) {
                    nextValue();
                }
            });
        previous.getControl(GuiControl.class).setFocusable(false);        
        previous.addClickCommands(new Command<Button>() {
                public void execute( Button source ) {
                    previousValue();
                }
            });
        
        setModel(model);                
    }
 
    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Styles styles, Attributes attrs ) {
        ElementId parent = new ElementId(ELEMENT_ID);  
        styles.getSelector(parent.child(UP_ID), null).set("text", "+", false);
        styles.getSelector(parent.child(UP_ID), null).set("insets", new Insets3f(0, 0, 0, 0), false);
        styles.getSelector(parent.child(DOWN_ID), null).set("text", "-", false);
        styles.getSelector(parent.child(DOWN_ID), null).set("insets", new Insets3f(0, 0, 0, 0), false);
        styles.getSelector(parent.child(VALUE_ID), null).set("textVAlignment", VAlignment.Center, false);
    }

    @Override
    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        if( modelRef == null || modelRef.update() ) {
            resetValue();
        }
        if( valueEditor != null ) {
            if( edit != null && !valueEditor.updateState(tpf) ) {
                stopEditing();
            }
        }
    }

    public void setModel( SequenceModel<T> model ) {
        if( this.model == model )
            return;
        this.model = model;
        this.modelRef = null;
    }

    public SequenceModel<T> getModel() {
        return model;
    }

    public void setValueEditor( ValueEditor<T> valueEditor ) {
        if( Objects.equals(this.valueEditor, valueEditor) ) {
            return;
        }
        stopEditing();
        this.valueEditor = valueEditor;
    }
    
    public ValueEditor<T> getValueEditor() {
        return valueEditor;
    }

    public void setValue( T value ) {
        getModel().setObject(value);
    }
    
    public T getValue() {
        if( model == null ) {
            return null;
        }
        return getModel().getObject();
    }

    public void nextValue() {
        if( model == null ) {
            return;
        }
        model.setObject(model.getNextObject());
        commandMap.runCommands(SpinnerAction.NextValue);
        runEffect(EFFECT_NEXT);
    }
    
    public void previousValue() {
        if( model == null ) {
            return;
        }
        model.setObject(model.getPreviousObject());
        commandMap.runCommands(SpinnerAction.PreviousValue);
        runEffect(EFFECT_PREVIOUS);
    }
 
    public void setEnabled( boolean b ) {
        if( this.enabled == b )
            return;
        this.enabled = b;
         
        if( isEnabled() ) {
            commandMap.runCommands(SpinnerAction.Enabled);
            runEffect(EFFECT_ENABLE);
        } else {
            commandMap.runCommands(SpinnerAction.Disabled);
            runEffect(EFFECT_DISABLE);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void startEditing() {
        if( valueEditor == null ) {
            return;
        }
        commandMap.runCommands(SpinnerAction.StartEdit);
        runEffect(EFFECT_START_EDIT);
        
        if( this.view != null ) {
            layout.removeChild(this.view);
        }
        
        Panel newEdit = valueEditor.startEditing(model.getObject());
        if( newEdit != edit ) {
            layout.removeChild(edit);
        }
        
        this.edit = newEdit;
        layout.addChild(edit, 0, 0);
        GuiGlobals.getInstance().requestFocus(edit); 
    }
    
    public void stopEditing() {
        if( valueEditor == null ) {
            return;
        }
        
        // Take what we got
        model.setObject(valueEditor.getObject());
        
        commandMap.runCommands(SpinnerAction.StopEdit);
        runEffect(EFFECT_STOP_EDIT);
 
        // If the focus is still on the edit field then we will
        // need to transfer focus somewhere else.  This indicates that
        // editing was stopped programatically.
        boolean navigateNext = edit.getControl(GuiControl.class).isFocused();
        
        layout.removeChild(edit);
        edit = null;
        if( this.view != null ) {
            layout.addChild(view, 0, 0);
        }
        
        if( navigateNext ) {
            GuiGlobals.getInstance().getFocusNavigationState().requestChangeFocus(this, FocusTraversal.TraversalDirection.Next);
        }        
    }

    public boolean isEditing() {
        return edit != null;
    }

    protected void setView( Panel view ) {
        if( this.view == view ) {
            return;
        }
        if( this.view != null ) {
            this.view.getControl(GuiControl.class).removeFocusChangeListener(focusObserver);
            MouseEventControl.removeListenersFromSpatial(this.view, FocusMouseListener.INSTANCE, mouseHandler); 
            layout.removeChild(this.view);
        }
        this.view = view;
        if( this.view != null ) {
            this.view.getControl(GuiControl.class).addFocusChangeListener(focusObserver);
            this.view.getControl(GuiControl.class).setFocusable(true);
            MouseEventControl.addListenersToSpatial(this.view, FocusMouseListener.INSTANCE, mouseHandler); 
            if( !isEditing() ) {
                layout.addChild(this.view, 0, 0);
            }           
        }
    }
    
    protected void resetValue() {
        if( modelRef == null ) {
            modelRef = model.createReference();
        }
        setView(valueRenderer.getView(modelRef.get(), false, this.view));
    } 
    
    protected class FocusObserver implements FocusChangeListener, StateFunctionListener {
        
        public void focusGained( FocusChangeEvent event ) {
            if( !isEnabled() ) {
                return;
            }
            commandMap.runCommands(SpinnerAction.FocusGained);
            runEffect(EFFECT_FOCUS);
            
            // Adding this listener just in case we ever want to support
            // separate focus and editing... ie: navigate to the field and then
            // press a button to activate editing.
            GuiGlobals.getInstance().getInputMapper().addStateListener(this, FocusNavigationFunctions.F_ACTIVATE);
            
            startEditing();
        }
        
        public void focusLost( FocusChangeEvent event ) {
            GuiGlobals.getInstance().getInputMapper().removeStateListener(this, FocusNavigationFunctions.F_ACTIVATE);
 
            // It's actually up to the editor to stop... because by nature
            // we may lose focus as soon as the editor is active anyway.
            //stopEditing();
           
            commandMap.runCommands(SpinnerAction.FocusLost);
            runEffect(EFFECT_UNFOCUS);            
        }
        
        public void valueChanged( FunctionId func, InputState value, double tpf ) {
            //if( pressed && value == InputState.Off ) {
                // Do click processing... the mouse does click processing before
                // up processing so we will too
                //runClick();
            //}
            // Only mapped to one function so no need to distinguish
            //setPressed(isEnabled() && value == InputState.Positive);
        }
    }

    protected class SpinnerMouseHandler extends DefaultMouseListener {

        @Override
        protected void click( MouseButtonEvent event, Spatial target, Spatial capture ) {
        }

        @Override
        public void mouseButtonEvent( MouseButtonEvent event, Spatial target, Spatial capture ) {
        
            // Buttons always consume their click events
            event.setConsumed();
        
            // Do our own better handling of 'click' now
            //super.mouseButtonEvent(event, target, capture);
            if( !isEnabled() )
                return;                                            

            //if( event.isPressed() ) {
                //setPressed(event.isPressed());
            //} else if( isPressed() ) {
            //    // Only run the up processing if we were already pressed
            //    // This also handles the case where we weren't enabled before
            //    // but are now, etc.
            //    
            //    if( target == capture ) {
            //        // Then we are still over the button and we should run the
            //        // click
            //        //runClick();
            //    }
            //    // If we run the up without checking properly then we
            //    // potentially get up events with no down event.  This messes
            //    // up listeners that are (correctly) expecting an up for every
            //    // down and no ups without downs.
            //    // So, any time the capture is us then we will run, else not
            //    //if( capture == Button.this ) {
            //    //    setPressed(false);
            //    //}
            //}
        }

        @Override
        public void mouseEntered( MouseMotionEvent event, Spatial target, Spatial capture ) {
            if( !isEnabled() )
                return;
            if( capture == Spinner.this || (target == Spinner.this && capture == null) ) {
                //showHighlight(true);
                commandMap.runCommands(SpinnerAction.HighlightOn);
                runEffect(EFFECT_ACTIVATE);
            }
        }

        @Override
        public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture ) {
            //if( !isEnabled() )
            //    return;
            //if( !isHighlightOn() ) {
            //    // If the highlight is on then we need to run through
            //    // the events regardless of enabled state... and if it's 
            //    // not on then there is no reason to run events. 
            //}
            //showHighlight(false);
            commandMap.runCommands(SpinnerAction.HighlightOff);
            runEffect(EFFECT_DEACTIVATE);
        }
        
        @Override
        public void mouseMoved( MouseMotionEvent event, Spatial target, Spatial capture ) {
            //System.out.println("mouseMoved(" + event + ")");
            commandMap.runCommands(SpinnerAction.Hover);
        }
        
    }
}


