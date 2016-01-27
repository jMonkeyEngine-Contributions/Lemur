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
 * SOFTWARE, Even IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur;


/**
 *  Specifies the overall type of fill for a distribution
 *  or layout.
 *
 *  None - indicates that none of the children will be stretched and
 *         there default size will always be used.
 *  Even - all children will get an even amount of stretching.  This does
 *         not mean that they are all the same size.  It means that
 *         any extra sizing is evenly distributed amongst all children.
 *  Proportional - children will get stretching based on their portion
 *         of the overal preferred size.  Instead of evenly distributing
 *         extra sizing, each child will get a proportional amount of
 *         the sizing based on their relative size.  If three children
 *         start out at 100, 50, and 50 and there are 10 extra units to
 *         go around then the first will get 5 and the other two will get
 *         2.5.
 *  ForcedEven - all children will be the same size, evenly distributed
 *         over the total container size.  Note: this may make children
 *         lay out smaller than their preferred size where they otherwise
 *         wouldn't be.
 *  First - the first child gets all of the extra space and all other children
 *          are preferred size.
 *  Last  - the last child gets all of the extra space and all other children
 *          are preferred size.
 *
 *  @author    Paul Speed
 */
public enum FillMode {
    None, Even, Proportional, ForcedEven, First, Last
}
