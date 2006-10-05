/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui.renderers;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableInfo;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * An extension of {@link DefaultListCellRenderer} that can appropriately
 * handle objects that implement {@link FormDataObjIFace}.
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
            FormDataObjIFace dataObj = (FormDataObjIFace)value;
            l.setText(dataObj.getIdentityTitle());
            int tableId = dataObj.getTableId();
            TableInfo tableInfo = DBTableIdMgr.lookupInfoById(tableId);
            Icon icon = tableInfo.getIcon(IconSize.Std16);
            l.setIcon(icon);
        }
        return l;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        Dimension superSize = super.getPreferredSize();
        superSize.width+=25;
        return superSize;
    }
}
