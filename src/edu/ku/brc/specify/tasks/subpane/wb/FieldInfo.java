/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import javax.swing.ImageIcon;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.tasks.subpane.TableFieldPair;
import edu.ku.brc.ui.IconManager;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 6, 2007
 *
 */
public class FieldInfo extends TableFieldPair implements TableListItemIFace
{
    protected static ImageIcon checkMark   = IconManager.getIcon("Checkmark", IconManager.IconSize.Std16);
    
    public FieldInfo(DBTableIdMgr.TableInfo tableinfo, DBTableIdMgr.FieldInfo fieldInfo)
    {
        super(tableinfo, fieldInfo);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#getIcon()
     */
    public ImageIcon getIcon()
    {
        return checkMark;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#getText()
     */
    public String getText()
    {
        return fieldInfo.getColumn();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isChecked()
     */
    public boolean isChecked()
    {
        return isInUse;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isExpandable()
     */
    public boolean isExpandable()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isExpanded()
     */
    public boolean isExpanded()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#setChecked(boolean)
     */
    public void setChecked(boolean checked)
    {
       setInUse(checked);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#setExpanded(boolean)
     */
    public void setExpanded(boolean expand)
    {
        // no-op
    }

}
