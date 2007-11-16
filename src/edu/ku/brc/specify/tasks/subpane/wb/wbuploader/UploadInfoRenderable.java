/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Vector;

import edu.ku.brc.af.core.expresssearch.TableNameRendererIFace;

public class UploadInfoRenderable implements TableNameRendererIFace, Comparable<UploadInfoRenderable>
{
    protected Class<?> tblClass;
    protected String title;
    protected Integer createdCnt;
    protected boolean showCreatedCnt = false;
    protected Vector<UploadTable> myTables;
    
    protected void refresh()
    {
        createdCnt = 0;
        for (UploadTable ut : myTables)
        {
            createdCnt += ut.getUploadedKeys().size();
        }
    }
    
    public UploadInfoRenderable(final UploadTable ut)
    {
        this.tblClass = ut.getTblClass();
        this.title = ut.getTable().getTableInfo().getTitle();
        this.createdCnt = 0;
        myTables = new Vector<UploadTable>();
        myTables.add(ut);
    }
    
    public void addTable(final UploadTable ut)
    {
        myTables.add(ut);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.TableNameRendererIFace#getIconName()
     */
    public String getIconName()
    {
        return tblClass.getSimpleName();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.TableNameRendererIFace#getTitle()
     */
    public String getTitle()
    {
        if (!showCreatedCnt)
        {
            return title;
        }
        return title + " (" + createdCnt + " objects created)";
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(UploadInfoRenderable obj)
    {
        return title.compareTo(obj.getTitle());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj.getClass().equals(UploadInfoRenderable.class))
        {
            return false;
        }
        return compareTo((UploadInfoRenderable)obj) == 0;
    }
    
    /**
     * @param showCreatedCnt the showCreatedCnt to set
     */
    public void setShowCreatedCnt(boolean showCreatedCnt)
    {
        this.showCreatedCnt = showCreatedCnt;
    }
    
    public String getTableName()
    {
        return tblClass.getSimpleName();
    }
}
