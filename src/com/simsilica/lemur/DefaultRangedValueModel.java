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

package com.simsilica.lemur;

import com.simsilica.lemur.core.VersionedReference;


/**
 *  A default implementation of the RangedValueModel interface
 *  that keeps a value and a version, incrementing the version
 *  whenever the value changes.  Values are kept between the
 *  configured min and max.
 *
 *  @author    Paul Speed
 */
public class DefaultRangedValueModel implements RangedValueModel {

    private long version;
    private double min;
    private double max;
    private double value;

    public DefaultRangedValueModel() {
        this(0, 100, 0);
    }

    public DefaultRangedValueModel( double min, double max, double value ) {
        this.min = min;
        this.max = max;
        this.value = value;
        checkRange();
    }

    public long getVersion() {
        return version;
    }

    public Double getObject() {
        return getValue();
    }

    public VersionedReference<Double> createReference() {
        return new VersionedReference<Double>(this);
    }

    protected void checkRange() {
        value = Math.max(min, value);
        value = Math.min(max, value);
    }

    public void setValue( double value ) {
        if( this.value == value )
            return;
        this.value = value;
        version++;
        checkRange();
    }

    public double getValue() {
        return value;
    }

    public void setPercent( double v ) {
        double range = max - min;
        double projected = min + range * v;
        setValue(projected);
    }

    public double getPercent() {
        double range = max - min;
        if( range == 0 )
            return 0;
        double part = getValue() - min;
        return part / range;
    }

    public void setMaximum( double max ) {
        if( this.max == max ) 
            return;
        this.max = max;
        version++;
        checkRange();
    }

    public double getMaximum() {
        return max;
    }

    public void setMinimum( double min ) {
        if( this.min == min ) 
            return;
        this.min = min;
        version++;
        checkRange();
    }

    public double getMinimum() {
        return min;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[value=" + value + ", min=" + min + ", max=" + max + "]";
    }
}

