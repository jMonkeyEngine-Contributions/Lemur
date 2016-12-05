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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.core.UnshadedMaterialAdapter;
import com.simsilica.lemur.core.LightingMaterialAdapter;
import com.simsilica.lemur.style.Styles;
import com.simsilica.lemur.event.KeyListener;
import com.simsilica.lemur.event.KeyInterceptState;
import com.simsilica.lemur.event.MouseAppState;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.focus.FocusManagerState;
import com.simsilica.lemur.focus.FocusNavigationState;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapFont;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.event.TouchAppState;
import com.simsilica.lemur.input.InputMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  A utility class that sets up some default global behavior for
 *  the default GUI elements and provides some common access to
 *  things like the AssetManager.
 *
 *  <p>When initialized, GuiGlobals will keep a reference to the
 *  AssetManager for use in creating materials, loading fonts, and so
 *  on.  It will also:
 *  <ul>
 *  <li>Setup the KeyInterceptState for allowing edit fields to intercept
 *      key events ahead of the regular input processing.</li>
 *  <li>Initialize InputMapper to provide advanced controller input processing.</li>
 *  <li>Setup the MouseAppState to provide default mouse listener and picking
 *      support for registered pick roots.</li>
 *  <li>Setup the FocusManagerState that keeps track of the currently
 *      focused component and makes sure transition methods are properly called.</li>
 *  <li>Setup the default styles.</li>
 *  <li>Sets up the layer based geometry comparators for the default app
 *      viewport.</li>
 *  </ul>
 *
 *  <p>For applications that wish to customize the behavior of GuiGlobals,
 *  it is possible to set a custom subclass instead of initializing the
 *  default implemenation.  Examples of reasons do do this might include
 *  using custom materials instead of the default JME materials or otherwise
 *  customizing the initialization setup.</p>
 *
 *  @author    Paul Speed
 */
public class GuiGlobals {

    static Logger log = LoggerFactory.getLogger(GuiGlobals.class);

    private static GuiGlobals instance;

    private AssetManager assets;
    private InputMapper  inputMapper;
    private KeyInterceptState keyInterceptor;
    private MouseAppState mouseState;
    private TouchAppState touchState;
    private FocusManagerState focusState;
    private FocusNavigationState focusNavState;
    private AnimationState animationState;
    private PopupState popupState;
    private String iconBase;

    private Styles styles;

    public static void initialize( Application app ) {
        setInstance(new GuiGlobals(app));
    }

    public static void setInstance( GuiGlobals globals ) {
        instance = globals;
        log.info( "Initializing GuiGlobals with:" + globals );
        instance.logBuildInfo();
    }

    public static GuiGlobals getInstance() {
        return instance;
    }

    protected GuiGlobals( Application app ) {
        this.assets = app.getAssetManager();
        this.keyInterceptor = new KeyInterceptState(app);
        
        // For now, pick either mouse or touch based on the
        // availability of touch.  It's an either/or at the 
        // moment but the rest of the code is setup to support
        // both at once should we ever want to support touch
        // devices that also may have a mouse connected.
        if (app.getContext().getTouchInput() == null) {
            this.mouseState = new MouseAppState(app);
        } else {
            this.touchState = new TouchAppState(app);
        }
        
        this.inputMapper = new InputMapper(app.getInputManager());
        this.focusState = new FocusManagerState();
        this.focusNavState = new FocusNavigationState(inputMapper, focusState);
        this.animationState = new AnimationState();
        this.popupState = new PopupState();

        // Write the app state dependencies directly so that:
        // a) they are there before initialization
        // b) so that the states don't have to rely on GuiGlobals to find
        //    them.
        // c) so that we might disable them properly even at runtime
        //    if the user kills or replaces the nav state
        focusState.setFocusNavigationState(focusNavState);
        
        app.getStateManager().attach(keyInterceptor);
        
        if( mouseState != null ) {
            app.getStateManager().attach(mouseState);
        }
        if( touchState != null ) {
            app.getStateManager().attach(touchState);
        }
        
        app.getStateManager().attach(focusState);
        app.getStateManager().attach(focusNavState);
        app.getStateManager().attach(animationState);
        app.getStateManager().attach(popupState);

        styles = new Styles();
        setDefaultStyles();

        iconBase = getClass().getPackage().getName().replace( '.', '/' ) + "/icons";

        ViewPort main = app.getViewPort();
        setupGuiComparators(main);
    }

    protected AssetManager getAssetManager() {
        return assets;
    }
 
    protected String getIconBase() {
        return iconBase;
    }

    protected void logBuildInfo() {
        try {
            java.net.URL u = Resources.getResource("lemur.build.date");
            String build = Resources.toString(u, Charsets.UTF_8);
            log.info("Lemur build date:" + build);
        } catch( Exception e ) {
            log.error("Error reading build info", e);
        }
    }

    public void setupGuiComparators( ViewPort view ) {
        RenderQueue rq = view.getQueue();

        rq.setGeometryComparator(Bucket.Opaque,
                                 new LayerComparator(rq.getGeometryComparator(Bucket.Opaque), -1));
        rq.setGeometryComparator(Bucket.Transparent,
                                 new LayerComparator(rq.getGeometryComparator(Bucket.Transparent), -1));
        rq.setGeometryComparator(Bucket.Gui,
                                 new LayerComparator(rq.getGeometryComparator(Bucket.Gui), -1));
    }

