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

import com.simsilica.lemur.style.ElementId;

/**
 *  For a given value, classes that implement this interface will
 *  provide a Panel that can display that value.  A ValueRenderer
 *  will often be reused for several values at a time (for example
 *  displaying the items of a list) which is why the 'existing' 
 *  panel is provided for reuse.  This is in contrast to a ValueEditor
 *  that will only be activated for one specific value at a time.  
 *  
 *  @author    Paul Speed
 */
public interface ValueRenderer<T> {

    /**
     *  Called by the using component to set the preferred ElementId
     *  and style for the renderer.  Implementations can ignore this if
     *  they wish to override the default element ID or style.
     */
    public void configureStyle( ElementId elementId, String style );

    /**
     *  Returns a new view Panel or returns a reconfigured version of
     *  the existing Panel that will display the specified value.  The 'selected'
     *  flag can indicate if the value should appear as selected or focused.
     *  For example, some views may change their color or animate if they
     *  are the selected view.
     */
    public Panel getView( T value, boolean selected, Panel existing );
}


