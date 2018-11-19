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

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.TweenAnimation;
import java.util.HashMap;
import java.util.Map;


/**
 *  Manages the available named Effects for a Spatial and keeps track of
 *  the existing effects run for a particular channel.  Callers can run
 *  any of the configured effects by name.
 *
 *  @author    Paul Speed
 */
public class EffectControl<T extends Spatial> extends AbstractControl {

    private AnimationState state;
    private final Map<String, Effect<? super T>> effects = new HashMap<String, Effect<? super T>>();
    private final Map<String, EffectInfo> channels = new HashMap<String, EffectInfo>();
 
    /**
     *  Creates an effect control that will use the specified AnimationState
     *  for running its effects.  Normally an application will only have
     *  one AnimationState but it's possible to have multiple states depending
     *  on need.  For example, the UI might be run by one AnimationState while
     *  certain in-game controls run by a different AnimationState so that they
     *  can be paused without affecting the UI.
     */   
    public EffectControl( AnimationState anim ) {
        this.state = anim;
    }

    /**
     *  Creates an effect control that will run animations using the default
     *  animation state returned by AnimationState.getDefaultInstance().
     */   
    public EffectControl() {
        this(null);
    }
 
    /**
     *  Type-parameter safe version of getSpatial().
     */   
    @SuppressWarnings("unchecked")
    public T getSpatial() {
        return (T)super.getSpatial();
    }
    
    protected AnimationState anim() {
        if( state == null ) {
            state = AnimationState.getDefaultInstance();
        }
        return state;
    }
    
    public void addEffect( String name, Effect<? super T> effect ) {
        effects.put(name, effect);
    }
 
    public boolean hasEffect( String name ) {
        return effects.containsKey(name);
    }
 
    public Effect<? super T> removeEffect( String name ) {
        return effects.remove(name);       
    }
 
    public Map<String, Effect<? super T>> getEffects() {
        return effects;
    }
 
    public EffectInfo runEffect( String name ) {
        return runEffect(name, true);
    }
    
    public EffectInfo runEffect( String name, boolean fastForward ) {
        if( spatial == null ) {
            return null;
        }
        
        Effect<? super T> e = effects.get(name);
        if( e == null ) {
            return null;
        }
 
        String channel = e.getChannel();       
        EffectInfo existing = null;
        if( channel != null ) {
            existing = channels.remove(channel);
        }
        
        Animation a = e.create(getSpatial(), existing);
 
        // If we want to fast forward and we are a different effect       
        if( fastForward && existing != null && e != existing.getEffect() ) {
            // Then see if these are animations that we can use to fast forward
            if( a instanceof TweenAnimation && existing.getAnimation() instanceof TweenAnimation ) {
                TweenAnimation aFrom = (TweenAnimation)existing.getAnimation();
                TweenAnimation aTo = (TweenAnimation)a;
                aTo.fastForwardPercent(aFrom.getPercentRemaining());
            }
        }
        
        // If there was an existing channel animation the always cancel it         
        if( existing != null ) {
            anim().cancel(existing.getAnimation());
        }
 
        EffectInfo result = null;
 
        if( a != null ) {
            anim().add(a);
            
            // Always create a result even if we won't put it in the channel.
            // This gives us something useful to return to the caller about
            // what is being animated.
            result = new EffectInfo(name, e, a);
            
            if( channel != null ) {
                // Keep track of it for later
                channels.put(channel, result);
            }            
        }
        return result;
    } 

    @Override
    protected void controlUpdate( float tpf ) {
    }

    @Override
    protected void controlRender( RenderManager rm, ViewPort vp ) {
    }
}
