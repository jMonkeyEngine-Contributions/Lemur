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

package com.simsilica.lemur.geom;

import com.jme3.math.*;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;

/**
 *  A three-by-three quad that can stretch textures in useful
 *  ways.  The vertexes are arranged with the 10 outer vertexes
 *  first and then the 4 inner vertexes.
 *
 * <p>The texture is split up into a three-by-three grid in this fashion:
 *
 * <pre>
 *    +---+--------------+-------+
 *    |   |    &lt;-----&gt;   |       |
 * y2 +---+--------------+-------+
 *    |   |              |       |
 *    | ^ |       ^      |   ^   |
 *    | | |       |      |   |   |
 *    | | |    &lt;-----&gt;   |   |   |
 *    | | |       |      |   |   |
 *    | v |       v      |   v   |
 *    |   |              |       |
 * y1 +---+--------------+-------+
 *    |   |    &lt;-----&gt;   |       |
 *    +---+--------------+-------+
 *       x1             x2
 * </pre>
 *
 * Arrows indicate the direction in which each grid cell will stretch to fill
 * the area given by the component.<br>
 * All coordinates are relative to the lower-left border.
 * <p>
 * <b>Common pitfalls</b>
 * <ul>
 * <li>Placing controls on a fractional coordinate. This usually happens when
 * centering controls.
 * <li>Having a contrast at the border between stretched and unstretched texture
 * zone (e.g. a black border and a white center). Stretching involves
 * interpolating colors with the neighbouring pixels (i.e. you'll get a
 * black-to-white color gradient).<br>
 * The easiest workaround is to make the border one pixel wider so that the
 * contrast will be safely inside the unstretched area.
 * <li>Using a texture that is larger than the quad.
 * </ul>
 *
 * @author Paul Speed, Joachim "Toolforger" Durchholz (Javadoc)
 */
