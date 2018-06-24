/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
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

package demo;

import java.util.*;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.event.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.*;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.*;

import com.simsilica.lemur.*;
import com.simsilica.lemur.dnd.*;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.event.*;

/**
 *  Demo and test of the drag-and-drop support in Lemur.
 *
 *  @author    Paul Speed
 */
public class DragAndDropDemoState extends BaseAppState {

    private Node dndRoot;

    private ColorRGBA containerColor = new ColorRGBA(1, 1, 0, 0.5f);
    private ColorRGBA containerHighlight = new ColorRGBA(0, 1, 0, 0.5f);

    private ContainerNode container1;    
    private ContainerNode container2;

    public DragAndDropDemoState() {
    }
    
    protected Node getRoot() {
        return dndRoot;
    }

    @Override 
    protected void initialize( Application app ) {
        dndRoot = new Node("dndRoot");
 
        DirectionalLight sun = new DirectionalLight(new Vector3f(1, -2, -5).normalizeLocal(),
                                                    ColorRGBA.White);
        dndRoot.addLight(sun);
        
        AmbientLight ambient = new AmbientLight(ColorRGBA.Gray);
        dndRoot.addLight(ambient);            
 
        // Setup a stack-based container       
        container1 = new ContainerNode("container1", containerColor);
        container1.setLocalTranslation(-3, 0, -4); 
        
        container1.addControl(new DragAndDropControl(new StackContainerListener(container1)));
        container1.addControl(new StackControl());        
        MouseEventControl.addListenersToSpatial(container1, 
                                                new HighlightListener(container1.material, 
                                                                      containerHighlight, 
                                                                      containerColor));
        dndRoot.attachChild(container1);
 
        // Setup a grid based container       
        container2 = new ContainerNode("container2", containerColor);
        container2.setSize(3, 3, 1);
        container2.setLocalTranslation(2f, -0.5f, -4);
        MouseEventControl.addListenersToSpatial(container2, 
                                                new HighlightListener(container2.material, 
                                                                      containerHighlight, 
                                                                      containerColor));
        container2.addControl(new GridControl(3));
        container2.addControl(new DragAndDropControl(new GridContainerListener(container2)));
        dndRoot.attachChild(container2);
        
        // Add some random items to our MVC stack 'model' control
        container1.getControl(StackControl.class).addChild(createItem());
        container1.getControl(StackControl.class).addChild(createItem());
        container1.getControl(StackControl.class).addChild(createItem());
        
        // Add some random items to our MVC grid 'model' control
        container2.getControl(GridControl.class).setCell(0, 0, createItem());  
        container2.getControl(GridControl.class).setCell(2, 1, createItem());  
    }
 
    private Spatial createItem() {
        Sphere sphere = new Sphere(12, 24, 1);
        Geometry geom = new Geometry("item", sphere);
        
        // Create a random color
        float r = (float)(Math.random() * 0.4 + 0.2);
        float g = (float)(Math.random() * 0.6 + 0.2);
        float b = (float)(Math.random() * 0.6 + 0.2);
        //ColorRGBA color = new ColorRGBA(r, g, b, 1);
        ColorRGBA color = GuiGlobals.getInstance().srgbaColor(r, g, b, 1);
        
        Material mat = GuiGlobals.getInstance().createMaterial(color, true).getMaterial();
        //mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Ambient", color);
        geom.setMaterial(mat);
        return geom;
    }
    
    @Override 
    protected void cleanup( Application app ) {
    }
    
    @Override
    protected void onEnable() {        
        ((DemoLauncher)getApplication()).getRootNode().attachChild(dndRoot);
    }
    
    @Override
    protected void onDisable() {
        dndRoot.removeFromParent();
    }
    
    @Override
    public void update( float tpf ) {
        //System.out.println("-------- update -----");
    }
 
    /**
     *  Just to encapsulate the visuals needed to have both a wireframe
     *  view but an actual box for picking.
     */   
    private class ContainerNode extends Node {
 
        private GuiMaterial material;
        private WireBox wire;
        private Geometry wireGeom;
        private Box box;
        private Geometry boxGeom;
        
        public ContainerNode( String name, ColorRGBA color ) {
            super(name);
            material = GuiGlobals.getInstance().createMaterial(containerColor, false);
                   
            wire = new WireBox(1, 1, 1);
            wireGeom = new Geometry(name + ".wire", wire);
            wireGeom.setMaterial(material.getMaterial());
            attachChild(wireGeom);
            
            box = new Box(1, 1, 1);
            boxGeom = new Geometry(name + ".box", box);
            boxGeom.setMaterial(material.getMaterial()); // might as well reuse it
            boxGeom.setCullHint(CullHint.Always); // invisible
            attachChild(boxGeom);
        }
        
