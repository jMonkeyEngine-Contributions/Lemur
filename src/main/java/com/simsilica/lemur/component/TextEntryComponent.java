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

import java.util.*;

import com.jme3.font.*;
import com.jme3.font.BitmapFont.Align;
import com.jme3.font.BitmapFont.VAlign;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Quad;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.core.GuiUpdateListener;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.event.KeyListener;
import com.simsilica.lemur.event.KeyModifiers;
import com.simsilica.lemur.event.ModifiedKeyInputEvent;
import com.simsilica.lemur.focus.FocusTarget;
import com.simsilica.lemur.focus.FocusNavigationState;
import com.simsilica.lemur.focus.FocusTraversal.TraversalDirection;
import com.simsilica.lemur.text.DocumentModel;
import com.simsilica.lemur.text.DefaultDocumentModel;


/**
 *  A basic text entry component that allows displaying and editing of
 *  text based on a DocumentModel.  Default key bindings are setup for
 *  common navigation functions and the key input is taken over while the
 *  component has focus.
 *
 *  @author    Paul Speed
 */
public class TextEntryComponent extends AbstractGuiComponent
                                implements FocusTarget, ColoredComponent {

    public static final KeyActionListener DOC_HOME = new DocumentHome();
    public static final KeyActionListener DOC_END = new DocumentEnd();
    public static final KeyActionListener LINE_HOME = new LineHome();
    public static final KeyActionListener LINE_END = new LineEnd();
    public static final KeyActionListener PREV_LINE = new PreviousLine();
    public static final KeyActionListener NEXT_LINE = new NextLine();
    public static final KeyActionListener LEFT = new CaratLeft();
    public static final KeyActionListener RIGHT = new CaratRight();
    public static final KeyActionListener NO_OP = new NullAction();
    public static final KeyActionListener BACKSPACE = new Backspace();
    public static final KeyActionListener NEW_LINE = new NewLine();
    public static final KeyActionListener DELETE = new Delete();

    public static final KeyActionListener FOCUS_NEXT = new FocusChange(TraversalDirection.Next);
    public static final KeyActionListener FOCUS_PREVIOUS = new FocusChange(TraversalDirection.Previous);
    public static final KeyActionListener FOCUS_DOWN = new FocusChange(TraversalDirection.Down);
    public static final KeyActionListener FOCUS_UP = new FocusChange(TraversalDirection.Up);

    private static final Map<KeyAction,KeyActionListener> standardActions = new HashMap<KeyAction,KeyActionListener>();
    static {
        standardActions.put(new KeyAction(KeyInput.KEY_HOME), LINE_HOME);
        standardActions.put(new KeyAction(KeyInput.KEY_END), LINE_END);
        standardActions.put(new KeyAction(KeyInput.KEY_HOME, KeyModifiers.CONTROL_DOWN), DOC_HOME);
        standardActions.put(new KeyAction(KeyInput.KEY_END, KeyModifiers.CONTROL_DOWN), DOC_END);

        standardActions.put(new KeyAction(KeyInput.KEY_UP), PREV_LINE);
        standardActions.put(new KeyAction(KeyInput.KEY_DOWN), NEXT_LINE);
        standardActions.put(new KeyAction(KeyInput.KEY_LEFT), LEFT);
        standardActions.put(new KeyAction(KeyInput.KEY_RIGHT), RIGHT);

        standardActions.put(new KeyAction(KeyInput.KEY_BACK), BACKSPACE);
        standardActions.put(new KeyAction(KeyInput.KEY_RETURN), NEW_LINE);
        standardActions.put(new KeyAction(KeyInput.KEY_NUMPADENTER), NEW_LINE);
        standardActions.put(new KeyAction(KeyInput.KEY_DELETE), DELETE);
    }

    private BitmapFont font;
    private BitmapText bitmapText;
    private Rectangle textBox;
    private HAlignment hAlign = HAlignment.Left;
    private VAlignment vAlign = VAlignment.Top;
    private Vector3f preferredSize;
    private float preferredWidth;
    private int preferredLineCount;
    private KeyHandler keyHandler = new KeyHandler();
    private Quad cursorQuad;
    private Geometry cursor;
    private DocumentModel model;
    private boolean singleLine;
    private boolean focused;
    private boolean cursorVisible = true;

    private VersionedReference<DocumentModel> modelRef;
    private VersionedReference<Integer> caratRef;
    private GuiUpdateListener updateListener = new ModelChecker();

    // This really only works properly in single-line mode.
    private int textOffset = 0;

    private Map<KeyAction,KeyActionListener> actionMap = new HashMap<KeyAction,KeyActionListener>(standardActions);

    public TextEntryComponent( BitmapFont font ) {
        this( new DefaultDocumentModel(), font );
    }

    public TextEntryComponent( DocumentModel model, BitmapFont font ) {
        this.font = font;
        this.bitmapText = new BitmapText(font);
        bitmapText.setLineWrapMode(LineWrapMode.Clip);
        // Can't really do this since we don't know what
        // bucket it will actually end up in Gui or regular.
        //bitmapText.setQueueBucket( Bucket.Transparent );
        this.model = model;
        
        // Create a versioned reference for watching for updates, external or otherwise
        this.modelRef = model.createReference();
        this.caratRef = model.createCaratReference();

        cursorQuad = new Quad(bitmapText.getLineHeight()/16f, bitmapText.getLineHeight());
        cursor = new Geometry( "cursor", cursorQuad );
        GuiMaterial mat = GuiGlobals.getInstance().createMaterial(new ColorRGBA(1,1,1,0.75f), false);
        cursor.setMaterial(mat.getMaterial());
        cursor.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        cursor.setUserData("layer", 1);
        bitmapText.attachChild(cursor);

        if( model.getText() != null ) {
            resetText();
        }
    }

    @Override
    public TextEntryComponent clone() {
        TextEntryComponent result = (TextEntryComponent)super.clone();
        result.bitmapText = new BitmapText(font);
        bitmapText.setLineWrapMode(LineWrapMode.Clip);
        
        result.model = model.clone();
        result.preferredSize = null;
        result.textBox = null;
        result.keyHandler = result.new KeyHandler();
        result.cursorQuad = new Quad(bitmapText.getLineHeight()/16f, bitmapText.getLineHeight());
        result.cursor = new Geometry("cursor", cursorQuad);
        GuiMaterial mat = GuiGlobals.getInstance().createMaterial(new ColorRGBA(1,1,1,0.75f), false);
        result.cursor.setMaterial(mat.getMaterial());
        result.cursor.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        result.bitmapText.attachChild(cursor);
        result.resetText();

        return result;
    }

    @Override
    public void attach( GuiControl parent ) {
        super.attach(parent);
        parent.addUpdateListener(updateListener);
        getNode().attachChild(bitmapText);
        resetCursorPosition();
        resetCursorState();

        if( focused ) {
            GuiGlobals.getInstance().addKeyListener(keyHandler);
        }
    }

    @Override
    public void detach( GuiControl parent ) {
        GuiGlobals.getInstance().removeKeyListener(keyHandler);

        getNode().detachChild(bitmapText);
        parent.removeUpdateListener(updateListener);
        super.detach(parent);
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public boolean isFocusable() {
        return true; // should return isEnabled() when we have one
    }

    @Override
    public void focusGained() {
        if( this.focused )
            return;
        this.focused = true;
        GuiGlobals.getInstance().addKeyListener(keyHandler);
        resetCursorState();
    }

    @Override
    public void focusLost() {
        if( !this.focused )
            return;
        this.focused = false;
        GuiGlobals.getInstance().removeKeyListener(keyHandler);
        resetCursorState();
    }

    public Map<KeyAction,KeyActionListener> getActionMap() {
        return actionMap;
    }

    public DocumentModel getDocumentModel() {
        return model;
    }

    public void setSingleLine( boolean f ) {
        this.singleLine = f;
        if( singleLine ) {
            actionMap.put(new KeyAction(KeyInput.KEY_RETURN), FOCUS_NEXT);
            actionMap.put(new KeyAction(KeyInput.KEY_NUMPADENTER), FOCUS_NEXT);
            actionMap.put(new KeyAction(KeyInput.KEY_TAB), FOCUS_NEXT);
            actionMap.put(new KeyAction(KeyInput.KEY_TAB, KeyModifiers.SHIFT_DOWN), FOCUS_PREVIOUS);
            actionMap.put(new KeyAction(KeyInput.KEY_UP), FOCUS_UP);
            actionMap.put(new KeyAction(KeyInput.KEY_DOWN), FOCUS_DOWN);
        } else {
            actionMap.put(new KeyAction(KeyInput.KEY_RETURN), NEW_LINE);
            actionMap.put(new KeyAction(KeyInput.KEY_NUMPADENTER), NEW_LINE);
            
            // We may choose to do something different with tab someday... but 
            // the user can also just remove the action if they like.
            actionMap.put(new KeyAction(KeyInput.KEY_TAB), FOCUS_NEXT);
            actionMap.put(new KeyAction(KeyInput.KEY_TAB, KeyModifiers.SHIFT_DOWN), FOCUS_PREVIOUS);
            
            actionMap.put(new KeyAction(KeyInput.KEY_UP), PREV_LINE);
            actionMap.put(new KeyAction(KeyInput.KEY_DOWN), NEXT_LINE);
        }
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setFont( BitmapFont font ) {
        if( font == bitmapText.getFont() )
            return;
    
        if( isAttached() ) {
            bitmapText.removeFromParent();
        }
        
        // Can't change the font once created so we'll
        // have to create it fresh
        BitmapText newText = new BitmapText(font);
        newText.setLineWrapMode(LineWrapMode.Clip);
        newText.setText(getText());
        newText.setColor(getColor());
        newText.setLocalTranslation(bitmapText.getLocalTranslation());
        newText.setSize(getFontSize());
        this.bitmapText = newText;
 
        // The cursor is attached to the bitmap text directly
        // so we need to move it.       
        bitmapText.attachChild(cursor);
                
        resizeCursor();
        resetCursorPosition();
        resetText();
                
        if( isAttached() ) {
            getNode().attachChild(bitmapText);
        }
    }

    public BitmapFont getFont() {
        return bitmapText.getFont();
    }

    public void setFontSize( float f ) {
        this.bitmapText.setSize(f);
        resizeCursor();
        resetCursorPosition();
        resetText();
    }

    public float getFontSize() {
        return bitmapText.getSize();
    }

    protected void resetCursorColor() {
        float alpha = bitmapText.getAlpha();
        if( alpha == -1 ) {
            alpha = 1;
        }
        ColorRGBA color = bitmapText.getColor();
        
        if( alpha == 1 ) {
            cursor.getMaterial().setColor("Color", color);
        } else {
            ColorRGBA cursorColor = color != null ? color.clone() : ColorRGBA.White.clone();
            cursorColor.a = alpha;
            cursor.getMaterial().setColor("Color", cursorColor);
        }
    }

    @Override
    public void setColor( ColorRGBA color ) {
        float alpha = bitmapText.getAlpha();
        bitmapText.setColor(color);
        if( alpha != 1 ) {
            bitmapText.setAlpha(alpha);
        }
        resetCursorColor();
    }

    @Override
    public ColorRGBA getColor() {
        return bitmapText.getColor();
    }

    @Override
    public void setAlpha( float f ) {
        bitmapText.setAlpha(f);
        resetCursorColor();
    }
    
    @Override
    public float getAlpha() {
        return bitmapText.getAlpha();
    }

    protected void resetText() {
        String text = model.getText();
        if( textOffset != 0 ) {
            textOffset = Math.min(textOffset, text.length());
            text = text.substring(textOffset);

            if( textBox != null ) {
                // See if this offset even makes sense now
                float x = getVisibleWidth(text);
                if( x < textBox.width ) {
                    while( textOffset > 0 ) {
                        textOffset--;
                        text = model.getText().substring(textOffset);
                        x = getVisibleWidth(text);
                        if( x > textBox.width ) {
                            textOffset++;
                            text = model.getText().substring(textOffset);
                            break;
                        }
                    }

                }
            }
        }

        if( text != null && text.equals(bitmapText.getText()) )
            return;

        bitmapText.setText(text);
        resetCursorPosition();
        invalidate();
    }

    protected float getVisibleWidth( String text ) {
        float x = font.getLineWidth(text + " ");
        x -= font.getLineWidth(" ");
        //x += 1;
        float scale = bitmapText.getSize() / font.getPreferredSize();
        x *= scale;
        return x;
    }

    protected void resizeCursor() {
        cursorQuad.updateGeometry(bitmapText.getLineHeight()/16f, bitmapText.getLineHeight());
        cursorQuad.clearCollisionData(); 
    }

    protected void resetCursorState() {
        if( isAttached() && focused && cursorVisible ) {
            cursor.setCullHint(CullHint.Inherit);
        } else {
            cursor.setCullHint(CullHint.Always);
        }
    }

    protected void resetCursorPosition() {
        // Find the current cursor position.
        int line = model.getCaratLine();
        int column = model.getCaratColumn();

        if( column < textOffset ) {
            textOffset = column;
            resetText();
        }

        String row = model.getLine(line);
        row = row.substring(textOffset,column);

        // We add an extra space to properly advance (since often
        // the space character only has a width of 1 but will advance
        // far) then we subtract that space width back.
        float x = font.getLineWidth(row + " ");
        x -= font.getLineWidth(" ");

        // And pad it out just a bit...
        //x += 1;

        float scale = bitmapText.getSize() / font.getPreferredSize();
        x *= scale;

        float y = -line * bitmapText.getLineHeight();
        y -= bitmapText.getLineHeight();

        if( textBox != null && x > textBox.width ) {
            if( singleLine ) {
                // Then we can move the text offset and try again
                textOffset++;
                resetText();
                resetCursorPosition();
                return;
            } else {
                // Make it invisible
                cursorVisible = false;
                resetCursorState();
            }
        } else {
            cursorVisible = true;
            resetCursorState();
        }

        cursor.setLocalTranslation(x, y, 0.01f);
    }

    public void setText( String text ) {
        if( text != null && text.equals(model.getText()) )
            return;

        model.setText(text);
        //resetText();  ...should be automatic now
    }

    public String getText() {
        return model.getText();
    }
    
    public void setHAlignment( HAlignment a ) {
        if( hAlign == a )
            return;
        hAlign = a;
        resetAlignment();
    }

    public HAlignment getHAlignment() {
        return hAlign;
    }

    public void setVAlignment( VAlignment a ) {
        if( vAlign == a )
            return;
        vAlign = a;
        resetAlignment();
    }

    public VAlignment getVAlignment() {
        return vAlign;
    }

    public void setPreferredSize( Vector3f v ) {
        this.preferredSize = v;
        invalidate();
    }

    public Vector3f getPreferredSize() {
        return preferredSize;
    }

    public void setPreferredWidth( float f ) {
        this.preferredWidth = f;
        invalidate();
    }

    public float getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredLineCount( int i ) {
        this.preferredLineCount = i;
        invalidate();
    }

    public float getPreferredLineCount() {
        return preferredLineCount;
    }

    @Override
    public void reshape(Vector3f pos, Vector3f size) {
        bitmapText.setLocalTranslation(pos.x, pos.y, pos.z);
        textBox = new Rectangle(0, 0, size.x, size.y);
        bitmapText.setBox(textBox);
        resetAlignment();
    }

    @Override
    public void calculatePreferredSize( Vector3f size ) {
        if( preferredSize != null ) {
            size.set(preferredSize);
            return;
        }

        // Make sure that the bitmapText reports a reliable
        // preferred size
        bitmapText.setBox(null);

        if( preferredWidth == 0 ) {
            size.x = bitmapText.getLineWidth();
        } else {
            size.x = preferredWidth;
        }
        if( preferredLineCount == 0 ) {
            size.y = bitmapText.getHeight();
        } else {
            size.y = bitmapText.getLineHeight() * preferredLineCount;
        }

        // Reset any text box we already had
        bitmapText.setBox(textBox);
    }

    protected void resetAlignment() {
        if( textBox == null )
            return;

        switch( hAlign ) {
            case Left:
                bitmapText.setAlignment(Align.Left);
                break;
            case Right:
                bitmapText.setAlignment(Align.Right);
                break;
            case Center:
                bitmapText.setAlignment(Align.Center);
                break;
        }
        switch( vAlign ) {
            case Top:
                bitmapText.setVerticalAlignment(VAlign.Top);
                break;
            case Bottom:
                bitmapText.setVerticalAlignment(VAlign.Bottom);
                break;
            case Center:
                bitmapText.setVerticalAlignment(VAlign.Center);
                break;
        }
    }

    private static class DocumentHome implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.home(false);
            //source.resetCursorPosition(); should be automatic now
        }
    }

    private static class LineHome implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.home(true);
            //source.resetCursorPosition(); should be automatic now
        }
    }

    private static class DocumentEnd implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.end(false);
            //source.resetCursorPosition(); should be automatic now
        }
    }

    private static class LineEnd implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.end(true);
            //source.resetCursorPosition(); should be automatic now
        }
    }

    private static class PreviousLine implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.up();
            //source.resetCursorPosition(); should be automatic now
        }
    }

    private static class NextLine implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.down();
            //source.resetCursorPosition(); should be automatic now
        }
    }

    private static class CaratLeft implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.left();
            //source.resetCursorPosition(); should be automatic now
        }
    }

    private static class CaratRight implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.right();
            //source.resetCursorPosition(); should be automatic now
        }
    }

    private static class NullAction implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
        }
    }

    private static class Backspace implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.backspace();
            //source.resetText(); // should be automic now
        }
    }

    private static class NewLine implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.insertNewLine();
            //source.resetText(); // should be automic now
        }
    }

    private static class Delete implements KeyActionListener {
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            source.model.delete();
            //source.resetText(); // should be automic now
        }
    }
    
    private static class FocusChange implements KeyActionListener {
        private TraversalDirection dir;
        
        public FocusChange( TraversalDirection dir ) {
            this.dir = dir;
        }
    
        @Override
        public void keyAction( TextEntryComponent source, KeyAction key ) {
            FocusNavigationState nav = GuiGlobals.getInstance().getFocusNavigationState();
            if( nav == null ) {
                return;
            }
            Spatial current = GuiGlobals.getInstance().getCurrentFocus();
            nav.requestChangeFocus(current, dir);    
        } 
    }


    private class KeyHandler implements KeyListener {
        private boolean shift = false;
        private boolean control = false;

        @Override
        public void onKeyEvent( KeyInputEvent evt ) {
            ModifiedKeyInputEvent mEvt = (ModifiedKeyInputEvent)evt;                    
            if( mEvt.isPressed() ) {
                KeyAction key = mEvt.toKeyAction(); //new KeyAction(code, (control?KeyAction.CONTROL_DOWN:0), (shift?KeyAction.SHIFT_DOWN:0) );
                KeyActionListener handler = actionMap.get(key);
                if( handler != null ) {
                    handler.keyAction(TextEntryComponent.this, key);
                    evt.setConsumed();
                    return;
                }

                // Making sure that no matter what, certain
                // characters never make it directly to the
                // document
                if( evt.getKeyChar() >= 32 ) {
                    model.insert(evt.getKeyChar());
                    evt.setConsumed();
                    //resetText(); ...should be automatic now
                }
            }
        }
    }
 
    /**
     *  Checks for changes in the model and updates the text display
     *  or cursor position as necessary.
     */   
    private class ModelChecker implements GuiUpdateListener {
    
        @Override       
        public void guiUpdate( GuiControl source, float tpf ) {
            if( modelRef.update() ) {
                resetText();
            }
            if( caratRef.update() ) {
                resetCursorPosition();   
            }
        }
    }
}
