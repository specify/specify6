/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Vector;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadMappingDefTreeLevels
{
    protected final Vector<TreeMapElements> levels;
    
    public UploadMappingDefTreeLevels(Vector<Vector<TreeMapElement>> levels) throws Exception
    {
        this.levels = new Vector<TreeMapElements>();
        for (Vector<TreeMapElement> level : levels)
        {
            this.levels.add(new TreeMapElements(level));
        }
    }
    
    public int size()
    {
        return levels != null ? levels.size() : 0;
    }
    
    public TreeMapElements get(int index)
    {
        return levels.get(index);
    }
}