        private void setSize( float x, float y, float z ) {
            wire.updatePositions(x, y, z);
            box.updateGeometry(Vector3f.ZERO, x, y, z);
            box.clearCollisionData();
            
            wireGeom.updateModelBound();
            boxGeom.updateModelBound();
        }
    }
    
    private class StackControl extends AbstractControl {
    
        private ContainerNode node;
        private List<Spatial> model = new ArrayList<>();
 
        public StackControl() {
        }
 
        @Override       
        public void setSpatial( Spatial s ) {
            super.setSpatial(s);
            this.node = (ContainerNode)s;
            updateLayout();
        }
    
        public void addChild( Spatial child ) {
            model.add(child);
            node.attachChild(child);
            updateLayout();
        }

        public void addChild( int slot, Spatial child ) {
            model.add(slot, child);
            node.attachChild(child);
            updateLayout();
        }
        
        public void removeChild( Spatial child ) {
            model.remove(child);
            if( child.getParent() == node ) {
                node.detachChild(child);
            }            
            updateLayout();
        }

        public Spatial removeChild( int index ) {
            Spatial result = model.remove(index);
            updateLayout();
            return result;
        }
 
        protected void updateLayout() {
            int count = Math.max(1, model.size());
            node.setSize(1, count, 1);
            float yStart = -(count - 1);
            for( Spatial s : model ) {
                s.setLocalTranslation(0, yStart, 0);
                yStart += 2;
            }   
        }
    
        @Override
        protected void controlUpdate( float tpf ) {
        }
    
        @Override
        protected void controlRender( RenderManager rm, ViewPort vp ) {
        }
    }

    private class GridControl extends AbstractControl {
 
        private ContainerNode node;
        private int gridSize;
        private Spatial[][] grid;
    
        public GridControl( int gridSize ) {
            this.gridSize = gridSize;
            this.grid = new Spatial[gridSize][gridSize];
        }
    
        @Override       
        public void setSpatial( Spatial s ) {
            super.setSpatial(s);
            this.node = (ContainerNode)s;
            updateLayout();
        }
        
        public Spatial getCell( int x, int y ) {
            return grid[x][y];
        }
        
        public void setCell( int x, int y, Spatial child ) {
            if( grid[x][y] != null ) {
                grid[x][y].removeFromParent();
            }
            grid[x][y] = child;
            if( child != null ) {
                node.attachChild(child);
            }
            updateLayout();
        }
 
        public Spatial removeCell( int x, int y ) {
            Spatial result = grid[x][y];
            grid[x][y] = null;
            if( result != null ) {
                updateLayout();
            }
            return result;
        }
        
        public void addChild( Spatial child ) {
            // Find the first valid cell
            for( int x = 0; x < gridSize; x++ ) {
                for( int y = 0; y < gridSize; y++ ) {
                    // just in case the child is already in the grid 
                    if( grid[x][y] == child ) {
                        return; 
                    }
                    if( grid[x][y] == null ) {
                        setCell(x, y, child);
                        return;
                    }
                }
            }            
        }
        
        public void removeChild( Spatial child ) {
            for( int x = 0; x < gridSize; x++ ) {
                for( int y = 0; y < gridSize; y++ ) {
                    if( child == grid[x][y] ) {
                        if( child.getParent() == node ) {
                            child.removeFromParent();
                        }
                        grid[x][y] = null;
                    }
                }
            }
            updateLayout();
        }

        protected void updateLayout() {
            node.setSize(gridSize, gridSize, 1);
            for( int x = 0; x < gridSize; x++ ) {
                for( int y = 0; y < gridSize; y++ ) {
                    Spatial child = grid[x][y];
                    if( child != null ) {
                        child.setLocalTranslation(-(gridSize - 1) + x * 2, (gridSize - 1) - y * 2, 0);
                    }
                }
            }
        }    
        
        @Override
        protected void controlUpdate( float tpf ) {
        }
    
        @Override
        protected void controlRender( RenderManager rm, ViewPort vp ) {
        }
    }
    
    /**
     *  Listens for enter/exit events and changes the color of
     *  the geometry accordingly.
     */   
    private class HighlightListener extends DefaultMouseListener {
        private GuiMaterial material;
        private ColorRGBA enterColor;
        private ColorRGBA exitColor;
        
        public HighlightListener( GuiMaterial material, ColorRGBA enterColor, ColorRGBA exitColor ) {
            this.material = material;
            this.enterColor = enterColor;
            this.exitColor = exitColor;
        }
        
        public void mouseEntered( MouseMotionEvent event, Spatial target, Spatial capture ) {
            material.setColor(enterColor);
        }

        public void mouseExited( MouseMotionEvent event, Spatial target, Spatial capture ) {
            material.setColor(exitColor);
        }        
    }

