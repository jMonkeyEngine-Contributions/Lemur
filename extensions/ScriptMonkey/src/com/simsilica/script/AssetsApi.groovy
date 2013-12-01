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


import com.jme3.asset.*;
import com.jme3.asset.plugins.FileLocator;

scripts.addDefaultImports( "com.jme3.asset.*" );


// Setup a default local directory for dumping assets
def localAssets = new File("localAssets");
if( localAssets.exists() ) {
    app.assetManager.registerLocator(localAssets.path, FileLocator);
}


// Texture support---------------------------------
import com.jme3.texture.*;
scripts.addDefaultImports("com.jme3.texture.*");

Texture loadTexture( TextureKey key ) {
    return app.assetManager.loadTexture(key);
}

Texture loadTexture( String assetName ) {
    return app.assetManager.loadTexture(assetName); 
}




// Stick the sky stuff here for now
import com.jme3.util.SkyFactory;
import com.jme3.scene.Spatial;

Spatial createSky( Texture west, Texture east, Texture north, Texture south, Texture up, Texture down ) {
    return SkyFactory.createSky(app.assetManager, west, east, north, south, up, down);
}

Spatial createSky( String west, String east, String north, String south, String up, String down ) {
    return SkyFactory.createSky(app.assetManager, 
                                loadTexture(west), 
                                loadTexture(east), 
                                loadTexture(north), 
                                loadTexture(south), 
                                loadTexture(up), 
                                loadTexture(down));
}


