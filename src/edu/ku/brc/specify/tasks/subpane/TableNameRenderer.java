/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import edu.ku.brc.ui.IconManager;

/**
 * Renderer for the Table List from DBTableIdMgr.
 *  
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 23, 2007
 *
 */
public class TableNameRenderer extends DefaultListCellRenderer 
{
    protected IconManager.IconSize iconSize;
    
    public TableNameRenderer(final IconManager.IconSize iconSize) 
    {
        // Don't paint behind the component
            this.setOpaque(false);
            this.iconSize = iconSize;
        }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,   // value to display
                                                  int index,      // cell index
                                                  boolean iss,    // is the cell selected
                                                  boolean chf)    // the list and the cell have the focus
    {
        super.getListCellRendererComponent(list, value, index, iss, chf);

        TableNameRendererIFace ti = (TableNameRendererIFace)value;
        ImageIcon icon = IconManager.getIcon(ti.getIconName(), iconSize);
        setIcon(icon != null ? icon : IconManager.getIcon("BlankIcon", iconSize));
        
        if (iss) {
            setOpaque(true);
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);

        } else {
            this.setOpaque(false);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setText(ti.getTitle());
        return this;
    }
    
    public interface TableNameRendererIFace
    {
        public String getIconName();
        
        public String getTitle();
        
    }
}