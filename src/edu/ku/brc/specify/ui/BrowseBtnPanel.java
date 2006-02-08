/* Filename:    $RCSfile: BrowseBtnPanel.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.validation.ValTextField;

/**
 * This is a JPanel that contains a JTextField abd a Button that enables the user to browser for a file
 * and sets the the file and path into the text field
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class BrowseBtnPanel extends JPanel implements GetSetValueIFace
{
    protected JTextField textField;
    protected JButton    browseBtn;

    /**
     * Constructor
     * @param value the value is set into the text field using "toString"
     * @param cols the number of columns for the text field
     */
    public BrowseBtnPanel(Object  value,
                          int     cols)
    {
        super(new BorderLayout());

        createUI(value, cols);
    }

    /**
     * Constructor
     * @param textField the text field to use (most likely is a ValTextField)
     */
    public BrowseBtnPanel(JTextField textField)
    {
        super(new BorderLayout());
        this.textField = textField;

        createUI(null, -1);
   }

    /**
     * CReates the UI and figures out whether it needs to create a JTextField or use the one it was given
     * @param value the value for the new TextField
     * @param cols the number of columns for the new TextField
     */
    protected void createUI(final Object value, int cols)
    {
        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("f:p:g, 2dlu, r:p", "p"));
        CellConstraints cc = new CellConstraints();

        if (textField == null)
        {
            textField = new ValTextField(value != null ? value.toString() : "", cols);
        }
        panelBuilder.add(textField, cc.xy(1,1));

        browseBtn = new JButton(getResourceString("browse"));
        browseBtn.addActionListener(new BrowseAction(textField));
        panelBuilder.add(browseBtn, cc.xy(3,1));

        add(panelBuilder.getPanel(), BorderLayout.CENTER);

    }

    /**
     * @return the text field
     */
    public JTextField getTextField()
    {
        return textField;
    }


    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
        browseBtn.setEnabled(enabled);
    }

    //-----------------------------------------------------
    // GetSetValueIFace
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {
        if (value instanceof String)
        {
            String newValue = (String)value;
            String oldValue = textField.getText();
            textField.setText(newValue);
            firePropertyChange("setValue", oldValue, newValue);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return textField.getText();
    }

    //---------------------------------------------------------
    // Inner Class
    //---------------------------------------------------------

    public class BrowseAction implements ActionListener
    {
        private JTextField textField;

        /**
         * Constructor with Commandaction
         * @param textField the text control of the Browse Action
         */
        public BrowseAction(final JTextField textField)
        {
            this.textField = textField;
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser chooser = new JFileChooser();

            int returnVal = chooser.showOpenDialog(UICacheManager.get(UICacheManager.TOPFRAME));
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                textField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }


}
