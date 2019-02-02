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

package com.simsilica.lemur.component;

import com.jme3.math.Vector3f;
import com.simsilica.lemur.Insets3f;


/**
 *
 *  @author    Paul Speed
 */
public class DynamicInsetsComponent extends InsetsComponent {

    private Vector3f lastPreferredSize;

    public DynamicInsetsComponent( float top, float left, float bottom, float right ) {
        super( new Insets3f(top, left, bottom, right) );
    }

    public DynamicInsetsComponent( float top, float left, float bottom, float right,
                            float front, float back ) {
        super( new Insets3f(top, left, bottom, right, front, back) );
    }

    public DynamicInsetsComponent( Insets3f insets ) {
        super( insets );
    }

    @Override
    public void setInsets( Insets3f insets ) {

        Insets3f balanced = insets.clone();

        // Make sure the insets add to 1.0 in each axis
        for( int i = 0; i < 3; i++ ) {
            float min = balanced.min.get(i);
            float max = balanced.max.get(i);
            //if( min == 0 && max == 0 ) {
            //    // If both are 0 then center... prevents divide by zero
            //    min = 0.5f;
            //    max = 0.5f;
            //}
            float size = min + max;
            float scale = size < 1 ? 1 : (1 / size);
            balanced.min.set(i, min * scale);
            balanced.max.set(i, max * scale);
        }

        super.setInsets( balanced );
    }

    @Override
    public DynamicInsetsComponent clone() {
        DynamicInsetsComponent result = (DynamicInsetsComponent)super.clone();
        result.lastPreferredSize = null;
        return result;
    }

    @Override
    public void calculatePreferredSize( Vector3f size ) {
    
        // Keep track of the preferred size of the rest of
        // the stack up to this point.  We don't add any insets
        // here.
        lastPreferredSize = size.clone();
    }

    @Override
    public void reshape( Vector3f pos, Vector3f size ) {
        Vector3f prefSize = lastPreferredSize;
        if( prefSize == null ) {
        
            // Dynamic insets by its nature is going to be the
            // 'base' component or very close to it.  If we don't have
            // a lastPreferredSize then it means calculatePreferredSize
            // was never called.  This can be the result of our parent
            // already knowing what it's preferred size is.  So if 
            // we are attached we will assume that our parent knows
            // best.  We won't cache the value, though, since it implies
            // that we might be incorrect later.  We'll fetch it every time.
            if( isAttached() ) {
                prefSize = getGuiControl().getPreferredSize(); 
            } else {           
                // There is nothing we can do, so we won't do anything
                return;
            }
        }

        // Otherwise, see what the difference is between our
        // desired size and
        Vector3f delta = size.subtract(prefSize);
        Insets3f insets = getInsets();
        for( int i = 0; i < 3; i++ ) {
            float d = delta.get(i);
            if( d <= 0 ) {
                // We don't go smaller than preferred size so
                // we skip if less than 0 and 0 would be a no-op
                // anyway so we skip then too
                continue;
            }

            float min = insets.min.get(i);
            float max = insets.max.get(i);
            float p = pos.get(i);
            float s = size.get(i);

            if( i == 1 ) {
                // To match regular insets we invert y adjustment
                // so that min is at the top.
                pos.set(i, p - min * d);
            } else {
                pos.set(i, p + min * d);
            }
            
            // Set the size such that min and max are accounted
            // for
            size.set(i, s - (min * d + max * d));
        }
    }

}

