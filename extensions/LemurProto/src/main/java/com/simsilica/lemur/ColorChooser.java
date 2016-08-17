/*
 * $Id$
 *
 * Copyright (c) 2015, Simsilica, LLC
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

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DefaultCursorListener;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;
import java.awt.Color;


/**
 *
 *
 *  @author    Paul Speed
 */
public class ColorChooser extends Panel {

    public static final String ELEMENT_ID = "colorChooser";
    public static final String CONTAINER_ID = "container";
    public static final String COLORS_ID = "colors";
    public static final String BRIGHTNESS_ID = "brightness.slider";
    public static final String VALUE_ID = "value";

    public static final Texture2D defaultTexture = new Texture2D(256, 256, Image.Format.RGBA8);
    static {
        defaultTexture.getImage().setData(BufferUtils.createByteBuffer(256 * 256 * 4));
        ImageRaster raster = ImageRaster.create(defaultTexture.getImage());
        for( int i = 0; i < 256; i++ ) {
            for( int j = 0; j < 256; j++ ) {
                Color hsb = Color.getHSBColor(i/255f, j/255f, 0.5f);
                raster.setPixel(i, j, toJmeColor(hsb));
            }
        }
    }

    private VersionedObject<ColorRGBA> model;
    private VersionedReference<ColorRGBA> modelRef;
    private Texture2D swatchTexture;

    private Panel value;
    private Container colorPanel;
    private Panel colors;
    private QuadBackgroundComponent valueColor = new QuadBackgroundComponent();
    private QuadBackgroundComponent swatchComponent;
    private Slider brightness;
    private VersionedReference brightnessRef;

    private float hIndex = 0;
    private float sIndex = 0;
    private float bIndex = 0.5f;

    public ColorChooser() {
        this(true, null, new ElementId(ELEMENT_ID), null);
    }

    public ColorChooser( String style ) {
        this(true, null, new ElementId(ELEMENT_ID), style);
    }

    public ColorChooser( ElementId elementId, String style ) {
        this(true, null, elementId, style);
    }

    protected ColorChooser( boolean applyStyles, VersionedObject<ColorRGBA> model,
                            ElementId elementId, String style ) {
        super(false, elementId.child(CONTAINER_ID), style);

        this.swatchTexture = defaultTexture;

        SpringGridLayout layout = new SpringGridLayout();
        getControl(GuiControl.class).setLayout(layout);

        colorPanel = new Container(elementId.child(COLORS_ID), style);
        colorPanel.setLayout(new SpringGridLayout());
        colors = new Panel();
        colorPanel.addChild(colors);
        this.swatchComponent = new QuadBackgroundComponent(swatchTexture);
        CursorEventControl.addListenersToSpatial(colors, new SwatchListener());
        colors.setBackground(swatchComponent);
        colors.setPreferredSize(new Vector3f(256, 64, 0));
        layout.addChild(colorPanel, 2);

        brightness = new Slider(Axis.Y, elementId.child(BRIGHTNESS_ID), style);
        layout.addChild(brightness, 1);
        brightnessRef = brightness.getModel().createReference();

        value = new Panel(elementId.child(VALUE_ID), style);
        value.setPreferredSize(new Vector3f(64, 64, 0));
        value.setBackground(valueColor);
        layout.addChild(value, 0);

        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, getElementId(), style);
        }

        setModel(model);
    }

    public void setModel( VersionedObject<ColorRGBA> model ) {
        if( this.model != null ) {
            // clean up whatever
        }
        if( model == null ) {
            // Create a default model
            model = new VersionedHolder<ColorRGBA>(new ColorRGBA(0.5f, 0.5f, 0.5f, 1));
        }
        this.model = model;
        modelRef = model.createReference();
        updateColorView();
    }

    public VersionedObject<ColorRGBA> getModel() {
        return model;
    }

    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);
        if( modelRef.update() ) {
            updateColorView();
        }
        if( brightnessRef.update() ) {
            updateBrightness();
        }
    }

    protected void updateModelValue( float h, float s, float b ) {
        if( h == hIndex && s == sIndex && b == bIndex ) {
            return;
        }
        this.hIndex = h;
        this.sIndex = s;
        this.bIndex = b;

        Color awtColor = Color.getHSBColor(hIndex, sIndex, bIndex);
        ((VersionedHolder<ColorRGBA>)model).setObject(toJmeColor(awtColor));
    }

    protected void updateBrightness() {
        float v = (float)(brightness.getModel().getValue()/100);
        updateModelValue(hIndex, sIndex, v);
    }

    protected static ColorRGBA toJmeColor( Color clr ) {
        float r = clr.getRed() / 255f;
        float g = clr.getGreen() / 255f;
        float b = clr.getBlue() / 255f;
        return new ColorRGBA(r, g, b, 1);
    }

    protected void updateColorView() {

        ColorRGBA c = model.getObject();

        int r = (int)Math.round(c.getRed() * 255);
        int g = (int)Math.round(c.getGreen() * 255);
        int b = (int)Math.round(c.getBlue() * 255);
        float[] hsb = Color.RGBtoHSB(r, g, b, null);

        this.hIndex = hsb[0];
        this.sIndex = hsb[1];
        this.bIndex = hsb[2];

        updateColorView(hsb[0], hsb[1], hsb[2]);
    }

    protected void updateColorView( float h, float s, float v ) {

        Color awtColor = Color.getHSBColor(h, s, v);
        valueColor.setColor(toJmeColor(awtColor));

        // Now we need to get the B of the HSB to set that one
        brightness.getModel().setValue(v * 100);
    }

    private class SwatchListener extends DefaultCursorListener {

        @Override
        protected void click( CursorButtonEvent event, Spatial target, Spatial capture ) {

            Vector3f world = new Vector3f(event.getX(), event.getY(), 0);
            Vector3f local = colors.worldToLocal(world, null);
            Vector3f size = colors.getSize();
            float h = (local.x / size.x);
            float s = (size.y + local.y) / size.y;
            updateModelValue(h, s, bIndex);
        }
    }
}
