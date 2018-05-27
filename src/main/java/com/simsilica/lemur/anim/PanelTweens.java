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

import com.jme3.math.FastMath;
import com.simsilica.lemur.Panel;


/**
 *  Static utility methods for creating common Lemur Panel-specific Tween objects.
 *
 *  @author    Paul Speed
 */
public class PanelTweens {

    /**
     *  Returns a tween object that will interpolate the alpha value of a panel
     *  between to supplied values.  If either alpha value is null then they will 
     *  be substituted with the Panel's current alpha value AT THE TIME OF THIS CALL. 
     */
    public static Tween fade( Panel target, Float fromAlpha, Float toAlpha, double length ) {
        if( fromAlpha == null ) {
            fromAlpha = target.getAlpha();
        }
        if( toAlpha == null ) {
            toAlpha = target.getAlpha();
        }
        return new Fade(target, fromAlpha, toAlpha, length);   
    }
 
    private static class Fade extends AbstractTween {

        private final Panel target;
        private final float from;
        private final float to;
        
        public Fade( Panel target, float from, float to, double length ) {
            super(length);
            this.target = target;
            this.from = from;
            this.to = to;
        }
        
        @Override
        protected void doInterpolate( double t ) {
            float value = FastMath.interpolateLinear((float)t, from, to);
            target.setAlpha(value);
        }
    }  
}
