/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

/**
 * An extension of {@link DefaultListCellRenderer} that can appropriately
 * handle objects that implement {@link Trayable}.
 *
 * @author jstewart
 * @code_status Complete
 */
public class TrayableListCellRenderer extends DefaultListCellRenderer
{
    /**
     * Creates a new instance with default configuration.
     */
    public TrayableListCellRenderer()
    {
        super();
        this.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    /* (non-Javadoc)
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(value instanceof Trayable)
        {
            Trayable t = (Trayable)value;
            l.setIcon(t.getIcon());
            l.setText(t.getName());
        }
        return l;
    }

}
