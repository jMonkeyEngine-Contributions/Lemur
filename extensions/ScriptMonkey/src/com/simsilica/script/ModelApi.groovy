/*
 * $Id$
 *
 * Copyright (c) 2013-2013 jMonkeyEngine
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

import com.jme3.scene.Spatial;
import com.jme3.asset.plugins.FileLocator;

Spatial loadModel( String model ) {
    return app.assetManager.loadModel(model);
}

Spatial loadModel( AssetKey key ) {
    return app.assetManager.loadModel(key);
}

Spatial loadModel( File model ) {
    def assets = app.assetManager;

    assets.registerLocator(model.parentFile.path, ExpandingLocator.class);
 
    try {
        return assets.loadModel(model.name);          
    } finally {
        assets.unregisterLocator(model.parentFile.path, ExpandingLocator.class);
    }
}

Spatial loadModel( File root, String model ) {
    def assets = app.assetManager;

    // Build a list of directories up to some "assets" directory
    // so that we can try to catch all of the potential roots    
    // Temporarily configure a new locator
    assets.registerLocator(root.path, FileLocator.class);
    try {
        return assets.loadModel(model);       
    } finally {
        assets.unregisterLocator(root.path, FileLocator.class);
    }
    
}

 
