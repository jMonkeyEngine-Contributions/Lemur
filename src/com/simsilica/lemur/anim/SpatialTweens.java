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
import com.jme3.scene.Spatial;


/**
 *  Static utility methods for creating common Spatial-related Tween objects.
 *
 *  @author    Paul Speed
 */
public class SpatialTweens {

    /**
     *  Creates a tween that will interpolate the location of the specified target 
     *  from one location to another.  If either location is null then they will 
     *  be substituted with the Spatial's current local translation AT THE TIME OF THIS CALL.
     *  This method will use the distance between the two locations as the 
     *  tween length.  This makes it easier to create a sequence of movements
     *  that all move at the same speed.  The overall sequence can always be rescaled
     *  to fit whatever outer time constraints required. 
     */
    public static Tween move( Spatial target, Vector3f from, Vector3f to ) {
        from = from != null ? from : target.getLocalTranslation();
        to = to != null ? to : target.getLocalTranslation();
        return new MoveSpatial(target, from, to);
    }
    
    /**
     *  Creates a tween that will interpolate the location of the specified target 
     *  from one location to another.  If either location is null then they will 
     *  be substituted with the Spatial's current local translation AT THE TIME OF THIS CALL.
     */
    public static Tween move( Spatial target, Vector3f from, Vector3f to, double length ) {
        from = from != null ? from : target.getLocalTranslation();
        to = to != null ? to : target.getLocalTranslation();
        return new MoveSpatial(target, from, to, length);
    }
    
    /**
     *  Creates a tween that will interpolate the rotation of the specified target 
     *  from one rotation to another.  If either rotation is null then they will 
     *  be substituted with the Spatial's current local rotation AT THE TIME OF THIS CALL.
     */
    public static Tween rotate( Spatial target, Quaternion from, Quaternion to, double length ) {
        from = from != null ? from : target.getLocalRotation();
        to = to != null ? to : target.getLocalRotation();
        return new RotateSpatial(target, from, to, length);
    }
    
    /**
     *  Creates a tween that will interpolate the overall scale of the specified target 
     *  from one scale to another.  
     */
    public static Tween scale( Spatial target, float from, float to, double length ) {
        return scale(target, new Vector3f(from, from, from), new Vector3f(to, to, to), length);
    }
    
    /**
     *  Creates a tween that will interpolate the scale of the specified target 
     *  from one scale to another.  If either scale is null then they will 
     *  be substituted with the Spatial's current local scale AT THE TIME OF THIS CALL.
     */
    public static Tween scale( Spatial target, Vector3f from, Vector3f to, double length ) {
        from = from != null ? from : target.getLocalScale();
        to = to != null ? to : target.getLocalScale();
        return new ScaleSpatial(target, from, to, length);
    }
 
    /**
     *  Creates a Tween that will detach the specified spatial when executed
     *  with any value of t greater than or equal to 0.
     *  (Note: internally this just calls Tweens.callMethod().)
     */   
    public static Tween detach( Spatial target ) {
        return Tweens.callMethod(target, "removeFromParent");
    }
    
    /**
     *  Creates a Tween that will attach the specified spatial to the specified
     *  parent when executed with any value of t greater than or equal to 0.
     *  (Note: internally this just calls Tweens.callMethod().)
     */   
    public static Tween attach( Spatial target, Spatial parent ) {
        return Tweens.callMethod(parent, "attachChild", target);
    }
    
    private static class MoveSpatial extends AbstractTween {

        private final Spatial target;
        private final Vector3f from;
        private final Vector3f to;
        private final Vector3f value;

        public MoveSpatial( Spatial target, Vector3f from, Vector3f to ) {
            this(target, from, to, to.distance(from));
        }
        
        public MoveSpatial( Spatial target, Vector3f from, Vector3f to, double length ) {
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
            target.setLocalTranslation(value);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[target=" + target + ", from=" + from + ", to=" + to + ", length=" + getLength() + "]";
        }
    }
    
    private static class RotateSpatial extends AbstractTween {

        private final Spatial target;
        private final Quaternion from;
        private final Quaternion to;
        private final Quaternion value;

        public RotateSpatial( Spatial target, Quaternion from, Quaternion to, double length ) {
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
            target.setLocalRotation(value);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[target=" + target + ", from=" + from + ", to=" + to + ", length=" + getLength() + "]";
        }
    }
    
    private static class ScaleSpatial extends AbstractTween {

        private final Spatial target;
        private final Vector3f from;
        private final Vector3f to;
        private final Vector3f value;

        public ScaleSpatial( Spatial target, Vector3f from, Vector3f to, double length ) {
            super(length);
            this.target = target;
            this.from = from.clone();
            this.to = to.clone();
            this.value = from.clone();
        }

        @Override
        protected void doInterpolate( double t ) {
            // Interpolate
            value.interpolateLocal(from, to, (float)t);
            target.setLocalScale(value);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[target=" + target + ", from=" + from + ", to=" + to + ", length=" + getLength() + "]";
        }
    }
}

