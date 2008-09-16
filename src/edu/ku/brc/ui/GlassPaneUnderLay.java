/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui;

import java.awt.Dimension;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *An interface to allow more flexible Glass Pane behavior.
 *
 */
public interface GlassPaneUnderLay
{
    /**
     * @return the dimensions that will be covered by the GlassPane
     */
    Dimension getUnderLaySize();
    
    /**
     * Must be called when a GlassPane has been written over the GlassPaneUnderLay
     */
    void cover();
    
    /**
     * Must be called when a GlassPane is cleared from over the GlassPaneUnderLay
     */
    void unCover();
}
