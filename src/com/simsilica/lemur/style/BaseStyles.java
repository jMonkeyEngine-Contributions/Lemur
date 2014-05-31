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

package com.simsilica.lemur.style;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  Easy access to some built in style resources.
 *
 *  @author    Paul Speed
 */
public class BaseStyles {

    static Logger log = LoggerFactory.getLogger(BaseStyles.class);

    public static final String GLASS = "glass";
    public static final String GLASS_STYLE_RESOURCE = "com/simsilica/lemur/style/base/glass-styles.groovy";

    /**
     *  Loads the glass style and any glass style extensions found on 
     *  the classpath.
     */
    public static void loadGlassStyle() {
        // Find all of the glass style resources
        loadStyleResources(GLASS_STYLE_RESOURCE);
    }
    
    public static void loadStyleResources( String resource ) {
 
        if( resource.startsWith("/") ) {
            resource = resource.substring(1);
        }
        log.info("loadStyleResource(" + resource + ")");
        
        StyleLoader loader = new StyleLoader();
        
        // Attempt to load the class-local resource first... ie:
        // our version
        URL baseResource = BaseStyles.class.getResource("/" + resource);
        log.info("Loading base resource:" + baseResource);        
        loader.loadStyle(baseResource);
 
        log.info("Loading extension resources for:" + resource);
        ClassLoader cl = BaseStyles.class.getClassLoader();
        try {
            for( Enumeration en = cl.getResources(resource); en.hasMoreElements(); ) {
                URL u = (URL)en.nextElement();
                if( u.equals(baseResource) ) {
                    continue;
                }
                log.info("Loading extension resource:" + u);
                loader.loadStyle(u);
            }
        } catch( IOException e ) {
            throw new RuntimeException("Error retreiving resources:" + resource, e);
        }
    }

}
