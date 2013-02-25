/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.ui.renderers;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.specify.ui.RepresentativeIconFactory;
import edu.ku.brc.specify.ui.RepresentativeTextFactory;

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
    private ChangeListener listener;
    
    /**
     * Creates a new instance with default configuration.
     */
    public TrayListCellRenderer(final ChangeListener listener)
    {
        super();
        
        this.listener = listener;
        
        // lookup, DefaultListCellRenderer extends JLabel,
        // so we can set any JLabel properties we want
        this.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.setHorizontalTextPosition(SwingConstants.CENTER);
        this.setHorizontalAlignment(SwingConstants.CENTER);
        
        //setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 4));
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
            l.setToolTipText(text);
            // ask for the icon representation of the object
            ImageIcon icon = RepresentativeIconFactory.getInstance().getIcon(value, null);
            l.setIcon(icon);
        }
        return l;
    }
}