    protected void setDefaultStyles() {
        styles.setDefault(loadFont("Interface/Fonts/Default.fnt"));
        styles.setDefault(ColorRGBA.LightGray);

        // Setup some default styles for the "DEFAULT" Style
        styles.getSelector(null).set("color", ColorRGBA.White);
    }

    public Styles getStyles() {
        return styles;
    }

    public InputMapper getInputMapper() {
        return inputMapper;
    }

    public AnimationState getAnimationState() {
        return animationState;
    }
    
    public PopupState getPopupState() {
        return popupState;
    }
    
    public FocusManagerState getFocusManagerState() {
        return focusState;
    }

    public FocusNavigationState getFocusNavigationState() {
        return focusNavState;
    }

    /**
     *  Goes through all of the font page materials and sets
     *  alpha test and alpha fall-off.
     */
    public void fixFont( BitmapFont font ) {
        for( int i = 0; i < font.getPageSize(); i++ ) {
            Material m = font.getPage(i);
            m.getAdditionalRenderState().setAlphaTest(true);
            m.getAdditionalRenderState().setAlphaFallOff(0.1f);
            m.setFloat("AlphaDiscardThreshold", 0.1f);
        }
    }

    private Texture getTexture( Material mat, String name ) {
        MatParam mp = mat.getParam(name);
        if( mp == null ) {
            return null;
        }
        return (Texture)mp.getValue();
    }

    public void lightFont( BitmapFont font ) {
        Material[] pages = new Material[font.getPageSize()];
        for( int i = 0; i < pages.length; i++ ) {
            Material original = font.getPage(i);
            Material m = new Material(assets, "Common/MatDefs/Light/Lighting.j3md");
            m.setTexture("DiffuseMap", getTexture(original, "ColorMap"));
            pages[i] = m;
        }
        font.setPages(pages);
    }

    public BitmapFont loadFont( String path ) {
        BitmapFont result = assets.loadFont(path);
        fixFont(result);
        return result;
    }

    public GuiMaterial createMaterial( boolean lit ) {
        if( lit ) {
            return new LightingMaterialAdapter(new Material(assets, "Common/MatDefs/Light/Lighting.j3md"));
        } else {
            return new UnshadedMaterialAdapter(new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md"));
        }
    }

    public GuiMaterial createMaterial( ColorRGBA color, boolean lit ) {
        GuiMaterial mat = createMaterial(lit);
        mat.setColor(color);
        return mat;
    }

    public GuiMaterial createMaterial( Texture texture, boolean lit ) {
        GuiMaterial mat = createMaterial(lit);
        mat.setTexture(texture);
        return mat;
    }

    public Texture loadDefaultIcon( String name ) {
        return loadTexture(iconBase + "/" + name, false, false);
    }

    public Texture loadTexture( String path, boolean repeat, boolean generateMips ) {
        TextureKey key = new TextureKey(path);
        key.setGenerateMips(generateMips);

        Texture t = assets.loadTexture(key);
        if( t == null ) {
            throw new RuntimeException("Error loading texture:" + path);
        }

        if( repeat ) {
            t.setWrap(Texture.WrapMode.Repeat);
        } else {
            t.setWrap(Texture.WrapMode.Clamp);
        }

        return t;
    }

    public void requestFocus( Spatial s ) {
        focusState.setFocus(s);
    }

    public Spatial getCurrentFocus() {
        return focusState.getFocus();
    }

    public void addKeyListener( KeyListener l ) {
        keyInterceptor.addKeyListener(l);
    }

    public void removeKeyListener( KeyListener l ) {
        keyInterceptor.removeKeyListener(l);
    }

    @Deprecated
    public ViewPort getCollisionViewPort( Spatial s ) {
        if( mouseState != null ) {
            return mouseState.findViewPort(s);
        } else if( touchState != null ) {
            return touchState.findViewPort(s);
        } else {
            return null;
        }
    }

    /**
     *  @deprecated Use setCursorEventsEnabled() instead.
     */
    @Deprecated
    public void setMouseEventsEnabled( boolean f ) {
        setCursorEventsEnabled(f);
    }
    
    public void setCursorEventsEnabled( boolean f ) {
        if( mouseState != null ) {
            mouseState.setEnabled(f);
        }
        if( touchState != null ) {
            touchState.setEnabled(f);
        }
        if( focusNavState != null ) {
            focusNavState.setEnabled(f);
        }
    }

    /**
     *  @deprecated Use isCursorEventsEnabled() instead.
     */
    @Deprecated
    public boolean isMouseEventsEnabled() {
        return isCursorEventsEnabled(); 
    }
    
    public boolean isCursorEventsEnabled() {
        if( mouseState != null ) {
            return mouseState.isEnabled();
        } else if( touchState != null ) {
            return touchState.isEnabled();
        } else {
            return false;
        }        
    }

    @Deprecated
    public Vector3f getScreenCoordinates( Spatial relativeTo, Vector3f pos ) {
        ViewPort vp = getCollisionViewPort(relativeTo);
        if( vp == null ) {
            throw new RuntimeException("Could not find viewport for:" + relativeTo);
        }

        // Calculate the world position relative to the spatial
        pos = relativeTo.localToWorld(pos, null);

        Camera cam = vp.getCamera();
        if( cam.isParallelProjection() ) {
            return pos.clone();
        }

        return cam.getScreenCoordinates(pos);
    }
}
