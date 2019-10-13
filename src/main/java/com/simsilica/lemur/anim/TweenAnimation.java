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


/**
 *  An Animation implementation that tracks execution time and
 *  calls a delegate Tween object once per frame.  The animation
 *  can be fast-forwarded and the current exeuction time can be
 *  queried in various ways.
 *
 *  @author    Paul Speed
 */
public class TweenAnimation implements Animation {

    private Tween delegate;
    private double t = -1;
    private boolean running = true;
    private boolean loop;
    
    public TweenAnimation( Tween... delegates ) {
        this(false, delegates);
    }
    
    public TweenAnimation( boolean loop, Tween... delegates ) {
        this.delegate = Tweens.sequence(delegates);
        this.loop = loop;
    }

    /**
     *  Returns true if this is a looping animation.
     */
    public boolean isLooping() {
        return loop;
    }

    /**
     *  Returns true if the animation is currently running.
     */
    public boolean isRunning() {
        return running && (loop || t >= 0);
    }
 
    /**
     *  Returns the total duration for this animation or the time
     *  of a single loop iteration if looping. 
     */   
    public double getLength() {
        return delegate.getLength();
    }

    /**
     *  Returns the current execution time for this animation, ie:
     *  how far it has been run.
     */
    public double getTime() {
        return t;
    }

    /**
     *  Returns the remaining time left for this animation.  If the animation
     *  is looping then this returns the remaining time for the current loop
     *  iteration.
     */
    public double getRemaining() {
        if( t < 0 ) {
            return delegate.getLength();
        }
        return Math.max(0, delegate.getLength() - t);
    }

    /**
     *  Returns the remaining time as a scaled value between 0 and 1.0.
     */
    public double getPercentRemaining() {
        return getRemaining() / delegate.getLength();
    }

    /**
     *  Fast-forwards the animation to the specified time as a
     *  value between 0 and 1.0 that will be scaled to the animation
     *  duration.  
     */
    public void fastForwardPercent( double t ) { 
        fastForward(t * delegate.getLength());
    }

    /**
     *  Fast-forwards the animation to the specified time value.
     */
    public void fastForward( double t ) {
        if( t < 0 ) {
            return;
        }
        if( t < this.t ) {
            // No need to fast-forward as the animation is already
            // past the specified time.
            return;
        }
        
        if( this.t < 0 ) {
            // This animation hasn't been executed before.        
            // Get the initial one in to signal first frame
            animate(0);
        }
        
        // And then skip ahead
        animate(t);
    }

    /**
     *  Called by the AnimationState to execute this animation.  Generally
     *  user-code should not call this directly.
     */
    @Override
    public boolean animate( double tpf ) {
        if( !running ) {
            return false;
        }
        if( t < 0 ) {
            // First frame
            t = 0;
        } else {
            t += tpf;
        }
        if( loop ) {
            if( !delegate.interpolate(t) ) {       
                t = t - delegate.getLength();
                if( t > 0 ) {
                    delegate.interpolate(t);
                } 
            }
        } else {
            running = delegate.interpolate(t); 
        } 
        return running;
    }

    /**
     *  Called by the AnimationState when this animation is canceled.  Generally
     *  user-code should not call this directly.
     */
    @Override
    public void cancel() {
        running = false;
    }
}
