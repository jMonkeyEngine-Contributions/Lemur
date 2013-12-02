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


// If the "localAssets" directory doesn't exist then create it for the user
new File("localAssets").mkdirs();

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




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileAssetInfo extends AssetInfo {

    private File file;

    public FileAssetInfo( AssetManager manager, AssetKey key, File file ) {
        super(manager, key);
        this.file = file;
    }

    @Override
    public InputStream openStream() {
        try {
            return new FileInputStream(file);
        } catch( FileNotFoundException ex ) {
            // NOTE: Can still happen even if file.exists() is true, e.g.
            // permissions issue and similar
            throw new AssetLoadException("Failed to open file: " + file, ex);
        }
    }
}

/**
 *  Implements a looser/smarter asset locating for when
 *  picking random assets from a folder.  This attempts to
 *  find dependent assets but checking elements of the asset's
 *  path against elements of the root path on up the hiearchy.
 *  The search expands as follows:
 *  For every root path expanding up from the current one:
 *  -check for the asset without folders
 *  -check for the asset with just the last folder part
 *  -check for the asset with the next previous foloder part
 *  -and so on
 */
class ExpandingLocator implements AssetLocator {

    static Logger log = LoggerFactory.getLogger("scripts.ExpandingLocator");
    
    String rootPath;
           
    public AssetInfo locate( AssetManager manager, AssetKey key ) {
        
        if( log.isTraceEnabled() ) {
            log.trace("locate(" + key + ")");
        }
        
        // See if that path exists on the file system
        File f = new File(rootPath, key.toString());
        if( f.exists() ) {
            return new FileAssetInfo(manager, key, f); 
        }
 
        // Build the list of paths that we will check
        // at each directory level
        def paths = key.folder.split("/").reverse();
        def name = key.name - key.folder;
 
        def post = "";
        paths = paths.collect { it += "/" + post; post = it; it }
        paths.add(0, "/");  // make sure we check just the asset name itself, too
        

        // We will search up one level at a time in order
        // to find the closest sibling match possible.  In other
        // words, we'd rather find a neighboring somedir/foo.ext
        // than to find a foo.ext in the root
        for( File root = new File(rootPath); root != null; root = root.parentFile ) {
            if( log.isTraceEnabled() ) {
                log.trace("Checking root:" + root);
            }
            for( path in paths ) {
                def fullPath = path + name;
                f = new File(root, fullPath);
                if( log.isTraceEnabled() ) {
                    log.trace("File:" + f + "  exists:" + f.exists());
                }
                if( f.exists() ) {
                    return new FileAssetInfo(manager, key, f);  
                }
            }
        }
        
        return null;
    }
}
