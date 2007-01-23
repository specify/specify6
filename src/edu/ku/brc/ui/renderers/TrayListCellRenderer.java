/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui.renderers;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

import edu.ku.brc.specify.ui.RepresentativeIconFactory;
import edu.ku.brc.specify.ui.RepresentativeTextFactory;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * An extension of {@link DefaultListCellRenderer} that can appropriately
 * handle objects that implement {@link FormDataObjIFace}. This classes
 * relies on a {@link RepresentativeIconFactory} to provide the icons.
 *
 * @author jstewart
 * @code_status Complete
 */
public class TrayListCellRenderer extends DefaultListCellRenderer
{
    /**
     * Creates a new instance with default configuration.
     */
    public TrayListCellRenderer()
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
        if(value instanceof FormDataObjIFace)
        {
            String text = RepresentativeTextFactory.getInstance().getString(value);
            l.setText(text);
            ImageIcon icon = RepresentativeIconFactory.getInstance().getIcon(value);
            l.setIcon(icon);
        }
       
        l.setHorizontalTextPosition(SwingConstants.CENTER);
        l.setHorizontalAlignment(SwingConstants.CENTER);
       
        return l;
    }
}
