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

package com.simsilica.lemur.anim;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;


/**
 *  Static utility methods for creating common Camera-related Tween objects.
 *
 *  @author    Paul Speed
 */
public class CameraTweens {

    /**
     *  Creates a tween that will interpolate the location of the specified camera 
     *  from one location to another.  If either location is null then they will 
     *  be substituted with the Camera's current local translation AT THE TIME OF THIS CALL.
     *  This method will use the distance between the two locations as the 
     *  tween length.  This makes it easier to create a sequence of movements
     *  that all move at the same speed.  The overall sequence can always be rescaled
     *  to fit whatever outer time constraints required. 
     */
    public static Tween move( Camera target, Vector3f from, Vector3f to ) {
        from = from != null ? from : target.getLocation();
        to = to != null ? to : target.getLocation();
        return new MoveCamera(target, from, to);
    }
    
    /**
     *  Creates a tween that will interpolate the location of the specified target 
     *  from one location to another.  If either location is null then they will 
     *  be substituted with the Camera's current local translation AT THE TIME OF THIS CALL.
     */
    public static Tween move( Camera target, Vector3f from, Vector3f to, double length ) {
        from = from != null ? from : target.getLocation();
        to = to != null ? to : target.getLocation();
        return new MoveCamera(target, from, to, length);
    }
    
    /**
     *  Creates a tween that will interpolate the rotation of the specified target 
     *  from one rotation to another.  If either rotation is null then they will 
     *  be substituted with the Camera's current local rotation AT THE TIME OF THIS CALL.
     */
    public static Tween rotate( Camera target, Quaternion from, Quaternion to, double length ) {
        from = from != null ? from : target.getRotation();
        to = to != null ? to : target.getRotation();
        return new RotateCamera(target, from, to, length);
    }    

    private static class MoveCamera extends AbstractTween {

        private final Camera target;
        private final Vector3f from;
        private final Vector3f to;
        private final Vector3f value;

        public MoveCamera( Camera target, Vector3f from, Vector3f to ) {
            this(target, from, to, to.distance(from));
        }
        
        public MoveCamera( Camera target, Vector3f from, Vector3f to, double length ) {
            super(length);
            this.target = target;
            this.from = from.clone();
            this.to = to.clone();
            this.value = new Vector3f(from);
        }

        @Override
        protected void doInterpolate( double t ) {
            // Interpolate
            value.interpolateLocal(from, to, (float)t);
            target.setLocation(value);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[target=" + target + ", from=" + from + ", to=" + to + ", length=" + getLength() + "]";
        }
    }
    
    private static class RotateCamera extends AbstractTween {

        private final Camera target;
        private final Quaternion from;
        private final Quaternion to;
        private final Quaternion value;

        public RotateCamera( Camera target, Quaternion from, Quaternion to, double length ) {
            super(length);
            this.target = target;
            this.from = from.clone();
            this.to = to.clone();
            this.value = from.clone();
        }

        @Override
        protected void doInterpolate( double t ) {
            // Interpolate
            value.slerp(from, to, (float)t);
            target.setRotation(value);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[target=" + target + ", from=" + from + ", to=" + to + ", length=" + getLength() + "]";
        }
    }

}
