/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.db;

import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;


/**
 * Create a TextField with accompanying UI that enables it to display a single formatted value in the text field
 * and then the "info" button can be pressed to display a dialog/window of the full data for the object that is in the control.
 * See the constructr for details.

 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class TextFieldWithInfo extends JPanel implements GetSetValueIFace, AppPrefsChangeListener
{
    protected static final Logger log                 = Logger.getLogger(TextFieldWithInfo.class);

    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

    protected boolean            isInError  = false;
    protected boolean            isRequired = false;
    protected boolean            isChanged  = false;
    protected Color              bgColor    = null;

    protected JTextField         textField;
    protected JButton            infoBtn     = null;
    protected String             className;
    protected String             idName;
    protected String             keyName;
    protected String             format;
    protected String             uiFieldFormatterName;
    protected String             dataObjFormatterName;
    protected Class<?>           classObj    = null;
    protected DataGetterForObj   getter      = null;
    protected String             displayInfoDialogName;
    protected String[]           fieldNames;
    protected Object             dataObj     = null;
    protected String             frameTitle = null;
    
    protected boolean            isRestricted;
    protected String             restrictedStr;

    protected ViewBasedDisplayIFace frame      = null;
    protected MultiView             multiView  = null;


    /**
     * Constructor.
     * @param className the Class name of the java object that represents the table
     * @param idName the POJO field name of the ID column
     * @param keyName the POJO field name of the key column
     * @param format the format specification (null is OK if displayNames is null)
     * @param displayInfoDialogName the name to look up to display the search dialog (from the search dialog factory)
     */
    public TextFieldWithInfo(final String className,
                             final String idName,
                             final String keyName,
                             final String format,
                             final String uiFieldFormatterName,
                             final String dataObjFormatterName,
                             final String displayInfoDialogName,
                             final String objTitle)
    {
        this.className        = className;
        this.idName           = idName;
        this.keyName          = keyName;
        this.format           = format;
        this.uiFieldFormatterName  = uiFieldFormatterName;
        this.displayInfoDialogName = displayInfoDialogName;
        this.dataObjFormatterName  = dataObjFormatterName;
        
        textField = new JTextField();

        init(objTitle);

        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(classObj.getSimpleName());
        restrictedStr = FormHelper.checkForRestrictedValue(tableInfo);
        if (restrictedStr != null)
        {
            isRestricted = true;
        }
    }

    /**
     * Sets the string that is pre-appended to the title.
     * @param frameTitle the string arg
     */
    public void setFrameTitle(final String frameTitle)
    {
        this.frameTitle = frameTitle;
    }

    /**
     * Sets the MultiView parent into the control.
     * @param multiView parent multiview
     */
    public void setMultiView(final MultiView multiView)
    {
        this.multiView = multiView;
    }

    /**
     * Returns the text field for this control.
     * @return the text field for this control
     */
    public JTextField getTextField()
    {
        return textField;
    }

    /**
     * @param displayInfoDialogName the displayInfoDialogName to set
     */
    public void setDisplayInfoDialogName(String displayInfoDialogName)
    {
        this.displayInfoDialogName = displayInfoDialogName;
    }

    /**
     * Creates a Dialog (non-modal) that will display detail information
     * for the object in the text field.
     */
    protected void createInfoFrame()
    {
        frame = UIRegistry.getViewbasedFactory().createDisplay(UIHelper.getWindow(this),
                                                               displayInfoDialogName,
                                                               frameTitle,
                                                               getResourceString("CLOSE"), //$NON-NLS-1$
                                                               false,  // false means View mode
                                                               MultiView.NO_OPTIONS | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE,
                                                               null,
                                                               ViewBasedDialogFactoryIFace.FRAME_TYPE.FRAME);
        if (frame == null)
        {
            return;
        }
        //frame.getOkBtn().setEnabled(true);

        if (multiView != null)
        {
            multiView.registerDisplayFrame(frame);
        }
        
        frame.setCloseListener(new ViewBasedDisplayActionAdapter()
        {
            @Override
            public boolean okPressed(ViewBasedDisplayIFace vbd)
            {
                if (frame != null)
                {
                    if (multiView != null)
                    {
                        multiView.unregisterDisplayFrame(frame);
                    }
                    frame.dispose();
                    frame = null;
                }
                return true;
            }
            
        });
        frame.setData(dataObj);
        frame.showDisplay(true);
        
        if (frame instanceof ViewBasedDisplayFrame)
        {
            frame.getOkBtn().setEnabled(true);
        }

    }

    /* (non-Javadoc)
     * @see java.awt.Component#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        textField.requestFocus();
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        updateEnabled(enabled);
    }
    
    /**
     * @param isEnabled
     */
    private void updateEnabled(final boolean enabled)
    {
        boolean isEnabled = enabled;
        if (isRestricted)
        {
            isEnabled = false;
        }
        
        textField.setEnabled(isEnabled);
        if (infoBtn != null)
        {
            infoBtn.setEnabled(isEnabled && dataObj != null);
        }
    }

    /**
     * Creates the UI for the ComboBox.
     * @param objTitle the title of one object needed for the Info Button
     */
    public void init(final String objTitle)
    {
        setControlSize(textField);
        
        fieldNames = split(StringUtils.deleteWhitespace(keyName), ","); //$NON-NLS-1$

        try
        {
            classObj = Class.forName(className);

        } catch (ClassNotFoundException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TextFieldWithInfo.class, ex);
           log.error(ex);
           throw new RuntimeException(ex);
        }

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g,1px,p", "c:p"), this); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc         = new CellConstraints();

        builder.add(textField, cc.xy(1,1));

        if (StringUtils.isNotEmpty(displayInfoDialogName))
        {
            infoBtn = new JButton(IconManager.getIcon("InfoIcon", IconManager.IconSize.Std16)); //$NON-NLS-1$
            infoBtn.setToolTipText(String.format(getResourceString("ShowRecordInfoTT"), new Object[] {objTitle})); //$NON-NLS-1$
            infoBtn.setFocusable(false);
            infoBtn.setMargin(new Insets(1,1,1,1));
            infoBtn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            builder.add(infoBtn, cc.xy(3,1));
            
            infoBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    createInfoFrame();
                }
            });
        }

        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        bgColor = textField.getBackground();
        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor       = AppPrefsCache.getColorWrapper("ui", "formatting", "valtextcolor"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            requiredfieldcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        AppPreferences.getRemote().addChangeListener("ui.formatting.requiredfieldcolor", this); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        if (this.isInError && textField.isEnabled())
        {
            Dimension dim = getSize();
            g.setColor(valtextcolor.getColor());
            g.drawRect(0, 0, dim.width-1, dim.height-1);
        }
    }

    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        dataObj = value;
        updateEnabled(value != null);
        
        if (isRestricted)
        {
            textField.setText(restrictedStr);
            return;
        }
        
        if (value != null)
        {
            String restricted = FormHelper.checkForRestrictedValue(dataObj);
            if (restricted != null)
            {
                textField.setText(restricted);
                textField.setCaretPosition(0);
                return;
            }
            
            if (getter == null)
            {
                getter = new DataGetterForObj();
            }

            // NOTE: If there was a formatName defined for this then the value coming
            // in will already be correctly formatted.
            // So just set the value if there is a format name.
            Object newVal = value;
            if (isEmpty(dataObjFormatterName))
            {
                Object[] val = UIHelper.getFieldValues(fieldNames, this.dataObj, getter);
                
                UIFieldFormatterIFace uiFieldFormatter = UIFieldFormatterMgr.getInstance().getFormatter(uiFieldFormatterName);
                if (uiFieldFormatter != null)
                {
                    newVal = uiFieldFormatter.formatFromUI(val[0]).toString();
                } else
                {
                    if (isNotEmpty(format))
                    {
                        newVal = UIHelper.getFormattedValue(format, val);
                    } else
                    {
                        newVal = this.dataObj;
                    }
                }
            } else
            {
                newVal = DataObjFieldFormatMgr.getInstance().format(value, dataObjFormatterName);
            }

            textField.setText(newVal != null ? newVal.toString() : "");
            textField.setCaretPosition(0);

        } else
        {
            textField.setText(""); //$NON-NLS-1$
            isInError = isRequired;
        }
        if (frame != null)
        {
            frame.setData(dataObj);
        }
        //repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        //return textField.getText();
        return dataObj;
    }

    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsChangeListener#preferenceChange(edu.ku.brc.af.prefs.AppPrefsChangeEvent)
     */
    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("requiredfieldcolor")) //$NON-NLS-1$
        {
            textField.setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        }
    }

}
