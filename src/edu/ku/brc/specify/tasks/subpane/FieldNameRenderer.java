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
 * Renderer for the Field List.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 23, 2007
 *
 */
public class FieldNameRenderer extends DefaultListCellRenderer 
{
    protected ImageIcon checkMark;
    protected ImageIcon blankIcon;
    
    public FieldNameRenderer(IconManager.IconSize iconSize) 
    {
        // Don't paint behind the component
        this.setOpaque(false);
        checkMark   = IconManager.getIcon("Checkmark", iconSize);
        blankIcon   = IconManager.getIcon("BlankIcon", iconSize);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,   // value to display
                                                  int index,      // cell index
                                                  boolean iss,    // is the cell selected
                                                  boolean chf)    // the list and the cell have the focus
    {
        super.getListCellRendererComponent(list, value, index, iss, chf);

        TableFieldPair tblField = (TableFieldPair)value;
        setIcon(tblField.isInUse() ? checkMark : blankIcon);
        
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

        setText(tblField.getTitle());
        return this;
    }

}