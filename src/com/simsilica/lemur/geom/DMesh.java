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

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import java.nio.FloatBuffer;


/**
 *  A mesh that wraps another mesh to provide a deformed
 *  view using a Deformation function.
 *
 *  @author    Paul Speed
 */
public class DMesh extends Mesh {

    private Mesh mesh;
    private Deformation deform;

    public DMesh( Mesh mesh ) {
        this.mesh = mesh;
    }

    public DMesh( Mesh mesh, Deformation deform ) {
        this.mesh = mesh;
        this.deform = deform;
        updateMesh();
    }

    protected VertexBuffer matchBuffer( VertexBuffer source ) {
        if( source == null )
            return null;

        VertexBuffer target = getBuffer(source.getBufferType());
        if( target == null || target.getData().capacity() < source.getData().limit() ) {
            target = source.clone();
            setBuffer(target);
        } else {
            target.getData().limit(source.getData().limit());
        }
        return target;
    }

    public void setDeformation( Deformation deform ) {
        this.deform = deform;
        updateMesh();
    }

    public Deformation getDeformation() {
        return deform;
    }

    public void updateMesh() {
        VertexBuffer sourcePos = mesh.getBuffer(Type.Position);
        VertexBuffer sourceNorms = mesh.getBuffer(Type.Normal);

        VertexBuffer targetPos = matchBuffer(sourcePos);
        VertexBuffer targetNorms = matchBuffer(sourceNorms);

        // Make sure we also have an index and texture buffer that matches
        // ...even though we don't transform them we still need copies of
        // them.  We could just reference them but then our other buffers
        // might get out of sync
        matchBuffer(mesh.getBuffer(Type.Index));
        matchBuffer(mesh.getBuffer(Type.TexCoord));

        morph(sourcePos, sourceNorms, targetPos, targetNorms);
        updateBound();
    }

    protected void morph( VertexBuffer sourcePos, VertexBuffer sourceNorms,
                          VertexBuffer targetPos, VertexBuffer targetNorms ) {
        FloatBuffer sp = (FloatBuffer)sourcePos.getData();
        sp.rewind();

        FloatBuffer sn = (FloatBuffer)sourceNorms.getData();
        sn.rewind();

        FloatBuffer tp = (FloatBuffer)targetPos.getData();
        tp.rewind();

        FloatBuffer tn = (FloatBuffer)targetNorms.getData();
        tn.rewind();

        morph(sp, sn, tp, tn);

        sp.rewind();
        sn.rewind();

        tp.rewind();
        targetPos.updateData(tp);
        tn.rewind();
        targetNorms.updateData(tn);
    }

    protected void morph( FloatBuffer sourcePos, FloatBuffer sourceNorms,
                          FloatBuffer targetPos, FloatBuffer targetNorms ) {
        if( deform == null )
            return;

        int count = sourcePos.limit() / 3;
        Vector3f v = new Vector3f();
        Vector3f normal = new Vector3f();

        for( int i = 0; i < count; i++ ) {
            v.x = sourcePos.get();
            v.y = sourcePos.get();
            v.z = sourcePos.get();
            normal.x = sourceNorms.get();
            normal.y = sourceNorms.get();
            normal.z = sourceNorms.get();

            morphVertex(v, normal);

            targetPos.put(v.x).put(v.y).put(v.z);
            targetNorms.put(normal.x).put(normal.y).put(normal.z);
        }
    }

    protected void morphVertex( Vector3f vert, Vector3f normal ) {
        deform.deform(vert, normal);
    }
}


