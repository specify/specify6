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
 * An extension of {@link DefaultListCellRenderer} that delegates out the 
 * task of producing an appropriate text and icon representation of the rendered
 * objects.
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
        
        // remember, DefaultListCellRenderer extends JLabel,
        // so we can set any JLabel properties we want
        this.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.setHorizontalTextPosition(SwingConstants.CENTER);
        this.setHorizontalAlignment(SwingConstants.CENTER);
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
            // ask for the text representation of the object
            String text = RepresentativeTextFactory.getInstance().getString(value);
            l.setText(text);
            // ask for the icon representation of the object
            ImageIcon icon = RepresentativeIconFactory.getInstance().getIcon(value);
            l.setIcon(icon);
        }
        return l;
    }
}