    private class StackContainerListener implements DragAndDropListener {    

        private Spatial container;

        public StackContainerListener( Spatial container ) {
            this.container = container;            
        }

        /**
         *  Returns the container 'model' (in the MVC sense) for this
         *  container listener.
         */
        public StackControl getModel() {
            return container.getControl(StackControl.class); 
        }

        private int getIndex( Vector3f world ) {
            Vector3f local = container.worldToLocal(world, null);
            
            // Calculate the index for that location
            float y = (getModel().model.size() + local.y) / 2;
            
            return (int)y;
        }
        
        public Draggable onDragDetected( DragEvent event ) {
        
            System.out.println("Stack.onDragDetected(" + event + ")");
 
            // Find the child we collided with
            StackControl control = getModel();
            
            // For now just use the first one
            if( control.model.isEmpty() ) {
                return null;
            }
            
            int index = getIndex(event.getCollision().getContactPoint());
 
            // The item is being dragged out of the container so remove it.
            Spatial item = control.removeChild(index);
            
            // Note: the item is still a child to the container so that we
            // can get its world translation and such below.  One of the slightly
            // strange things about using Spatials instead of some game object
            // as our container items.
            
            // We'll trust that the drag session won't lose it if we need
            // to put it back.                       
            event.getSession().set(DragSession.ITEM, item);
            
            // We'll keep track of the slot we took it from so we can stick
            // it back again if the drag is canceled. 
            event.getSession().set("stackIndex", index);
 
            // Clone the dragged item to use in our draggable and stick the
            // clone in the root at the same world location.
            Spatial drag = item.clone();
            drag.setLocalTranslation(item.getWorldTranslation());
            drag.setLocalRotation(item.getWorldRotation());
            getRoot().attachChild(drag);
                   
            // Now that we've got the world location of the item we can remove
            // it from the parent spatial since it is not really a child anymore.
            // We only left it so we could easily get its world location/rotation.
            item.removeFromParent();
             
            return new ColoredDraggable(event.getViewPort(), drag, event.getLocation());
        } 
    
        public void onDragEnter( DragEvent event ) {
            System.out.println("++++++++ Stack.onDragEnter(" + event + ")");
        }
          
        public void onDragExit( DragEvent event ) {
            System.out.println("-------- Stack.onDragExit(" + event + ")");
        }
        
        public void onDragOver( DragEvent event ) {
            System.out.println("Stack.onDragOver(" + event + ")");
            
            // Any location is valid on the stack
            event.getSession().setDragStatus(DragStatus.ValidTarget);
        }  
    
        // Target specific
        public void onDrop( DragEvent event ) {
            System.out.println("Stack.onDrop(" + event + ")");
 
            // Grab the payload we stored during drag start
            Spatial draggedItem = event.getSession().get(DragSession.ITEM, null);
 
            // Add the item to this stack
            getModel().addChild(draggedItem);
        }
    
        // Source specific  
        public void onDragDone( DragEvent event ) {
            System.out.println("Stack.onDragDone(" + event + ")");            
            
            // Check to see if drop target was null as this indicates
            // that the drag operation didn't finish and we need to 
            // put the item back.           
            if( event.getSession().getDropTarget() == null ) {
                                           
                // Grab the payload we stored during drag start
                Spatial draggedItem = event.getSession().get(DragSession.ITEM, null);
            
                // Grab the original slot of the item.  We tucked this away
                // during drag start just for this case.                
                int slot = event.getSession().get("stackIndex", 0);
                
                getModel().addChild(slot, draggedItem);
            } 
        }
    }  
    
    private class GridContainerListener implements DragAndDropListener {    

        private Spatial container;

        public GridContainerListener( Spatial container ) {
            this.container = container;            
        }

        /**
         *  Returns the container 'model' (in the MVC sense) for this
         *  container listener.
         */
        public GridControl getModel() {
            return container.getControl(GridControl.class); 
        }

        private Vector2f getCellLocation( Vector3f world ) {
            Vector3f local = container.worldToLocal(world, null);
            
            // Calculate the cell location
            float x = (3 + local.x) / 2;
            float y = (3 - local.y) / 2;
            
            // This will look a little off to the user towards the right edge because
            // clicking on the surface of the box in the center cell will actually project
            // into the sphere in the last column.  But it works for a demo.  We could
            // also have made a ray and done collideWith() on the childre but I wanted
            // to show model-cell interaction instead of picking.
            int xCell = (int)x;
            int yCell = (int)y; 
 
            return new Vector2f(xCell, yCell);
        }

