/*
 * $Id$
 *
 * Copyright (c) 2025, Simsilica, LLC
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

import java.util.*;

import org.slf4j.*;

import com.simsilica.lemur.core.VersionedReference;

/**
 *  Holds a list of CheckboxModels and makes sure that only one at a time
 *  is selected.
 *
 *  @author    Paul Speed
 */
public class CheckboxModelGroup {
    static Logger log = LoggerFactory.getLogger(CheckboxModelGroup.class);

    private Map<CheckboxModel, ModelEntry> models = new LinkedHashMap<>();
    private ModelEntry activeEntry;
    private boolean invalid;

    public CheckboxModelGroup() {
    }

    public CheckboxModelGroup( CheckboxModel... models ) {
        this(Arrays.asList(models));
    }

    public CheckboxModelGroup( List<CheckboxModel> models ) {
        for( CheckboxModel m : models ) {
            addModel(m);
        }
    }

    public void addModel( CheckboxModel model ) {
        // If there is already an active model and it is not the one supplied then make
        // sure the specified check is cleared
        if( activeEntry != null && activeEntry.model != model ) {
            model.setChecked(false);
        }
        models.put(model, new ModelEntry(model));
        invalid = true;
    }

    public void removeModel( CheckboxModel model ) {
        models.remove(model);
        if( activeEntry == model ) {
            // Clear the active model... the next check turned on will get to be active
            setActiveEntry(null);
        }
    }

    public CheckboxModel getSelectedModel() {
        return activeEntry == null ? null : activeEntry.model;
    }

    public void setSelectedModel( CheckboxModel model ) {
        ModelEntry entry = models.get(model);
        if( entry == null ) {
            throw new IllegalArgumentException("Model is not managed by this group:" + model);
        }
        setActiveEntry(entry);
    }

    public boolean update() {
        if( invalid ) {
            // When new models are added to a group with no active models
            // then we will pick the first one
            selectFirst();
            invalid = false;
        }

        // Make sure that all of the versioned references are up to date
        // but keep track of the first one that was changed to true... that will
        // be our new active model.
        ModelEntry first = null;
        boolean clear = false;
        for( ModelEntry entry : models.values() ) {
            if( entry.ref.update() ) {
                if( entry.model.isChecked() ) {
                    if( first == null ) {
                        first = entry;
                    }
                } else if( entry == activeEntry ) {
                    clear = true;
                }
            }
        }
        if( first != null ) {
            // A new checkbox was turned on, it is now the new active omdel
            setActiveEntry(first);
            return true;
        } else if( clear ) {
            // There were no new 'true' models but our active model was cleared
            setActiveEntry(null);
            return true;
        }
        return false;
    }

    protected void selectFirst() {
        for( ModelEntry entry : models.values() ) {
            if( entry.model.isChecked() ) {
                setActiveEntry(entry);
                break;
            }
        }
    }

    protected void setActiveEntry( ModelEntry activeEntry ) {
        this.activeEntry = activeEntry;
        if( activeEntry == null ) {
            return;
        }
        for( ModelEntry entry : models.values() ) {
            if( entry != activeEntry ) {
                // Turn all but the active one off
                entry.model.setChecked(false);
            }
        }
    }

    protected class ModelEntry {
        private CheckboxModel model;
        private VersionedReference<Boolean> ref;

        public ModelEntry( CheckboxModel model ) {
            this.model = model;
            this.ref = model.createReference();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[model:" + model + "]";
        }
    }
}
