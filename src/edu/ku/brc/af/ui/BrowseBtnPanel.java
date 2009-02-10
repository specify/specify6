/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.af.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * This is a JPanel that contains a JTextField and a Button that enables the user to browser for a file
 * and sets the the file and path into the text field.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class BrowseBtnPanel extends JPanel implements GetSetValueIFace, DocumentListener
{
    protected JTextField textField;
    protected JButton    browseBtn;
    protected boolean    isForInput;
    protected boolean    useNativeFileDlg = false;
    
    protected boolean    isValidFile      = false;
    protected boolean    isValidatingFile = false;

    /**
     * Constructor.
     * @param value the value is set into the text field using "toString"
     * @param cols the number of columns for the text field
     * @param doDirsOnly only show directories
     * @param isForInput for input
     */
    public BrowseBtnPanel(final Object  value,
                          final int     cols,
                          final boolean doDirsOnly,
                          final boolean isForInput)
    {
        super(new BorderLayout());
        
        setOpaque(false);
        
        this.isForInput       = isForInput;
        
        createUI(value, cols, doDirsOnly, isForInput);
    }

    /**
     * Constructor.
     * @param textField the text field to use (most likely is a ValTextField)
     */
    public BrowseBtnPanel(final JTextField textField, 
                          final boolean doDirsOnly, 
                          final boolean isForInput)
    {
        super(new BorderLayout());
        this.textField = textField;

        createUI(null, -1, doDirsOnly, isForInput);
   }

    /**
     * Creates the UI and figures out whether it needs to create a JTextField or use the one it was given.
     * @param value the value for the new TextField
     * @param cols the number of columns for the new TextField
     * @param doDirsOnly
     * @param isForInputArg
     */
    protected void createUI(final Object  value, 
                            final int     cols, 
                            final boolean doDirsOnly, 
                            final boolean isForInputArg)
    {
        this.useNativeFileDlg = !doDirsOnly && !isForInput;
        
        setControlSize(this);

        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("f:p:g, 2dlu, r:p", "p"), this);
        CellConstraints cc = new CellConstraints();

        if (textField == null)
        {
            textField = new ValTextField(value != null ? value.toString() : "", cols);
        }
        panelBuilder.add(textField, cc.xy(1,1));

        browseBtn = createButton(getResourceString("BROWSE"));
        browseBtn.addActionListener(new BrowseAction(textField, doDirsOnly, isForInputArg));
        panelBuilder.add(browseBtn, cc.xy(3,1));

        setOpaque(false);
        
    }
    

    /**
     * @return the isValidatingFile
     */
    public boolean isValidatingFile()
    {
        return isValidatingFile;
    }

    /**
     * @param isValidatingFile the isValidatingFile to set
     */
    public void setValidatingFile(boolean isValidatingFile)
    {
        this.isValidatingFile = isValidatingFile;
        
        if (this.isValidatingFile)
        {
            textField.getDocument().addDocumentListener(this);
        } else
        {
            textField.getDocument().removeDocumentListener(this);
            isValidFile = true; // then it is always valid
        }
    }

    /**
     * @return the isValidFile
     */
    public boolean isValidFile()
    {
        return isValidFile;
    }

    /**
     * The text field.
     * @return the text field
     */
    public JTextField getTextField()
    {
        return textField;
    }


    /**
     * @return the browseBtn
     */
    public JButton getBrowseBtn()
    {
        return browseBtn;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        
        textField.setEnabled(enabled);
        browseBtn.setEnabled(enabled);
    }
    
    //-----------------------------------------------------
    // DocumentListener
    //-----------------------------------------------------
    
    protected void verifyForValidFile()
    {
        String str = textField.getText();

        if (StringUtils.isNotEmpty(str))
        {
            File file = new File(str);
            isValidFile = file.exists();
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    //@Override
    public void changedUpdate(DocumentEvent e)
    {
        verifyForValidFile();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    //@Override
    public void insertUpdate(DocumentEvent e)
    {
        verifyForValidFile();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    //@Override
    public void removeUpdate(DocumentEvent e)
    {
        verifyForValidFile();
    }

    //-----------------------------------------------------
    // GetSetValueIFace
    //-----------------------------------------------------

     /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        if (value instanceof String)
        {
            String newValue = (String)value;
            String oldValue = textField.getText();
            textField.setText(newValue);
            firePropertyChange("setValue", oldValue, newValue);
        }
        
        if (value == null)
        {
            String oldValue = textField.getText();
            textField.setText(defaultValue);
            firePropertyChange("setValue", oldValue, defaultValue);
            
            // We had to put a repaint() call in here.  Swing should have done this for us.
            textField.repaint();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return textField.getText();
    }

    //---------------------------------------------------------
    // Inner Class
    //---------------------------------------------------------

    /**
     * Action used to pop up the File Dialog.
     * @author rods
     *
     */
    public class BrowseAction implements ActionListener
    {
        private JTextField   txtField;
        private JFileChooser chooser       = null;
        private FileDialog   fileDlg       = null;    
        private boolean      dirsOnly;
        private boolean      isForInputBA;

        /**
         * Constructor with CommandAction.
         * @param textField the text control of the Browse Action
         */
        public BrowseAction(final JTextField textField, 
                            final boolean dirsOnly, 
                            final boolean isForInput)
        {
            this.txtField   = textField;
            this.dirsOnly   = dirsOnly;
            this.isForInputBA = isForInput;
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            if (useNativeFileDlg)
            {
                if (isForInputBA)
                {
                    fileDlg = new FileDialog((Frame)getTopWindow(), getResourceString("CHOOSE_FILE"), FileDialog.LOAD);
                } else
                {
                    fileDlg = new FileDialog((Frame)getTopWindow(), getResourceString("CHOOSE_FILE"), FileDialog.SAVE);
                }
                fileDlg.setVisible(true);
                
                if (StringUtils.isNotEmpty(fileDlg.getFile()))
                {
                    String filePath = fileDlg.getDirectory() + fileDlg.getFile();
                    
                    if (textField instanceof ValTextField)
                    {
                        ((ValTextField)txtField).setValueWithNotification(filePath, "", true);
                    } else
                    {
                        txtField.setText(fileDlg.getDirectory() + fileDlg.getFile());
                    }
                    txtField.repaint();
                }
                
            } else
            {
                if (chooser == null)
                {
                    this.chooser    = new JFileChooser();
                    this.chooser.setFileSelectionMode(dirsOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
                }
    
                int returnVal;
                if (isForInputBA)
                {
                    returnVal = chooser.showOpenDialog(UIRegistry.getTopWindow());
                } else
                {
                    returnVal = chooser.showSaveDialog(UIRegistry.getTopWindow());
                }
                
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    if (textField instanceof ValTextField)
                    {
                        ((ValTextField)txtField).setValueWithNotification(chooser.getSelectedFile().getAbsolutePath(), "", true);
                    } else
                    {
                        txtField.setText(chooser.getSelectedFile().getAbsolutePath());
                    }
                    txtField.repaint();
                }
            }
        }
    }


}
