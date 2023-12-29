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
import com.jme3.math.Vector3f;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.AbstractGuiControlListener;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Styles;


/**
 *  A horizontal progress indicator supporting an optional text overlay.
 *  This is a composite component where an indicator panel overlays
 *  the base panel.
 *
 *  @author    Paul Speed
 */
public class ProgressBar extends Panel {

    public static final String ELEMENT_ID = "progress";
    public static final String CONTAINER_ID = "container";
    public static final String LABEL_ID = "label";
    public static final String VALUE_ID = "value";
 
    private BorderLayout layout;
    private Label label;
    private Panel value;
    
    private RangedValueModel model;
    private VersionedReference<Double> state;
 
    public ProgressBar() {
        this(new DefaultRangedValueModel(), true, new ElementId(ELEMENT_ID), null);
    }

    public ProgressBar( String style ) {
        this(new DefaultRangedValueModel(), true, new ElementId(ELEMENT_ID), style);
    }

    public ProgressBar( ElementId elementId, String style ) {
        this(new DefaultRangedValueModel(), true, elementId, style);
    }
 
    public ProgressBar( RangedValueModel model ) {
        this(model, true, new ElementId(ELEMENT_ID), null);
    }

    public ProgressBar( RangedValueModel model, String style ) {
        this(model, true, new ElementId(ELEMENT_ID), style);
    }
        
    protected ProgressBar( RangedValueModel model, boolean applyStyles, 
                           ElementId elementId, String style ) {
        super(false, elementId.child(CONTAINER_ID), style);

        this.model = model;        

        // Because the ProgressBar accesses styles (for its children) before
        // it has applied its own, it is possible that its default styles
        // will not have been applied.  So we'll make sure.
        Styles styles = GuiGlobals.getInstance().getStyles();
        styles.initializeStyles(getClass());

        // Having a label as a child is both nice for the caller as
        // well as convenient for us.  It means we have an easy component
        // to use to get the 'inner size' minus any background margins
        // or insets.
        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);

        // Add the label child.
        label = layout.addChild(new Label("", elementId.child(LABEL_ID), style));
        
        value = new Panel(elementId.child(VALUE_ID), style);
        attachChild(value);

        // We need to know about changes to our size from layout adjustments
        getControl(GuiControl.class).addListener(new ResizeListener());

        if( applyStyles ) {
            styles.applyStyles(this, getElementId(), style);
        }
    }                            
 
    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Styles styles, Attributes attrs ) {
        GuiGlobals globals = GuiGlobals.getInstance();
        ElementId parent = new ElementId(ELEMENT_ID);        
        styles.getSelector(parent.child(CONTAINER_ID), null).set("background", 
                                                new QuadBackgroundComponent(globals.srgbaColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 0.5f)), 2, 2)); 
        styles.getSelector(parent.child(VALUE_ID), null).set("background", 
                                                new QuadBackgroundComponent(globals.srgbaColor(new ColorRGBA(0.1f, 0.7f, 0.3f, 1)))); 
        styles.getSelector(parent.child(LABEL_ID), null).set("textHAlignment", HAlignment.Center, false);
    }
 
    /**
     *  Sets the current progress value as a percentage (0-1.0) of
     *  the current range.
     */
    public void setProgressPercent( double percent ) {
        this.model.setPercent(percent);
    }
    
    /**
     *  Returns the current progress value as a percentage (0-1.0) of
     *  the current range.
     */
    public double getProgressPercent() {
        return this.model.getPercent();
    }

    /**
     *  Sets the raw progress value.
     */
    public void setProgressValue( double val ) {
        this.model.setValue(val);
    }
    
    /**
     *  Returns the raw progress value.
     */
    public double getProgressValue() {
        return this.model.getValue();
    }
 
    /**
     *  Sets the ranged value model that will be used to 
     *  calculate progress percentage.  The default model is
     *  is a DefaultRangedValueModel() where the range is 0 to 100.
     *  If setModel(null) is called then a new default range is
     *  created. 
     */   
    public void setModel( RangedValueModel model ) {
        if( this.model == model ) {
            return;
        }
        if( model == null ) {
            model = new DefaultRangedValueModel();
        }
        this.model = model;
        this.state = null;
    }
 
    /**
     *  Returns the current range model for this progress bar.
     */   
    public RangedValueModel getModel() {
        return model;
    }
 
    /**
     *  Sets the message text that appears in the progress bar text overlay.
     */
    public void setMessage( String message ) {
        label.setText(message);
    }
 
    /** 
     *  Returns the message text that currently appears in the progress bar text
     *  overlay.
     */
    public String getMessage() {
        return label.getText();
    }
 
    /**
     *  Returns the GUI element that is used for the main progress
     *  bar area and overlay label.  This can be used to apply special
     *  styling.
     */   
    public Label getLabel() {
        return label;
    }
    
    /**
     *  Returns the GUI element that is used for the value indicator.
     *  This can be used to apply special styling.
     */   
    public Panel getValueIndicator() {
        return value;
    }
    
    @Override
    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        if( state == null || state.update() ) {
            resetStateView();
        }
    }

    protected void resetStateView() {
        if( state == null ) {
            state = model.createReference();
        }

        Vector3f labelSize = label.getSize();
        Vector3f labelPos = label.getLocalTranslation();
        double width = model.getPercent() * labelSize.x;
        value.setSize(new Vector3f((float)width, labelSize.y, labelSize.z));
        
        // The way we order these layers is both fragile and inflexible.
        value.setLocalTranslation(labelPos.x, labelPos.y, labelPos.z * 0.5f);
    }
    
    protected class ResizeListener extends AbstractGuiControlListener {
        @Override
        public void reshape( GuiControl source, Vector3f pos, Vector3f size ) {
            // If we don't reset the progress bar size then we can end up with cases
            // where the label resizes and leaves the progress bar in an invalid state,
            // potentially even hanging off the edge of the outer panel.
            resetStateView();
        }
    }
}
