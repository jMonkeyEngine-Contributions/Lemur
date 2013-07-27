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

/**
 *
 *  @author    Paul Speed
 */
public class Deformations {

    public static Ramp ramp( int majorAxis, int minorAxis, float scale ) {
        return new Ramp(majorAxis, minorAxis, scale);
    }

    public static Cylindrical cylindrical( int majorAxis, int minorAxis,
                                           Vector3f origin, float radius,
                                           float start, float limit ) {
        return new Cylindrical(majorAxis, minorAxis, origin, radius, start, limit);
    }

    public static class Ramp implements Deformation {

        private int majorAxis;
        private int minorAxis;
        private float scale;

        public Ramp( int majorAxis, int minorAxis, float scale )
        {
            this.majorAxis = majorAxis;
            this.minorAxis = minorAxis;
            this.scale = scale;
        }

        public void deform( Vector3f vert, Vector3f normal )
        {
            float major = vert.get(majorAxis);
            float minor = vert.get(minorAxis);
            minor += major * scale;
            vert.set(minorAxis, minor);

            // A normal gets weird here.  We can map it to the
            // new slope and ramp normal but then sideways
            // pointing normals will go funny.  The more "up"
            // a normal points the more directly we want to
            // map it.  If it points sideways then we don't
            // want to map it at all.
        }
    }

    public static class Cylindrical implements Deformation {

        private Vector3f origin;
        private float radius;
        private int majorAxis;
        private int minorAxis;
        private float start;
        private float limit;

        public Cylindrical( int majorAxis, int minorAxis, Vector3f origin, float radius,
                            float start, float limit ) {
            this.majorAxis = majorAxis;
            this.minorAxis = minorAxis;
            this.origin = origin;
            this.radius = radius;
            this.start = start;
            this.limit = limit;
        }

        public void setOrigin( Vector3f origin ) {
            this.origin = origin;
        }

        public Vector3f getOrigin() {
            return origin;
        }

        public void setRadius( float radius ) {
            this.radius = radius;
        }

        public float getRadius() {
            return radius;
        }

        public void setStart( float start ) {
            this.start = start;
        }

        public float getStart() {
            return start;
        }

        public void setLimit( float limit ) {
            this.limit = limit;
        }

        public float getLimit() {
            return limit;
        }

        public void deform( Vector3f vert, Vector3f normal ) {
            // Y will correspond to the perimeter of the circle
            // so that cos() and sin() make sense.
            float x = vert.get(minorAxis) - origin.get(minorAxis);
            float base = Math.min(origin.get(majorAxis), start);
            float y = vert.get(majorAxis) - base;
            if( y < 0 )
                return;

            float projection = 0;
            if( y > limit ) {
                projection = y - limit;
                y = limit;
            }

            float rads = y / radius; //Math.abs(x); //radius;

            // When x is negative, we are actually on the
            // back side of the cylinder and normal projection
            // isn't really correct.
            if( x < 0 ) {
                rads = FastMath.PI - rads;
            }

            float xd = (float)Math.cos(rads);
            float yd = (float)Math.sin(rads);
            float r = Math.abs(x);

            vert.set(minorAxis, origin.get(minorAxis) + xd * r);
            vert.set(majorAxis, base + yd * r);

            // Now we need to fix the normal, too.
            // xd, yd sort of form a new x-axis...
            // so -yd, xd is sort of a new up axis.
            // We can project our normal into this new 'tangent space'.
            float xRight = xd;
            float yRight = yd;
            float xUp = -yd;
            float yUp = xd;

            // If x was on the back side of the circle then the
            // normals will be 180 degrees off.  Even though we've
            // corrected the angle they'll still be projected backwards
            // if we don't flip that axis
            if( x < 0 ) {
                xRight *= -1;
                yRight *= -1;
                xUp *= -1;
                yUp *= -1;
            }


            float nx = xUp * normal.get(majorAxis);
            float ny = yUp * normal.get(majorAxis);
            nx += xRight * normal.get(minorAxis);
            ny += yRight * normal.get(minorAxis);
            normal.set(minorAxis, nx);
            normal.set(majorAxis, ny);

            if( projection > 0 ) {
                // Need to project out the vertex beyond what limit
                // limited.  We can use our normal axes from above
                float vx = vert.get(minorAxis) + xUp * projection;
                float vy = vert.get(majorAxis) + yUp * projection;
                vert.set(minorAxis, vx);
                vert.set(majorAxis, vy);
            }
        }

    }
}


