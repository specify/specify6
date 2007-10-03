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

import java.util.List;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 28, 2007
 *
 */
public interface LocalizableItemIFace
{
    public String getName();
    public void setName(String name);
    
    public String getType();
    public void setType(String type);
    
    public void addDesc(LocalizableStrIFace str);
    public void removeDesc(LocalizableStrIFace str);
    public void fillDescs(List<LocalizableStrIFace> descs);

    public void addName(LocalizableStrIFace str);
    public void removeName(LocalizableStrIFace str);
    public void fillNames(List<LocalizableStrIFace> names);
}
