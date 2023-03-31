/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellLabel;
import edu.ku.brc.af.ui.forms.persist.FormCellPanel;
import edu.ku.brc.af.ui.forms.persist.FormCellSubView;

/**
 * A simple container using the JGoodies layout for better organzing UI components. It also enables
 * the user to creat a button bar. The params to the JGoodies layout are defined in the FormCellPanel object
 * that is passed in.
 * 
 * @author rods
 *
 * @code_status Complete
 * 
 * Created Date: Oct 02, 2006
 *
 */
public class PanelViewable extends JPanel implements ViewBuilderIFace
{
    public enum PanelType {Unknown, Panel, ButtonBar}
    
    protected DefaultFormBuilder builder;
    protected CellConstraints    cc = new CellConstraints();
    protected ViewBuilderIFace   parentBuilder;
   
    /**
     * Creates a class that is used to organize or help crafted the layout. It is light-weight
     * and is merely for arranging UI components.
     * @param cellPanel the cell definition
     */
    public PanelViewable(final ViewBuilderIFace parentBuilder, final FormCellPanel cellPanel)
    {
        this.parentBuilder = parentBuilder;
        
        builder = new DefaultFormBuilder(new FormLayout(cellPanel.getColDef(), cellPanel.getRowDef()), this);
        setOpaque(false);
    }
    
    /**
     * Builds a button bar.
     * @param btns the buttons for the btn bar
     * @return the btn bar
     */
    public static JComponent buildButtonBar(final JButton[] btns)
    {
        return com.jgoodies.forms.factories.ButtonBarFactory.buildCenteredBar(btns);
    }
    
    /**
     * Get the type of the btn bar from a string
     * @param typeStr the string in question
     * @return the type
     */
    public static PanelType getType(final String typeStr)
    {
        return StringUtils.isNotEmpty(typeStr) && typeStr.equalsIgnoreCase("buttonbar") ? PanelType.ButtonBar : PanelType.Panel;
    }
    
    //--------------------------------------------------------
    // ViewBuilderIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addControlToUI(java.awt.Component, int, int, int, int)
     */
    public void addControlToUI(final Component control, final int colInx, final int rowInx, final int colSpan, final int rowSpan)
    {
        builder.add(control, cc.xywh(colInx, rowInx, colSpan, rowSpan));

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addLabel(edu.ku.brc.ui.forms.persist.FormCellLabel, javax.swing.JLabel)
     */
    public void addLabel(final FormCellLabel formCell, final JLabel label)
    {
        parentBuilder.addLabel(formCell, label);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#createSeparator(java.lang.String)
     */
    public Component createSeparator(final String title)
    {
        int        titleAlignment  = builder.isLeftToRight() ? SwingConstants.LEFT : SwingConstants.RIGHT;
        JComponent titledSeparator = builder.getComponentFactory().createSeparator(title, titleAlignment);
        setControlSize(titledSeparator);
        return titledSeparator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addRecordIndentifier(java.lang.String, javax.swing.ImageIcon)
     */
    public JComponent createRecordIndentifier(final String title, final ImageIcon icon)
    {
        // not supported
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addSubView(edu.ku.brc.ui.forms.persist.FormCell, edu.ku.brc.ui.forms.MultiView, int, int, int, int)
     */
    public void addSubView(final FormCellSubView formCell,
                           final MultiView subView,
                           final int colInx,
                           final int rowInx,
                           final int colSpan,
                           final int rowSpan)
    {
        if (parentBuilder instanceof FormViewObj)
        {
            ((FormViewObj)parentBuilder).addSubView(formCell, subView, colInx, rowInx, colSpan, rowSpan, false);
            builder.add(subView, cc.xywh(colInx, rowInx, colSpan, rowSpan, "fill,fill"));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#closeSubView(edu.ku.brc.ui.forms.persist.FormCellSubView)
     */
    public void closeSubView(FormCellSubView formCell)
    {
        // not supported
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#registerControl(edu.ku.brc.ui.forms.persist.FormCell, java.awt.Component)
     */
    public void registerControl(final FormCellIFace formCell, final Component control)
    {
        parentBuilder.registerControl(formCell, control);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ViewBuilderIFace#hasRequiredFields()
     */
    @Override
    public boolean hasRequiredFields()
    {
        return parentBuilder.hasRequiredFields();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#registerPlugin(edu.ku.brc.ui.forms.persist.FormCellIFace, edu.ku.brc.af.ui.forms.UIPluginable)
     */
    public void registerPlugin(FormCellIFace formCell, UIPluginable uip)
    {
        parentBuilder.registerPlugin(formCell, uip);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#setDataObjectRep(edu.ku.brc.ui.forms.DraggableRecordIdentifier)
     */
    public void setDataObjectRep(@SuppressWarnings("unused") final DraggableRecordIdentifier draggableRI)
    {
        // not supported
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#shouldFlatten()
     */
    public boolean shouldFlatten()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#getControlByName(java.lang.String)
     */
    public Component getControlByName(final String name)
    {
        return parentBuilder.getControlByName(name);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#getControlById(java.lang.String)
     */
    public Component getControlById(String id)
    {
        return parentBuilder.getControlById(id);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#fixUpRequiredDerivedLabels()
     */
    public void fixUpRequiredDerivedLabels()
    {
        // no op
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ViewBuilderIFace#doneBuilding()
     */
    @Override
    public void doneBuilding()
    {
    }

}
