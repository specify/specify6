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
    /**
     * @return
     */
    public String getName();
    
    /**
     * @param name
     */
    public void setName(String name);
    
    /**
     * @return
     */
    public String getType();
    
    /**
     * @param type
     */
    public void setType(String type);
    
    /**
     * @return
     */
    public Boolean getIsHidden();
    
    /**
     * @param isHidden the isHidden to set
     */
    public void setIsHidden(Boolean isHidden);

    /**
     * @param str
     */
    public void addDesc(LocalizableStrIFace str);
    
    /**
     * @param str
     */
    public void removeDesc(LocalizableStrIFace str);
    
    /**
     * @param descs
     */
    public void fillDescs(List<LocalizableStrIFace> descs);

    /**
     * @param str
     */
    public void addName(LocalizableStrIFace str);
    
    /**
     * @param str
     */
    public void removeName(LocalizableStrIFace str);
    
    /**
     * @param names
     */
    public void fillNames(List<LocalizableStrIFace> names);
    
    
}
