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
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.core.GuiControl;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import com.simsilica.lemur.component.BorderLayout;
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
    public static final String UP_ID = "slider.up.button";
    public static final String DOWN_ID = "slider.down.button";
    public static final String LEFT_ID = "slider.left.button";
    public static final String RIGHT_ID = "slider.right.button";
    public static final String THUMB_ID = "slider.thumb.button";
    public static final String RANGE_ID = "slider.range";

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

    public Slider(Axis axis, String style) {
        this(new DefaultRangedValueModel(), axis, true, new ElementId(ELEMENT_ID), style);
    }

    public Slider( RangedValueModel model, String style ) {
        this(model, Axis.X, true, new ElementId(ELEMENT_ID), style);
    }

    public Slider( RangedValueModel model, Axis axis, String style ) {
        this(model, axis, true, new ElementId(ELEMENT_ID), style);
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

        this.model = model;

        switch( axis ) {
            case X:
                increment = layout.addChild(BorderLayout.Position.East,
                                            new Button(null, true, new ElementId(RIGHT_ID), style));
                increment.addClickCommands( new ChangeValueCommand(1) );
                decrement = layout.addChild(BorderLayout.Position.West,
                                            new Button(null, true, new ElementId(LEFT_ID), style));
                decrement.addClickCommands( new ChangeValueCommand(-1) );
                range = layout.addChild(new Panel(true, 50, 2, new ElementId(RANGE_ID), style));
                break;
            case Y:
                increment = layout.addChild(BorderLayout.Position.North,
                                            new Button(null, true, new ElementId(UP_ID), style));
                increment.addClickCommands( new ChangeValueCommand(1) );
                decrement = layout.addChild(BorderLayout.Position.South,
                                            new Button(null, true, new ElementId(DOWN_ID), style));
                decrement.addClickCommands( new ChangeValueCommand(-1) );
                range = layout.addChild(new Panel(true, 2, 50, new ElementId(RANGE_ID), style));
                break;
            case Z:
                throw new IllegalArgumentException("Z axis not yet supported.");
        }

        thumb = new Button(null, true, new ElementId(THUMB_ID), style);
        ButtonDragger dragger = new ButtonDragger();
        CursorEventControl.addListenersToSpatial(thumb, dragger);
        CursorEventControl.addListenersToSpatial(range, dragger);
        attachChild(thumb);

        // A child that is not managed by the layout will not otherwise lay itself
        // out... so we will force it to be its own preferred size.
        thumb.getControl(GuiControl.class).setSize(thumb.getControl(GuiControl.class).getPreferredSize());

        if( applyStyles ) {
            styles.applyStyles(this, elementId.getId(), style);
        }
    }

    @StyleDefaults(ELEMENT_ID)
    public static void initializeDefaultStyles( Styles styles, Attributes attrs ) {
        styles.getSelector(UP_ID, null).set("text", "^", false);
        styles.getSelector(DOWN_ID, null).set("text", "v", false);
        styles.getSelector(LEFT_ID, null).set("text", "<", false);
        styles.getSelector(RIGHT_ID, null).set("text", ">", false);
        styles.getSelector(THUMB_ID, null).set("text", "#", false);
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
        }
    }
}
