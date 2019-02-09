/*
 * $Id$
 *
 * Copyright (c) 2014 jMonkeyEngine
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

package com.simsilica.lemur.props;

import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.CheckboxModel;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.DefaultCheckboxModel;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.RangedValueModel;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;



/**
 *  A panel that supports adding reflected property and
 *  field access directly to it, presenting a table of
 *  value editors to the user.
 *
 *  @author    Paul Speed
 */ 
public class PropertyPanel extends Panel  
                           implements VersionedObject<PropertyPanel> {
                           
    public static final ElementId ELEMENT_ID = new ElementId("properties");                            
                           
    private BorderLayout layout;
    private Container container;
    private List<AbstractProperty> properties = new ArrayList<AbstractProperty>();
    private AbstractProperty[] propertyArray;

    private Object enabledBean;
    private Checkbox enabledCheckbox;
    private CheckboxModel enabledModel = new EnabledCheckboxModel();
    private PropertyDescriptor enabledProperty;
    
    private long version;
    
    public PropertyPanel( String style ) {
        this(true, ELEMENT_ID, style);
    }

    public PropertyPanel( ElementId elementId, String style ) {
        this(true, elementId, style);
    }
    
    protected PropertyPanel( boolean applyStyles, ElementId elementId, String style ) {
        super(false, elementId, style);

        this.layout = new BorderLayout();
        getControl(GuiControl.class).setLayout(layout);
 
        this.container = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Last), 
                                       elementId.child("container"), style);
        layout.addChild(container, BorderLayout.Position.Center);       

        
        if( applyStyles ) {
            Styles styles = GuiGlobals.getInstance().getStyles();
            styles.applyStyles(this, elementId, style);
        }
    }
 
    protected AbstractProperty[] getArray() {
        if( propertyArray == null ) {
            propertyArray = new AbstractProperty[properties.size()];
            propertyArray = properties.toArray(propertyArray);
        }
        return propertyArray;
    }

    /**
     *  Call to force all property editors to refresh their values
     *  from the source object.  This is the only way to get the 
     *  UI to update if the values have been changed outside of the
     *  UI.
     */
    public void refresh() {
        for( AbstractProperty p : getArray() ) {
            p.refresh();
        }
    }

    /**
     *  Returns the raw container within which the property
     *  editors are added.
     */
    public Container getContainer() {
        return container;
    } 
    
    protected PropertyDescriptor findProperty( Object bean, String propertyName ) {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
 
            for( PropertyDescriptor pd : info.getPropertyDescriptors() ) {
                if( pd.getName().equals(propertyName) ) {
                    if( pd.getReadMethod() == null ) {
                        throw new RuntimeException("Property has no read method:" + propertyName + " on:" + bean.getClass());
                    }
                    if( pd.getWriteMethod() == null ) {
                        throw new RuntimeException("Property has no write method:" + propertyName + " on:" + bean.getClass());
                    }
                    return pd;
                }
            }
            throw new RuntimeException("No suche property:" + propertyName + " on:" + bean.getClass());
        } catch( IntrospectionException e ) {
            throw new RuntimeException("Error introspecting object", e);
        }
        //return null;        
    }
    
    protected Field findField( Object bean, String fieldName ) {
        try {
            return bean.getClass().getField(fieldName);
        } catch( NoSuchFieldException ex ) {
            throw new RuntimeException("Error inspecting object", ex);
        } catch( SecurityException ex ) {
            throw new RuntimeException("Error inspecting object", ex);
        }
    }
 
    protected void addProperty( AbstractProperty p ) {
        p.initialize(container);
        properties.add(p);
        propertyArray = null;
    } 
 
    protected void resetEnabled() {
        if( enabledCheckbox == null ) {
            // Perform setup
            enabledCheckbox = new Checkbox("", enabledModel, getElementId().child("enabled.checkbox"), getStyle());            
            enabledModel.setChecked((Boolean)getPropertyValue(enabledProperty, enabledBean));                         
        } else {
            setPropertyValue(enabledProperty, enabledBean, enabledModel.isChecked());
        }
    }
 
    /**
     *  Sets a boolean property that is presented as a panel-global 
     *  "enabled" state.  This is a way of exposing a boolean property
     *  as a checkbox model that can be used in an outer UI.  Otherwise,
     *  for direct boolean property editing just use the normal addBoolean**()
     *  methods.
     */
    public CheckboxModel setEnabledProperty( Object bean, String property ) {
        enabledProperty = findProperty(bean, property);
        enabledBean = bean;
        resetEnabled();
        return enabledModel;        
    }
 
    /**
     *  Returns the model that is used for enabled/disabled state.  Note:
     *  this state currently has no effect on this component itself but is
     *  simply a way to expose a single boolean property to the caller as
     *  a checkbox model.
     */   
    public CheckboxModel getEnabledModel() {
        return enabledModel;
    }
 
    public Property<Boolean> addBooleanProperty( String name, Object bean, String property ) {
        BooleanProperty p = new BooleanProperty(name, new PropertyAccess(bean, property));
        addProperty(p);
        return p;
    }
    
    public Property<Float> addFloatProperty( String name, Object bean, String property, float min, float max, float step ) {
        FloatProperty p = new FloatProperty(name, new PropertyAccess(bean, property), min, max, step);
        addProperty(p);
        return p;
    }

    public Property<Double> addDoubleProperty( String name, Object bean, String property, double min, double max, double step ) {
        DoubleProperty p = new DoubleProperty(name, new PropertyAccess(bean, property), min, max, step);
        addProperty(p);
        return p;
    }

    public Property<Integer> addIntProperty( String name, Object bean, String proprety, int min, int max, int step ) {
        IntProperty p = new IntProperty(name, new PropertyAccess(bean, proprety), min, max, step);      
        addProperty(p);
        return p;
    }

    public Property<Enum> addEnumProperty( String name, Object bean, String property ) {
        EnumProperty p = new EnumProperty(name, new PropertyAccess(bean, property));
        addProperty(p);
        return p;
    }
    
    public Property<Boolean> addBooleanField( String name, Object bean, String field ) {
        BooleanProperty p = new BooleanProperty(name, new FieldAccess(bean, field));
        addProperty(p);
        return p;
    }
    
    public Property<Float> addFloatField( String name, Object bean, String field, float min, float max, float step ) {
        FloatProperty p = new FloatProperty(name, new FieldAccess(bean, field), min, max, step);
        addProperty(p);
        return p;
    }
    
    public Property<Double> addDoubleField( String name, Object bean, String field, double min, double max, double step ) {
        DoubleProperty p = new DoubleProperty(name, new FieldAccess(bean, field), min, max, step);
        addProperty(p);
        return p;
    }

    public Property<Integer> addIntField( String name, Object bean, String field, int min, int max, int step ) {
        IntProperty p = new IntProperty(name, new FieldAccess(bean, field), min, max, step);
        addProperty(p);
        return p;
    }
    
    public Property<Enum> addEnumField( String name, Object bean, String field ) {
        EnumProperty p = new EnumProperty(name, new FieldAccess(bean, field));
        addProperty(p);
        return p;
    }
    
    @Override
    public void updateLogicalState( float tpf ) {
        super.updateLogicalState(tpf);
        for( AbstractProperty p : getArray() ) {
            p.update();
        }
    }

    protected <T> T getPropertyValue( PropertyDescriptor pd, Object bean ) {
        try {
            return (T)pd.getReadMethod().invoke(bean);
        } catch( IllegalAccessException e ) {
            throw new RuntimeException("Error getting value", e);
        } catch( InvocationTargetException e ) {
            throw new RuntimeException("Error getting value", e);
        }
    }

    protected void setPropertyValue( PropertyDescriptor pd, Object bean, Object value ) {
        try {
            pd.getWriteMethod().invoke(bean, new Object[] { value });
            version++;
        } catch( IllegalAccessException e ) {
            throw new RuntimeException("Error setting value", e);
        } catch( InvocationTargetException e ) {
            throw new RuntimeException("Error setting value", e);
        }
    }
 
    protected <T> T getFieldValue( Field field, Object bean ) {
        try {
            return (T)field.get(bean);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error getting value", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error getting value", e);
        }
    }
    
    protected void setFieldValue( Field field, Object bean, Object value ) {
        try {
            field.set(bean, value);
            version++;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error setting value", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error setting value", e);
        }
    }    
 
    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public PropertyPanel getObject() {
        return this;
    }

    @Override
    public VersionedReference<PropertyPanel> createReference() {
        return new VersionedReference<PropertyPanel>(this);
    }
 
    protected class EnabledCheckboxModel extends DefaultCheckboxModel {
    
        @Override
        public void setChecked( boolean checked ) {
            if( isChecked() == checked ) {
                return;
            }
            super.setChecked(checked);
            resetEnabled();
        }
    }
 
    public interface Property<T> {
        public void setValue( T value );
        public T getValue();
    }
    
    protected interface Access<T> {
        public void setValue( T value );
        public T getValue();
        public Class getType();
    }
 
    protected class PropertyAccess<T> implements Access<T> {
        private Object bean;
        private PropertyDescriptor pd;
        
        public PropertyAccess( Object bean, String propertyName ) {
            this.bean = bean;
            this.pd = findProperty(bean, propertyName);
            if( this.pd == null ) {
                throw new IllegalArgumentException("Property not found:" + propertyName + " on:" + bean);
            }
        }
                    
        @Override
        public void setValue( T value ) {
            setPropertyValue(pd, bean, value);
        }
        
        @Override
        public T getValue() {
            return getPropertyValue(pd, bean);
        }

        @Override        
        public Class getType() {
            return pd.getPropertyType();
        }
    }
 
    protected class FieldAccess<T> implements Access<T> {
        private Object bean;
        private Field fd;
        
        public FieldAccess( Object bean, String fieldName ) {
            this.bean = bean;
            this.fd = findField(bean, fieldName);
            if( this.fd == null ) {
                throw new IllegalArgumentException("Field not found:" + fieldName + " on:" + bean);
            }
        }

        @Override
        public void setValue( T value ) {
            setFieldValue(fd, bean, value);
        }

        @Override
        public T getValue() {
            return getFieldValue(fd, bean);
        }
        
        @Override        
        public Class getType() {
            return fd.getType();
        }
    }
    
    protected abstract class AbstractProperty<T> implements Property<T> {
        private String name;
        private Access<T> access;
        
        protected AbstractProperty( String name, Access<T> access ) {            
            this.name = name;
            this.access = access;
        }
 
        protected String getDisplayName() {
            return name;
        }
 
        @Override
        public void setValue( T value ) {
            access.setValue(value);
        }
        
        @Override
        public T getValue() {
            return access.getValue();
        }
 
        public abstract void initialize( Container container );
    
        public abstract void update();
        
        public abstract void refresh(); 
    }    
    
    protected class BooleanProperty extends AbstractProperty<Boolean> {
        private Label label;
        private Checkbox check;
        private VersionedReference<Boolean> value;
        
        public BooleanProperty( String name, Access<Boolean> access ) {
            super(name, access);
        }

        @Override
        public void initialize( Container container ) {
            label = new Label(getDisplayName() + ":", getElementId().child("boolean.label"), getStyle());                       
            label.setTextHAlignment(HAlignment.Right); 
            check = new Checkbox("", getElementId().child("boolean.checkbox"), getStyle());
            check.setChecked(getValue());
            value = check.getModel().createReference();
            container.addChild(label);
            container.addChild(check, 1); 
        }

        @Override
        public void update() {
            if( value.update() ) {
                super.setValue(check.isChecked());
            }
        }
        
        @Override
        public void refresh() {
            check.setChecked(getValue());
        }
    }
    
    protected class FloatProperty extends AbstractProperty<Float> {
        private Label label;
        private Label valueText;
        private Slider slider;
        private RangedValueModel model;
        private float step;        
        private VersionedReference<Double> value;
        private String format = "%14.3f";
        
        public FloatProperty( String name, Access<Float> access, float min, float max, float step ) {
            super(name, access);
 
            this.model = new DefaultRangedValueModel( min, max, 0 );
            this.step = step;
        }
        
        @Override
        public void initialize( Container container ) {
            label = new Label(getDisplayName() + ":", getElementId().child("float.label"), getStyle());
            label.setTextHAlignment(HAlignment.Right); 
            slider = new Slider( model, Axis.X, getElementId().child("float.slider"), getStyle());
            slider.setDelta(step);
            //Float current = getValue();
            //model.setValue(current);
            refresh();
            valueText = new Label("", getElementId().child("value.label"), getStyle());
            updateText();
                        
            value = slider.getModel().createReference();
            container.addChild(label);
            container.addChild(valueText, 1); 
            container.addChild(slider, 2); 
        }

        protected void updateText() {
            valueText.setText(String.format(format, model.getValue()));
        }

        @Override
        public void update() {
            if( value.update() ) {
                super.setValue((float)model.getValue());
                updateText();
            }
        }
        
        @Override
        public void refresh() {
            Float current = getValue();
            model.setValue(current);
        }
    }
 
    protected class DoubleProperty extends AbstractProperty<Double> {
        private Label label;
        private Label valueText;
        private Slider slider;
        private RangedValueModel model;
        private double step;        
        private VersionedReference<Double> value;
        private String format = "%14.3f";
        
        public DoubleProperty( String name, Access<Double> access, double min, double max, double step ) {
            super(name, access);
 
            this.model = new DefaultRangedValueModel( min, max, 0 );
            this.step = step;
        }
        
        @Override
        public void initialize( Container container ) {
            label = new Label(getDisplayName() + ":", getElementId().child("double.label"), getStyle());
            label.setTextHAlignment(HAlignment.Right); 
            slider = new Slider( model, Axis.X, getElementId().child("double.slider"), getStyle());
            slider.setDelta(step);
            //Double current = getValue();
            //model.setValue(current);
            refresh();
            valueText = new Label("", getElementId().child("value.label"), getStyle());
            updateText();
                        
            value = slider.getModel().createReference();
            container.addChild(label);
            container.addChild(valueText, 1); 
            container.addChild(slider, 2); 
        }

        protected void updateText() {
            valueText.setText(String.format(format, model.getValue()));
        }

        @Override
        public void update() {
            if( value.update() ) {
                super.setValue((double)model.getValue());
                updateText();
            }
        }
        
        @Override
        public void refresh() {
            Double current = getValue();
            model.setValue(current);
        }
    }
    
    protected class IntProperty extends AbstractProperty<Integer> {
        private Label label;
        private Label valueText;
        private Slider slider;
        private RangedValueModel model;
        private int step;        
        private VersionedReference<Double> value;
        private String format = "%14d";
        
        public IntProperty( String name, Access<Integer> access, int min, int max, int step ) {
            super(name, access);
 
            this.model = new DefaultRangedValueModel( min, max, 0 );
            this.step = step;
        }
        
        @Override
        public void initialize( Container container ) {
            label = new Label(getDisplayName() + ":", getElementId().child("int.label"), getStyle());
            label.setTextHAlignment(HAlignment.Right); 
            slider = new Slider( model, Axis.X, getElementId().child("int.slider"), getStyle());
            slider.setDelta(step);
            //Integer current = getValue();
            //model.setValue(current);
            refresh();
            valueText = new Label("", getElementId().child("value.label"), getStyle());
            updateText();
                        
            value = slider.getModel().createReference();
            container.addChild(label);
            container.addChild(valueText, 1); 
            container.addChild(slider, 2); 
        }

        protected void updateText() {
            valueText.setText(String.format(format, Math.round(model.getValue())));        
        }
        
        @Override
        public void update() {
            if( value.update() ) {
                super.setValue((int)model.getValue());
                updateText();
            }
        }
        
        @Override
        public void refresh() {
            Integer current = getValue();
            model.setValue(current);
        }
    }
    
    protected class EnumProperty extends AbstractProperty<Enum> {
        private Label label;
        private Label valueText;
        private Slider slider;
        private RangedValueModel model;
        private VersionedReference<Double> value;
        private Class type;
        private Enum[] values;
        
        public EnumProperty( String name, Access<Enum> access ) {
            super(name, access);
 
            this.type = access.getType();
            this.values = (Enum[])type.getEnumConstants();  
 
            this.model = new DefaultRangedValueModel( 0, values.length - 1, 0 );
        }
        
        @Override
        public void initialize( Container container ) {
            label = new Label(getDisplayName() + ":", getElementId().child("enum.label"), getStyle());
            label.setTextHAlignment(HAlignment.Right); 
            slider = new Slider( model, Axis.X, getElementId().child("enum.slider"), getStyle());
            refresh();
            valueText = new Label("", getElementId().child("value.label"), getStyle());
            updateText();
                        
            value = slider.getModel().createReference();
            container.addChild(label);
            container.addChild(valueText, 1); 
            container.addChild(slider, 2); 
        }

        protected void updateText() {
            int index = (int)model.getValue();
            valueText.setText(String.valueOf(values[index]));        
        }
        
        @Override
        public void update() {
            if( value.update() ) {
                int i = (int)model.getValue();                
                super.setValue(values[i]);
                updateText();
            }
        }
        
        @Override
        public void refresh() {
            Enum current = getValue();
            int index = current.ordinal();
            model.setValue(index);
        }
    }
}
