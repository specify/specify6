/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tools.schemalocale;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.PickList;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 28, 2007
 *
 */
public interface LocalizableIOIFace
{
    /**
     * @return
     */
    public abstract boolean load();
    
    /**
     * @return
     */
    public abstract boolean didModelChangeDuringLoad();
    
    /**
     * @param item
     * @param l
     */
    public abstract void getContainer(LocalizableJListItem item, LocalizableIOIFaceListener l);
    
    /**
     * @param container
     */
    public abstract void containerChanged(LocalizableContainerIFace container);
    
    /**
     * @param container
     * @param item
     * @return
     */
    public abstract LocalizableItemIFace getItem(LocalizableContainerIFace container, LocalizableJListItem item);
    
    /**
     * @return
     */
    public abstract Vector<LocalizableJListItem> getContainerDisplayItems();
    
    /**
     * @param container
     * @return
     */
    public abstract Vector<LocalizableJListItem> getDisplayItems(LocalizableJListItem container);
    
    /**
     * @param locale
     * @return
     */
    public abstract boolean isLocaleInUse(final Locale locale);
    
    /**
     * @return
     */
    public abstract Vector<Locale> getLocalesInUse();
    
    /**
     * @param src
     * @param dst
     */
    public abstract void copyLocale(Locale src, Locale dst);
    
    /**
     * @return true on save, false on failure
     */
    public abstract boolean save();
    
    /**
     * @param expportFile
     * @return
     */
    public abstract boolean exportToDirectory(File expportFile);
    
    /**
     * @return
     */
    public abstract boolean createResourceFiles();
    
    /**
     * The implementor MUST return all the 'common' and all the discipline specific PickLists merged
     * where the Discipline PickLists override the common.
     * @param disciplineName the name of the discipline
     * @return
     */
    public abstract List<PickList> getPickLists(String disciplineName);
    
    /**
     * @return true if the PickLists can be created/updates/deleted.
     */
    public abstract boolean hasUpdatablePickLists();
    
    
    /**
     * @return whether the application tables should be included to be configured.
     */
    public abstract boolean shouldIncludeAppTables();
}
