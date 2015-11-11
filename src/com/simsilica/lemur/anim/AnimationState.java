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

import java.util.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;


/**
 *  Manages a list of Animation tasks, calling them each once
 *  per frame until done or canceled.
 *
 *  @author    Paul Speed
 */
public class AnimationState extends BaseAppState {

    public static final double NANOS_TO_SECONDS = 1 / 1000000000.0;

    private static AnimationState defaultInstance;

    private final List<Animation> tasks = new ArrayList<Animation>();
    private Animation[] array = null;
    
    private long lastTime;
    
    public AnimationState() {
        if( defaultInstance == null ) {
            defaultInstance = this;
        }
    }
 
    /**
     *  Returns the default animation state instance.
     */   
    public static AnimationState getDefaultInstance() {
        return defaultInstance;
    }
 
    /**
     *  Returns true if the specified animation object is
     *  currently running, ie: will be executed this frame.
     */   
    public boolean isRunning( Animation anim ) {
        return tasks.contains(anim);
    }

    /**
     *  Begins executing the specified animation.  The passed
     *  animation is returned directly to the caller.
     */
    public <T extends Animation> T add( T anim ) {
        tasks.add(anim);
        array = null;
        return anim;
    }
 
    /**
     *  Creates a TweenAnimation from the specified tween or 
     *  tweens.  If more than one Tween is passed then they are wrapped
     *  in a sequence.
     */   
    public TweenAnimation add( Tween... sequence ) {
        TweenAnimation anim = new TweenAnimation(sequence);
        return add(anim);
    }
 
    /**
     *  Cancels a currently running animation.
     */   
    public void cancel( Animation anim ) {
        anim.cancel();
        remove(anim);
    }

    private Animation[] getArray() {
        if( array == null ) {
            array = new Animation[tasks.size()];
            array = tasks.toArray(array);
        }
        return array;
    }

    protected void remove( Animation anim ) {
        tasks.remove(anim);
        array = null;
    }

    @Override
    protected void initialize( Application app ) {
    }

    @Override
    protected void cleanup( Application app ) {
    
        // Seems prudent to cancel all of them and let
        // any cleanup get done that is required
        for( Animation a : getArray() ) {
            cancel(a);   
        }
        
        // See if there is another one after we've been removed
        defaultInstance = getState(AnimationState.class);
    }

    @Override
    protected void onEnable() {
        lastTime = System.nanoTime();
    }

    @Override
    public void update( float tpf ) {
        long time = System.nanoTime();
        long delta = time - lastTime;
        double t = delta * NANOS_TO_SECONDS;
        lastTime = time;
        
        for( Animation a : getArray() ) {
            if( !a.animate(t) ) {
                remove(a);
            }
        }          
    }

    @Override
    protected void onDisable() {
    }
}
