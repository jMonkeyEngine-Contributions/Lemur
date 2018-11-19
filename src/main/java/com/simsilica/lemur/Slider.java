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

import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.core.GuiControl;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.core.AbstractGuiControlListener;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.event.DefaultCursorListener;


/**
 *  A composite GUI element consisting of a draggable slider
 *  with increment and decrement buttons at each end.  The slider
 *  value is managed by a RangedValueModel.
 *
 *  @author    Paul Speed
 */
public class Slider extends Panel {

    public static final String ELEMENT_ID = "slider";
    /*public static final String UP_ID = "slider.up.button";
    public static final String DOWN_ID = "slider.down.button";
    public static final String LEFT_ID = "slider.left.button";
    public static final String RIGHT_ID = "slider.right.button";
    public static final String THUMB_ID = "slider.thumb.button";
    public static final String RANGE_ID = "slider.range";*/
    public static final String UP_ID = "up.button";
    public static final String DOWN_ID = "down.button";
    public static final String LEFT_ID = "left.button";
    public static final String RIGHT_ID = "right.button";
    public static final String THUMB_ID = "thumb.button";
    public static final String RANGE_ID = "range";

    private BorderLayout layout;
    private Axis axis;
    private Button increment;
    private Button decrement;
    private Panel  range;
    private Button thumb;

    private RangedValueModel model;
    private double delta = 1.0f;
    private VersionedReference<Double> state;

    public Slider() {
        this(new DefaultRangedValueModel(), Axis.X, true, new ElementId(ELEMENT_ID), null);
    }

    public Slider(Axis axis) {
        this(new DefaultRangedValueModel(), axis, true, new ElementId(ELEMENT_ID), null);
    }

    public Slider(RangedValueModel model) {
        this(model, Axis.X, true, new ElementId(ELEMENT_ID), null);
    }

    public Slider(RangedValueModel model, Axis axis) {
        this(model, axis, true, new ElementId(ELEMENT_ID), null);
    }

    public Slider(String style) {
        this(new DefaultRangedValueModel(), Axis.X, true, new ElementId(ELEMENT_ID), style);
    }

    public Slider(ElementId elementId, String style) {
        this(new DefaultRangedValueModel(), Axis.X, true, elementId, style);
    }

    public Slider(Axis axis, ElementId elementId, String style) {
        this(new DefaultRangedValueModel(), axis, true, elementId, style);
    }

    public Slider(Axis axis, String style) {
        this(new DefaultRangedValueModel(), axis, true, new ElementId(ELEMENT_ID), style);
    }

    public Slider( RangedValueModel model, String style ) {
        this(model, Axis.X, true, new ElementId(ELEMENT_ID), style);
    }

    public Slider( RangedValueModel model, ElementId elementId ) {
        this(model, Axis.X, true, elementId, null);
    }

    public Slider( RangedValueModel model, Axis axis, String style ) {
        this(model, axis, true, new ElementId(ELEMENT_ID), style);
    }

    public Slider( RangedValueModel model, Axis axis, ElementId elementId, String style ) {
        this(model, axis, true, elementId, style);
    }

    protected Slider( RangedValueModel model, Axis axis, boolean applyStyles,
                      ElementId elementId, String style ) {
        super(false, elementId, style);

        // Because the slider accesses styles (for its children) before
        // it has applied its own, it is possible that its default styles
        // will not have been applied.  So we'll make sure.
        Styles styles = GuiGlobals.getInstance().getStyles();
        styles.initializeStyles(getClass());

        this.axis = axis;
        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);
        getControl(GuiControl.class).addListener(new ReshapeListener());

        this.model = model;

        switch( axis ) {
            case X:
                increment = layout.addChild(BorderLayout.Position.East,
                                            new Button(null, true, elementId.child(RIGHT_ID), style));                
                decrement = layout.addChild(BorderLayout.Position.West,
                                            new Button(null, true, elementId.child(LEFT_ID), style));                
                range = layout.addChild(new Panel(true, 50, 2, elementId.child(RANGE_ID), style));
                break;
            case Y:
                increment = layout.addChild(BorderLayout.Position.North,
                                            new Button(null, true, elementId.child(UP_ID), style));
                decrement = layout.addChild(BorderLayout.Position.South,
                                            new Button(null, true, elementId.child(DOWN_ID), style));
                range = layout.addChild(new Panel(true, 2, 50, elementId.child(RANGE_ID), style));
                break;
            case Z:
                throw new IllegalArgumentException("Z axis not yet supported.");
        }
        setupCommands();

        thumb = new Button(null, true, elementId.child(THUMB_ID), style);
        ButtonDragger dragger = new ButtonDragger();
        CursorEventControl.addListenersToSpatial(thumb, dragger);
        attachChild(thumb);

        // A child that is not managed by the layout will not otherwise lay itself
        // out... so we will force it to be its own preferred size.
        thumb.getControl(GuiControl.class).setSize(thumb.getControl(GuiControl.class).getPreferredSize());

