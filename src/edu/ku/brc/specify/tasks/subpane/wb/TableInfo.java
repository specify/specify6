/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Vector;

import javax.swing.ImageIcon;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.ui.IconManager;

public class TableInfo implements TableListItemIFace, Comparable<TableInfo>
{
    protected DBTableIdMgr.TableInfo tableInfo;
    protected Vector<FieldInfo>      fieldItems;
    protected boolean                isExpanded;
    protected ImageIcon              icon;
    
    public TableInfo(final DBTableIdMgr.TableInfo tableInfo, IconManager.IconSize size)
    {
        this.tableInfo = tableInfo;
        
        icon = IconManager.getIcon(tableInfo.getObjTitle(), size);
    }

    /**
     * @return the fieldItems
     */
    public Vector<FieldInfo> getFieldItems()
    {
        return fieldItems;
    }

    /**
     * @param fieldItems the fieldItems to set
     */
    public void setFieldItems(Vector<FieldInfo> fieldItems)
    {
        this.fieldItems = fieldItems;
    }

    /**
     * @return the tableInfo
     */
    public DBTableIdMgr.TableInfo getTableInfo()
    {
        return tableInfo;
    }

    public String getIconName()
    {
        return tableInfo.getClassObj().getSimpleName();
    }
    
    /**
     * @param icon the icon to set
     */
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TableInfo obj)
    {
        return tableInfo.toString().compareTo(obj.tableInfo.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#getIcon()
     */
    public ImageIcon getIcon()
    {
        return icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#getText()
     */
    public String getText()
    {
        return tableInfo.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isChecked()
     */
    public boolean isChecked()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isExpandable()
     */
    public boolean isExpandable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isExpanded()
     */
    public boolean isExpanded()
    {
        return isExpanded;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#setChecked(boolean)
     */
    public void setChecked(boolean checked)
    {
        // no-op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#setExpanded(boolean)
     */
    public void setExpanded(boolean expand)
    {
        isExpanded = expand;
    }
    
    
}

