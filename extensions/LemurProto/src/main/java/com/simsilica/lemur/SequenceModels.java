/*
 * $Id$
 * 
 * Copyright (c) 2020, Simsilica, LLC
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

package com.simsilica.lemur;

import java.util.*;

import com.simsilica.lemur.core.VersionedReference;

/**
 *  Factory methods for standard sequence models.
 *  
 *  @author    Paul Speed
 */
public class SequenceModels {

    /**
     *  Returns an unbounded sequence that iterates over double values with
     *  the specified increment.  The step size is also used for the
     *  resolution.
     */
    public static SequenceModel<Double> doubleSequence( double initialValue, double step ) {
        return doubleSequence(initialValue, step, step);
    }
    
    /**
     *  Returns an unbounded sequence that iterates over double values with
     *  a specified increment.  A specified resolution can be included
     *  that will make sure that externally set values do not exceed
     *  some precision.  For example, a resolution of 0.001 makes sure that
     *  values will never be more precise than 3 decimal places.
     */
    public static SequenceModel<Double> doubleSequence( double initialValue, double step, double resolution ) {
        return new DoubleSequence(initialValue, step, resolution);
    }   

    /**
     *  Returns a bounded double sequence using the specified range model, step, and resolution.
     */
    public static SequenceModel<Double> rangedSequence( RangedValueModel model, double step, double resolution ) {
        return new RangedSequence(model, step, resolution); 
    }

    /**
     *  Returns an unbounded sequence that iterates over a list
     *  and wraps in both directions.  Note: lists with duplicate values
     *  may confuse the sequence model and should be avoided.
     */
    public static <T> SequenceModel<T> listSequence( List<T> list ) {
        return listSequence(list, null);
    }
    
    /**
     *  Returns an unbounded sequence that iterates over a list
     *  and wraps in both directions.  Note: lists with duplicate values
     *  may confuse the sequence model and should be avoided.
     */
    public static <T> SequenceModel<T> listSequence( List<T> list, T initialItem ) {
        return new ListSequence<>(list, initialItem);
    }
    
    public static abstract class AbstractSequence<T> implements SequenceModel<T> {
        private long version;
 
        protected void incrementVersion() {
            version++; 
        }
 
        @Override       
        public long getVersion() {
            return version;
        }

        @Override
        public VersionedReference<T> createReference() {
            return new VersionedReference<>(this); 
        }
                 
        @Override        
        public String toString() {
            return getClass().getSimpleName() + "[" + getObject() + "]";
        }
    }
    
    public static class DoubleSequence extends AbstractSequence<Double> {
        private double current;
        private double step;
        private double resolution;
        private double scale;
        
        public DoubleSequence( double current, double step, double resolution ) {
            this.current = current;
            this.step = step;
            this.resolution = resolution;
            this.scale = 1.0 / resolution;
        }

        public static double normalize( double value, double resolution ) {
            // Move the decimal place right and clamp the remainder
            double scaled = value / resolution;
            double clamped = Math.rint(scaled);
            // Move the decimal place back left again
            return clamped * resolution;            
        }

        /**
         *  Clamps the prevision of the specified value.
         */
        protected double normalize( double value ) {
            return normalize(value, resolution);            
        }

        @Override        
        public Double getObject() {
            return current;
        }
        
        @Override        
        public void setObject( Double object ) {
            if( object == null ) {
                throw new IllegalArgumentException("Value cannot be null");
            }
            double val = normalize(object);
            if( current == val ) {
                return;
            }
            this.current = val;            
            incrementVersion();
        }
         
        @Override        
        public Double getNextObject() {
            return normalize(current + step);
        }
        
        @Override        
        public Double getPreviousObject() {
            return normalize(current - step);
        }
        
    }    

    public static class RangedSequence extends AbstractSequence<Double> {
        private RangedValueModel model;
        private double step;
        private double resolution;
        private double scale;
        
        public RangedSequence( RangedValueModel model, double step, double resolution ) {
            this.model = model;
            this.step = step;
            this.resolution = resolution;
            this.scale = 1.0 / resolution;
        }

        /**
         *  Clamps the prevision of the specified value.
         */
        protected double normalize( double value ) {
            return DoubleSequence.normalize(value, resolution);            
        }

        @Override        
        public Double getObject() {
            return normalize(model.getValue());
        }
        
        @Override        
        public void setObject( Double object ) {
            if( object == null ) {
                throw new IllegalArgumentException("Value cannot be null");
            }
            double val = normalize(object);
            if( model.getValue() == val ) {
                return;
            }
            model.setValue(val);
        }
         
        @Override        
        public Double getNextObject() {
            return normalize(model.getValue() + step);
        }
        
        @Override        
        public Double getPreviousObject() {
            return normalize(model.getValue() - step);
        }
        
        @Override       
        public long getVersion() {
            return model.getVersion();
        }
    }    
    
    public static class ListSequence<T> extends AbstractSequence<T> {
        private Object currentValue; // we keep this in case the list moves
        private int index;
        private List<T> list;
        
        public ListSequence( List<T> list, T initialItem ) {
            this.list = list;
            
            // We don't gate for null because the list might actually
            // contain null.
            this.index = list.indexOf(initialItem);
            this.currentValue = initialItem;
            if( index < 0 ) {
                index = 0;
            }
        }
        
        protected int nextIndex( int i ) {
            return (i + 1) % list.size();
        }
        
        protected int previousIndex( int i ) {
            return (i + list.size() - 1) % list.size();
        }

        @Override        
        public T getObject() {
            if( list.isEmpty() ) {
                return null;
            }
            return list.get(index);
        }
        
        @Override        
        public void setObject( T object ) {
            // Try to do it the easy way first... if the value is the same as 
            // last time and the current value hasn't moved underneath us
            // Note: it's not enough just to check index or just to check value
            // because the list may have been reorganized in such a way that
            // the value might be the same but the index is different or the index
            // might be the same but the value is different... and then we need to
            // make sure to increment the version.
            if( Objects.equals(currentValue, object) && Objects.equals(currentValue, getObject()) ) {
                return; // we're already here   
            }
            if( Objects.equals(getNextObject(), object) ) {
                index = nextIndex(index);
            } else  if( Objects.equals(getPreviousObject(), object) ) {
                index = previousIndex(index);
            } else {
                // Else we have to find it
                index = list.indexOf(object);
                if( index < 0 ) {
                    throw new IllegalArgumentException("Item is not in sequence:" + object);
                }
            }
            this.currentValue = object;
            incrementVersion();
        }
         
        @Override        
        public T getNextObject() {
            return list.get(nextIndex(index));
        }
        
        @Override        
        public T getPreviousObject() {
            return list.get(previousIndex(index));
        }
        
    }  
}

