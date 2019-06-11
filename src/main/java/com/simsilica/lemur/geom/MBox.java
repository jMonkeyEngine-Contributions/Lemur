/*
 * $Id$
 *
 * Copyright (c) 2012-2019 jMonkeyEngine
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

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


/**
 *  A mesh implementation that creates a box with a specified
 *  number of slices, masking off one or more sides.  Separate
 *  slice counts can be provided for x,y,z axes and any or all
 *  of the sides can be turns on or off.  In other words, this
 *  can be used directly as a Box implementation with more
 *  triangles (subdivided) or it can be used as a subdivided
 *  Quad if only one side it specified.
 *
 *  @author    Paul Speed
 */
public class MBox extends Mesh implements Savable, Cloneable {

    public static final int TOP_MASK = 0x1;
    public static final int BOTTOM_MASK = 0x2;
    public static final int FRONT_MASK = 0x4;
    public static final int BACK_MASK = 0x8;
    public static final int LEFT_MASK = 0x10;
    public static final int RIGHT_MASK = 0x20;
    public static final int ALL_MASK = 0x3f;

    private static final int TOP = 0;
    private static final int BOTTOM = 1;
    private static final int FRONT = 2;
    private static final int BACK = 3;
    private static final int LEFT = 4;
    private static final int RIGHT = 5;

    private static Vector3f normals[] = {
            new Vector3f(0,1,0),
            new Vector3f(0,-1,0),
            new Vector3f(0,0,1),
            new Vector3f(0,0,-1),
            new Vector3f(-1,0,0),
            new Vector3f(1,0,0)
        };
    private static Vector3f tangents[] = {
            new Vector3f(1,0,0),
            new Vector3f(1,0,0),
            new Vector3f(1,0,0),
            new Vector3f(-1,0,0),
            new Vector3f(0,0,1),
            new Vector3f(0,0,-1)
        };
    private static Vector3f binormals[] = {
            new Vector3f(0,0,-1),
            new Vector3f(0,0,1),
            new Vector3f(0,1,0),
            new Vector3f(0,1,0),
            new Vector3f(0,1,0),
            new Vector3f(0,1,0)
        };

    private Vector3f extents = new Vector3f();
    private int[] slices = new int[3];
    private int sideMask;
    
    /**
     * Serialization only. Do not use.
     */
    public MBox() {
        super();
    }

    public MBox( float xExtent, float yExtent, float zExtent,
                 int xSlices, int ySlices, int zSlices ) {
        this(xExtent, yExtent, zExtent, xSlices, ySlices, zSlices, ALL_MASK);
    }

    public MBox( float xExtent, float yExtent, float zExtent,
                 int xSlices, int ySlices, int zSlices,
                 int sideMask ) {
        this.sideMask = sideMask;
        extents.set(xExtent, yExtent, zExtent);
        slices[0] = xSlices;
        slices[1] = ySlices;
        slices[2] = zSlices;

        refreshGeometry();
    }
    
    public Vector3f getExtents() {
        return extents;
    }
    
    public void resize( Vector3f extents ) {
        this.extents.set(extents);
        refreshGeometry();
    }

    @Override
    public MBox clone() {
        MBox result = (MBox)super.deepClone();
        result.extents = extents.clone();
        result.slices = slices.clone();
        return result;
    }