public class TbtQuad extends Mesh
                     implements Cloneable {

    // Internally, we need to keep track of the
    // overall size of the quad as well as where
    // the horizontal and vertical "folds" are.
    // Related texture coordinates must similarly
    // be tracked.
    private Vector2f size;
    private Vector2f imageSize;
    private float[] horzFolds;
    private float[] vertFolds;
    private float[] horzTexCoords;
    private float[] vertTexCoords;

    public TbtQuad( float width, float height ) {
        this.size = new Vector2f(width, height);
        this.imageSize = new Vector2f(width, height);
        this.horzFolds = new float[] { width/3f, 2 * width/3f };
        this.vertFolds = new float[] { height/3f, 2 * height/3f };
        this.horzTexCoords = new float[] { 0, 1/3f, 2/3f, 1 };
        this.vertTexCoords = new float[] { 0, 1/3f, 2/3f, 1 };
        refreshGeometry();
    }

    public TbtQuad( float width, float height, int x1, int y1, int x2, int y2, int imageWidth, int imageHeight, float imageScale ) {
        this.size = new Vector2f(width, height);

        // x1, y1 and x2, y2 are the insets sort of.
        // We treat the quad as if it was actually the same size as the image * imageScale.
        // Then we shrink/expand the center as needed to make up the width
        // and height of the real geometry.

        // Calculate the folds as if the quad was a full
        // imageWidth*imageScale by imageHeight*imageScale
        float iw = imageWidth * imageScale;
        float ih = imageHeight * imageScale;
        this.imageSize = new Vector2f(iw, ih);
        this.horzFolds = new float[] { imageScale * x1, imageScale * x2 };
        this.vertFolds = new float[] { imageScale * y1, imageScale * y2 };

        // Slide the far end necessary to make the proper width
        // and height
        horzFolds[1] += width - iw;
        vertFolds[1] += height - ih;

        this.horzTexCoords = new float[] { 0, (float)x1/imageWidth, (float)x2/imageWidth, 1 };
        this.vertTexCoords = new float[] { 0, (float)y1/imageHeight, (float)y2/imageHeight, 1 };
        refreshGeometry();
    }

    @Override
    public TbtQuad clone() {
        TbtQuad result = (TbtQuad)super.deepClone();
        result.size = size.clone();
        result.imageSize = imageSize.clone();
        result.horzFolds = horzFolds.clone();
        result.vertFolds = vertFolds.clone();
        result.horzTexCoords = horzTexCoords.clone();
        result.vertTexCoords = vertTexCoords.clone();
        return result;
    }

    public Vector2f getSize() {
        return size;
    }

    public void updateSize( float width, float height ) {
        if( size.x == width && size.y == height )
            return;

        // Put back the size adjustment we made in the first place
        horzFolds[1] -= size.x - imageSize.x;
        vertFolds[1] -= size.y - imageSize.y;

        size.set(width, height);

        // Adjust the middle fold for the new size
        horzFolds[1] += size.x - imageSize.x;
        vertFolds[1] += size.y - imageSize.y;
        refreshGeometry();
    }

    protected void refreshGeometry() {
        // Vertexes are arranged as:
        //
        //  9 -- 8 -- 7 -- 6
        //  | \  | /  | /  |
        // 10 --15 --14 -- 5
        //  | /  | /  | /  |
        // 11 --12 --13 -- 4
        //  | /  | /  | \  |
        //  0 -- 1 -- 2 -- 3
        //
        // Note: some of the corners are flipped to better support extrusion
        // if the caller desires to pull up the center quad.

        setBuffer(Type.Index, 3, new short[] {
                                        0, 1, 12,
                                        0, 12, 11,
                                        1, 2, 13,
                                        1, 13, 12,
                                        2, 3, 13,
                                        3, 4, 13,
                                        13, 4, 5,
                                        13, 5, 14,
                                        14, 5, 6,
                                        14, 6, 7,
                                        15, 14, 7,
                                        15, 7, 8,
                                        10, 15, 9,
                                        15, 8, 9,
                                        11, 12, 15,
                                        11, 15, 10,

                                        // The center
                                        12, 13, 14,
                                        12, 14, 15
                                    });

        setBuffer(Type.Position, 3, new float[] {
                                        0, 0, 0,
                                        horzFolds[0], 0, 0,
                                        horzFolds[1], 0, 0,
                                        size.x, 0, 0,
                                        size.x, vertFolds[0], 0,
                                        size.x, vertFolds[1], 0,
                                        size.x, size.y, 0,
                                        horzFolds[1], size.y, 0,
                                        horzFolds[0], size.y, 0,
                                        0, size.y, 0,
                                        0, vertFolds[1], 0,
                                        0, vertFolds[0], 0,

                                        // The center
                                        horzFolds[0], vertFolds[0], 0,
                                        horzFolds[1], vertFolds[0], 0,
                                        horzFolds[1], vertFolds[1], 0,
                                        horzFolds[0], vertFolds[1], 0
                                    });
        setBuffer(Type.TexCoord, 2, new float[] {
                                        horzTexCoords[0], vertTexCoords[0],
                                        horzTexCoords[1], vertTexCoords[0],
                                        horzTexCoords[2], vertTexCoords[0],
                                        horzTexCoords[3], vertTexCoords[0],
                                        horzTexCoords[3], vertTexCoords[1],
                                        horzTexCoords[3], vertTexCoords[2],
                                        horzTexCoords[3], vertTexCoords[3],
                                        horzTexCoords[2], vertTexCoords[3],
                                        horzTexCoords[1], vertTexCoords[3],
                                        horzTexCoords[0], vertTexCoords[3],
                                        horzTexCoords[0], vertTexCoords[2],
                                        horzTexCoords[0], vertTexCoords[1],

                                        // The center
                                        horzTexCoords[1], vertTexCoords[1],
                                        horzTexCoords[2], vertTexCoords[1],
                                        horzTexCoords[2], vertTexCoords[2],
                                        horzTexCoords[1], vertTexCoords[2]
                                    });

        setBuffer(Type.Normal, 3, new float[] {
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,

                                        // The center
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1,
                                        0, 0, 1
                                    });

        updateBound();
    }
}