        public Draggable onDragDetected( DragEvent event ) {
        
            System.out.println("Grid.onDragDetected(" + event + ")");
 
            // Find the child we collided with
            GridControl control = getModel();
 
            // See where we hit
            Vector2f hit = getCellLocation(event.getCollision().getContactPoint());
 
            // Remove the item from the grid if it exists.
            Spatial item = control.removeCell((int)hit.x, (int)hit.y);
            if( item != null ) {
                // Save the item in the session so the other containers (and ourselves)
                // know what we are dragging.
                event.getSession().set(DragSession.ITEM, item);
                
                // We'll keep track of the grid cell in case the drag is
                // canceled and we have to put it back. 
                event.getSession().set("gridLocation", hit);
                
                // Clone the dragged item to use in our draggable and stick the
                // clone in the root at the same world location.
                Spatial drag = item.clone();
                drag.setLocalTranslation(item.getWorldTranslation());
                drag.setLocalRotation(item.getWorldRotation());
                getRoot().attachChild(drag);
                
                // Now that we've got the world location of the item we can remove
                // it from the parent spatial since it is not really a child anymore.
                // We only left it so we could easily get its world location/rotation.
                item.removeFromParent(); 
                
                return new ColoredDraggable(event.getViewPort(), drag, event.getLocation());
            }       
            return null;
        } 
    
        public void onDragEnter( DragEvent event ) {
            System.out.println("+++++++ Grid.onDragEnter(" + event + ")");
        }
          
        public void onDragExit( DragEvent event ) {
            System.out.println("------- Grid.onDragExit(" + event + ")");
        }
        
        public void onDragOver( DragEvent event ) {
            System.out.println("Grid.onDragOver(" + event + ")");
            
            Vector2f hit = getCellLocation(event.getCollision().getContactPoint()); 
            Spatial item = getModel().getCell((int)hit.x, (int)hit.y);
            if( item == null ) {
                // An empty cell is a valid target
                event.getSession().setDragStatus(DragStatus.ValidTarget);
            } else {
                // A filled slot is not
                event.getSession().setDragStatus(DragStatus.InvalidTarget);
            }
        }  
    
        // Target specific
        public void onDrop( DragEvent event ) {
            System.out.println("Grid.onDrop(" + event + ")");
            
            Spatial draggedItem = event.getSession().get(DragSession.ITEM, null);                        

            Vector2f hit = getCellLocation(event.getCollision().getContactPoint());
            
            // One last check to see if the drop location is available 
            Spatial item = getModel().getCell((int)hit.x, (int)hit.y);
            if( item == null ) {
                // Then we can stick the new child right in
                getModel().setCell((int)hit.x, (int)hit.y, draggedItem);
            } else {
                // It wasn't really a valid drop
                event.getSession().setDragStatus(DragStatus.InvalidTarget);   
            }
        }
    
        // Source specific  
        public void onDragDone( DragEvent event ) {
            System.out.println("Grid.onDragDone(" + event + ")");
            
            DragSession session = event.getSession();
            
            // Check to see if drop target was null as this indicates
            // that the drag operation didn't finish and we need to 
            // put the item back.                       
            if( session.getDropTarget() == null ) {
            
                // Grab the payload we stored during drag start
                Spatial draggedItem = session.get(DragSession.ITEM, null);
                
                // Grab the original slot of the item.  We tucked this away
                // during drag start just for this case.                
                Vector2f slot = session.get("gridLocation", null);
                if( slot != null ) {
                    getModel().setCell((int)slot.x, (int)slot.y, draggedItem);
                } else {
                    System.out.println("Error, missing gridLocation for dragged item");
                    // This should not ever happen but if it does we'll at least
                    // try to deal with it
                    getModel().addChild(draggedItem);
                }  
            } 
        }
    }  
    
    private class ColoredDraggable extends DefaultDraggable {
 
        private Material originalMaterial;
        private ColorRGBA color = ColorRGBA.Blue; 
        private ColorRGBA none = ColorRGBA.Gray;
        private ColorRGBA invalid = ColorRGBA.Red;
        
        private Geometry geom;
        private Material mat;
    
        public ColoredDraggable( ViewPort view, Spatial spatial, Vector2f start ) {
            super(view, spatial, start);
 
            this.geom = (Geometry)spatial;
            this.originalMaterial = geom.getMaterial(); 
            this.mat = originalMaterial.clone();
        }
 
        protected void setColor( ColorRGBA color ) {
            mat.setColor("Diffuse", color);
            mat.setColor("Ambient", color);
            geom.setMaterial(mat);
        }
 
        @Override       
        public void updateDragStatus( DragStatus status ) {
            switch( status ) {
                case InvalidTarget:
                    setColor(invalid);
                    break;
                case ValidTarget:
                    //setColor(color);
                    geom.setMaterial(originalMaterial);
                    break;  
                case NoTarget:
                default:
                    setColor(none);                    
                    break;
            }
        }        
    }
}