    protected void refreshGeometry() {
        // The nunmber of quads along a side is 1
        // plus the number of "slices"... or splits.
        // A box with 0 slices is just a regular 6 quad box.
        // A box with 1 slice all around has four quads per side, etc.
        //
        // Vertex count is quads + 1
        // Total number of vertexes if all sides are on is:
        // top/bottom = (xSlices + 2) * (zSlices + 2) * 2
        // front/back = (xSlices + 2) * (ySlices + 2) * 2
        // left/right = (zSlices + 2) * (ySlices + 2) * 2

        int xVertCount = slices[0] + 2;
        int yVertCount = slices[1] + 2;
        int zVertCount = slices[2] + 2;
        int xQuadCount = slices[0] + 1;
        int yQuadCount = slices[1] + 1;
        int zQuadCount = slices[2] + 1;

        int upVertCount = xVertCount * zVertCount;
        int frontVertCount = xVertCount * yVertCount;
        int sideVertCount = zVertCount * yVertCount;
        int upTriCount = xQuadCount * zQuadCount * 2;
        int frontTriCount = xQuadCount * yQuadCount * 2;
        int sideTriCount = zQuadCount * yQuadCount * 2;

        int vertCount = 0;
        int triCount = 0;

        if( (sideMask & TOP_MASK) != 0 ) {
            vertCount += upVertCount;
            triCount += upTriCount;
        }
        if( (sideMask & BOTTOM_MASK) != 0 ) {
            vertCount += upVertCount;
            triCount += upTriCount;
        }
        if( (sideMask & FRONT_MASK) != 0 ) {
            vertCount += frontVertCount;
            triCount += frontTriCount;
        }
        if( (sideMask & BACK_MASK) != 0 ) {
            vertCount += frontVertCount;
            triCount += frontTriCount;
        }
        if( (sideMask & LEFT_MASK) != 0 ) {
            vertCount += sideVertCount;
            triCount += sideTriCount;
        }
        if( (sideMask & RIGHT_MASK) != 0 ) {
            vertCount += sideVertCount;
            triCount += sideTriCount;
        }

        FloatBuffer verts = BufferUtils.createFloatBuffer(vertCount * 3);
        FloatBuffer norms = BufferUtils.createFloatBuffer(vertCount * 3);
        FloatBuffer texes = BufferUtils.createFloatBuffer(vertCount * 2);
        ShortBuffer index = BufferUtils.createShortBuffer(triCount * 3);

        int lastIndex = 0;
        if( (sideMask & TOP_MASK) != 0 ) {
            lastIndex = fillSide(lastIndex, TOP, 0, xVertCount, 2, zVertCount, 1,
                                 verts, norms, texes, index);
        }
        if( (sideMask & BOTTOM_MASK) != 0 ) {
            lastIndex = fillSide(lastIndex, BOTTOM, 0, xVertCount, 2, zVertCount, 1,
                                 verts, norms, texes, index);
        }
        if( (sideMask & FRONT_MASK) != 0 ) {
            lastIndex = fillSide(lastIndex, FRONT, 0, xVertCount, 1, yVertCount, 2,
                                 verts, norms, texes, index);
        }
        if( (sideMask & BACK_MASK) != 0 ) {
            lastIndex = fillSide(lastIndex, BACK, 0, xVertCount, 1, yVertCount, 2,
                                 verts, norms, texes, index);
        }
        if( (sideMask & LEFT_MASK) != 0 ) {
            lastIndex = fillSide(lastIndex, LEFT, 2, zVertCount, 1, yVertCount, 0,
                                  verts, norms, texes, index);
        }
        if( (sideMask & RIGHT_MASK) != 0 ) {
            lastIndex = fillSide(lastIndex, RIGHT, 2, zVertCount, 1, yVertCount, 0,
                                 verts, norms, texes, index);
        }

        index.flip();
        norms.flip();
        verts.flip();
        texes.flip();

        setBuffer(Type.Index, 3, index);

        setBuffer(Type.Position, 3, verts);
        setBuffer(Type.TexCoord, 2, texes);

        setBuffer(Type.Normal, 3, norms);

        updateBound();
        clearCollisionData();

    }

    protected float[] spread( float min, float max, int count ) {
        float[] result = new float[count];
        float delta = (max - min);
        float step = 1.0f / (count - 1); // 2 verts is 1 step

        for( int i = 0; i < count; i++ ) {
            result[i] = min + (delta * i * step);
        }
        return result;
    }

    protected int fillSide( int lastIndex, int side,
                            int colAxis, int colCount,
                            int rowAxis, int rowCount, int otherAxis,
                            FloatBuffer verts, FloatBuffer norms,
                            FloatBuffer texes, ShortBuffer index )
    {
        // Waste some space because I don't care too much
        Vector3f normal = normals[side];
        Vector3f tangent = tangents[side];
        Vector3f binormal = binormals[side];

        float rowMin = binormal.get(rowAxis) * -extents.get(rowAxis);
        float rowMax = binormal.get(rowAxis) * extents.get(rowAxis);
        float colMin = tangent.get(colAxis) * -extents.get(colAxis);
        float colMax = tangent.get(colAxis) * extents.get(colAxis);

        float[] rowVals = spread(rowMin, rowMax, rowCount);
        float[] colVals = spread(colMin, colMax, colCount);

        Vector3f pos = new Vector3f();
        pos.set(otherAxis, normal.get(otherAxis) * extents.get(otherAxis));

        int lastBaseIndex = 0;
        for( int j = 0; j < rowCount; j++ ) {
            pos.set(rowAxis, rowVals[j]);

            int baseIndex = lastIndex + j * colCount;

            for( int i = 0; i < colCount; i++ ) {
                pos.set(colAxis, colVals[i]);

                verts.put(pos.x);
                verts.put(pos.y);
                verts.put(pos.z);

                norms.put(normal.x);
                norms.put(normal.y);
                norms.put(normal.z);

                texes.put((float)i/(colCount-1));
                texes.put((float)j/(rowCount-1));

                if( j > 0 && i < colCount - 1 ) {
                    // From the second row on, we can emit indexes
                    // 2---3   baseIndex+
                    // | / |
                    // 0---1   lastBaseIndex+
                    //
                    // 0, 1, 3 ... which is really lbi, lbi+, bi+1
                    // 0, 3, 2 ... lbi, bi+1, bi
                    //
                    index.put((short)lastBaseIndex);
                    index.put((short)(lastBaseIndex + 1));
                    index.put((short)(baseIndex + 1));
                    index.put((short)lastBaseIndex);
                    index.put((short)(baseIndex + 1));
                    index.put((short)baseIndex);

                    baseIndex++;
                    lastBaseIndex++;
                }
            }

            lastBaseIndex = lastIndex + j * colCount;
        }

        return lastIndex + colCount * rowCount;
    }
    
    @Override
    public void read(JmeImporter e) throws IOException {
        super.read(e);
        InputCapsule ic = e.getCapsule(this);
        extents = (Vector3f) ic.readSavable("extents", null);
        slices = ic.readIntArray("slices", null);
        sideMask = ic.readInt("sideMask", 0);
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        super.write(e);
        OutputCapsule oc = e.getCapsule(this);
        oc.write(extents, "extents", null);
        oc.write(slices, "slices", null);
        oc.write(sideMask, "sideMask", 0);
    }
}
   
