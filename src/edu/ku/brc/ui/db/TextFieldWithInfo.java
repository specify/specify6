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

package edu.ku.brc.ui.db;

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.ui.forms.DataGetterForObj;
import edu.ku.brc.ui.forms.DataObjFieldFormatMgr;
import edu.ku.brc.ui.forms.MultiView;


/**
 * Create a TextField with accompanying UI that enables it to display a single formatted value in the text field
 * and then the "info" button can be pressed to display a dialog/window of the full data for the object that is in the control.
 * See the constructr for details.

 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class TextFieldWithInfo extends JPanel implements GetSetValueIFace, AppPrefsChangeListener, PropertyChangeListener
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
    protected String             formatName;
    protected Class              classObj    = null;
    protected DataGetterForObj   getter      = null;
    protected String             displayInfoDialogName;
    protected String[]           fieldNames;
    protected Object             dataObj     = null;
    protected String             frameTitle = null;

    protected ViewBasedDisplayIFace  frame      = null;
    protected MultiView          multiView  = null;


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
                             final String formatName,
                             final String displayInfoDialogName,
                             final String objTitle)
    {
        this.className        = className;
        this.idName           = idName;
        this.keyName          = keyName;
        this.format           = format;
        this.formatName       = formatName;
        this.displayInfoDialogName = displayInfoDialogName;

        textField = new JTextField();

        init(false, objTitle);
    }

    /**
     * Sets the string that is preappended to the title.
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
     * Creates a Dialog (non-modl) that will display detail information
     * for the object in the text field.
     */
    protected void createInfoFrame()
    {
        frame = UICacheManager.getViewbasedFactory().createDisplay(displayInfoDialogName,
                                                                   frameTitle,
                                                                   getResourceString("Close"),
                                                                   false,  // false means View mode
                                                                   false,  // false means don't show switcher
                                                                   false,
                                                                   ViewBasedDialogFactoryIFace.FRAME_TYPE.FRAME);
        frame.setCloseListener(this);
        frame.setData(dataObj);
        frame.showDisplay(true);

        if (multiView != null)
        {
            multiView.registerDisplayFrame(frame);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.Component#requestFocus()
     */
    public void requestFocus()
    {
        textField.requestFocus();
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
        if (infoBtn != null)
        {
            infoBtn.setEnabled(enabled);
        }

    }

    /**
     * Creates the UI for the ComboBox.
     * @param makeEditable whether it can be editable
     * @param objTitle the title of one object needed for the Info Button
     */
    public void init(final boolean makeEditable, final String objTitle)
    {
        fieldNames = split(StringUtils.deleteWhitespace(keyName), ",");

        try
        {
            classObj = Class.forName(className);

        } catch (ClassNotFoundException ex)
        {
           log.error(ex);
           throw new RuntimeException(ex);
        }

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,1px,p", "c:p"), this);
        CellConstraints cc         = new CellConstraints();

        builder.add(textField, cc.xy(1,1));

        if (StringUtils.isNotEmpty(displayInfoDialogName))
        {
            infoBtn = new JButton(IconManager.getImage("InfoIcon"));
            infoBtn.setToolTipText(String.format(getResourceString("ShowRecordInfoTT"), new Object[] {objTitle}));
            infoBtn.setFocusable(false);
            infoBtn.setMargin(new Insets(1,1,1,1));
            infoBtn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            builder.add(infoBtn, cc.xy(3,1));
        }

        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        bgColor = textField.getBackground();
        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor       = AppPrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredfieldcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        AppPreferences.getRemote().addChangeListener("ui.formatting.requiredfieldcolor", this);


        infoBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createInfoFrame();
            }
        });

    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
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
    // PropertyChangeListener
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (multiView != null)
        {
            multiView.unregisterDisplayFrame(frame);
        }
        frame = null;
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
        if (value != null)
        {
            if (getter == null)
            {
                getter = new DataGetterForObj();
            }

            // NOTE: If there was a formatName defined for this then the value coming
            // in will already be correctly formatted.
            // So just set the cvalue if there is a format name.
            Object newVal = value;
            if (isEmpty(formatName))
            {
                Object[] val = UIHelper.getFieldValues(fieldNames, value, getter);
                if (isNotEmpty(format))
                {
                    newVal = UIHelper.getFormattedValue(val, format);
                } else
                {
                    newVal = value;
                }
            } else
            {
                newVal = DataObjFieldFormatMgr.format(value, formatName);
            }

            textField.setText(newVal.toString());

        } else
        {
            textField.setText("");
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
        return textField.getText();
    }

    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsChangeListener#preferenceChange(edu.ku.brc.af.prefs.AppPrefsChangeEvent)
     */
    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("requiredfieldcolor"))
        {
            textField.setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        }
    }

}