        if( applyStyles ) {
            styles.applyStyles(this, elementId, style);
        }
    }
    @SuppressWarnings("unchecked") // because Java doesn't like var-arg generics
    protected final void setupCommands() {
        increment.addClickCommands(new ChangeValueCommand(1));
        decrement.addClickCommands(new ChangeValueCommand(-1));
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Styles styles, Attributes attrs ) {
        ElementId parent = new ElementId(ELEMENT_ID);
        styles.getSelector(parent.child(UP_ID), null).set("text", "^", false);
        styles.getSelector(parent.child(DOWN_ID), null).set("text", "v", false);
        styles.getSelector(parent.child(LEFT_ID), null).set("text", "<", false);
        styles.getSelector(parent.child(RIGHT_ID), null).set("text", ">", false);
        styles.getSelector(parent.child(THUMB_ID), null).set("text", "#", false);
    }

    public void setModel( RangedValueModel model ) {
        if( this.model == model )
            return;
        this.model = model;
        this.state = null;
    }

    public RangedValueModel getModel() {
        return model;
    }

    public void setDelta( double delta ) {
        this.delta = delta;
    }

    public double getDelta() {
        return delta;
    }

    public Button getIncrementButton() {
        return increment;
    }

    public Button getDecrementButton() {
        return decrement;
    }

    public Panel getRangePanel() {
        return range;
    }

    public Button getThumbButton() {
        return thumb;
    }
    
    /**
     *  Returns the slider range value for the specified location
     *  in the slider's local coordinate system.  (For example,
     *  for world space location use slider.worldToLocal() first.)
     */
    public double getValueForLocation( Vector3f loc ) {

        Vector3f relative = loc.subtract(range.getLocalTranslation());

        // Components always grow down from their location
        // so we'll invert y
        relative.y *= -1;
                
        Vector3f axisDir = axis.getDirection();
        double projection = relative.dot(axisDir);
        if( projection < 0 ) {
            if( axis == Axis.Y ) {
                return model.getMaximum();
            } else {
                return model.getMinimum();
            }
        }
        
        Vector3f rangeSize = range.getSize().clone();
         
        double rangeLength = rangeSize.dot(axisDir);
        projection = Math.min(projection, rangeLength);
        double part = projection / rangeLength;       
        double rangeDelta = model.getMaximum() - model.getMinimum();
        
        // For the y-axis, the slider is inverted from the direction
        // that the component's grow... so our part is backwards
        if( axis == Axis.Y ) {
            part = 1 - part;
        }
 
        return model.getMinimum() + rangeDelta * part;        
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

        Vector3f pos = range.getLocalTranslation();
        Vector3f rangeSize = range.getSize();
        Vector3f thumbSize = thumb.getSize();
        Vector3f size = getSize();

        double visibleRange;
        double x;
        double y;

        switch( axis ) {
            case X:
                visibleRange = rangeSize.x - thumbSize.x;

                // Calculate where the thumb center should be
                x = pos.x + visibleRange * model.getPercent();
                y = pos.y - rangeSize.y * 0.5;

                // We cheated and included the half-thumb spacing in x already which
                // is why this is axis-specific.
                thumb.setLocalTranslation((float)x,
                                          (float)(y + thumbSize.y * 0.5),
                                          pos.z + size.z);
                break;
            case Y:
                visibleRange = rangeSize.y - thumbSize.y;

                // Calculate where the thumb center should be
                x = pos.x + rangeSize.x * 0.5;
                y = pos.y - rangeSize.y + (visibleRange * model.getPercent());

                thumb.setLocalTranslation((float)(x - thumbSize.x * 0.5),
                                          (float)(y + thumbSize.y),
                                          pos.z + size.z );
                break;
        }

    }

    private class ChangeValueCommand implements Command<Button> {

        private double scale;

        public ChangeValueCommand( double scale ) {
            this.scale = scale;
        }

        public void execute( Button source ) {
            model.setValue(model.getValue() + delta * scale);
        }
    }

    private class ReshapeListener extends AbstractGuiControlListener {
        @Override
        public void reshape( GuiControl source, Vector3f pos, Vector3f size ) {
            // Make sure the thumb is positioned appropriately
            // for the new size
            resetStateView();   
        }
    }

    private class ButtonDragger extends DefaultCursorListener {

        private Vector2f drag = null;
        private double startPercent;

        @Override
        public void cursorButtonEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
            if( event.getButtonIndex() != MouseInput.BUTTON_LEFT )
                return;

            //if( capture != null && capture != target )
            //    return;

            event.setConsumed();
            if( event.isPressed() ) {
                drag = new Vector2f(event.getX(), event.getY());
                startPercent = model.getPercent();
            } else {
                // Dragging is done.
                drag = null;
            }
        }

        @Override
        public void cursorMoved( CursorMotionEvent event, Spatial target, Spatial capture ) {
            if( drag == null )
                return;

            // Need to figure out how our mouse motion projects
            // onto the slider axis.  Easiest way is to project
            // the end points onto the screen to create a vector
            // against which we can do dot products.
            Vector3f v1 = null;
            Vector3f v2 = null;
            switch( axis ) {
                case X:
                    v1 = new Vector3f(thumb.getSize().x*0.5f,0,0);
                    v2 = v1.add(range.getSize().x - thumb.getSize().x*0.5f, 0, 0);
                    break;
                case Y:
                    v1 = new Vector3f(0,thumb.getSize().y*0.5f,0);
                    v2 = v1.add(0, (range.getSize().y - thumb.getSize().y*0.5f), 0);
                    break;
            }
                        
            v1 = event.getRelativeViewCoordinates(range, v1);
            v2 = event.getRelativeViewCoordinates(range, v2);

            Vector3f dir = v2.subtract(v1);
            float length = dir.length();
            dir.multLocal(1/length);
            Vector3f cursorDir = new Vector3f(event.getX() - drag.x, event.getY() - drag.y, 0);

            float dot = cursorDir.dot(dir);
            
            // Now, the actual amount is then dot/length
            float percent = dot / length;
            model.setPercent(startPercent + percent);

            event.setConsumed();
        }
    }
}
