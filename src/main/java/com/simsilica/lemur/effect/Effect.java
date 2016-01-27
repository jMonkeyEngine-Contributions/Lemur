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

package com.simsilica.lemur.effect;

import com.simsilica.lemur.anim.Animation;


/**
 *  Represents a sort of 'factory' that can create animations for
 *  a particular target.  An Effect in an EffectControl can be connected 
 *  with a particular channel and when run can receive information about 
 *  any last-run or currently running effects on that channel.
 *
 *  <p>The Effect object is basically a stylable animation that can
 *  be applied to GUI elements.  Many GUI elements can share the same
 *  Effect object as it's only a factory that creates the animation
 *  objects when the effect is run.</p>  
 *
 *  @author    Paul Speed
 */
public interface Effect<T> {
    
    /**
     *  Returns the channel name that will be used for looking up
     *  existing animations for a given target.  Effects that are on the 'null' 
     *  channel are not tracked in this way.  Otherwise, when the effect is 
     *  run, the channel is used to provide information to the effect about 
     *  any previous effects that were run.  This allows the new effect
     *  to adjust its own animations accordingly, either reducing time,
     *  starting from a different point, or entirely custom behavior depending
     *  on the needs of the effect. 
     */   
    public String getChannel();
    
    /**
     *  Creates a new animation task that will replace any existing
     *  animation task for this Effect's channel.  The last run
     *  animation is passed as the 'existing' parameter that this
     *  factory method can use to see if the previous one is still
     *  running and adjust accordingly.  (For example, a close window
     *  animation might do something different if the open window 
     *  animation hadn't completed yet.)  By default, if the caller requests it
     *  when running the effect, the EffectControl will attempt to manage this by fast
     *  forwarding the new animation to catch up to what was left of the old animation.
     */
    public Animation create( T target, EffectInfo existing );
}


