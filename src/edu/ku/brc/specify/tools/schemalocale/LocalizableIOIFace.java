/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.io.File;
import java.util.Locale;
import java.util.Vector;


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
    public abstract boolean load();
    
    public abstract boolean didModelChangeDuringLoad();
    
    public abstract LocalizableContainerIFace getContainer(LocalizableJListItem item);
    
    public abstract LocalizableItemIFace getItem(LocalizableContainerIFace container, LocalizableJListItem item);
    
    public abstract Vector<LocalizableJListItem> getContainerDisplayItems();
    
    public abstract Vector<LocalizableJListItem> getDisplayItems(LocalizableJListItem container);
    
    public abstract LocalizableItemIFace realize(LocalizableItemIFace item);
    
    public abstract boolean isLocaleInUse(final Locale locale);
    
    public abstract Vector<Locale> getLocalesInUse();
    
    public abstract void copyLocale(Locale src, Locale dst);
    
    public abstract boolean save();
    
    public abstract boolean export(File expportFile);
    
    public boolean createResourceFiles();
    
    
}
