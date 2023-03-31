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
package edu.ku.brc.specify.plugins;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.NotImplementedException;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayActionAdapter;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.extras.FishBaseInfoGetter;
import edu.ku.brc.specify.extras.FishBaseInfoGetterListener;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * FishBase plugin For SPNHC Demo

 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class FishBase extends JPanel implements GetSetValueIFace, UIPluginable, FishBaseInfoGetterListener
{
    protected JTextField          textField;
    protected Taxon               taxon;
    protected JButton             infoBtn    = null;
    protected JProgressBar        progress   = null;

    protected ViewBasedDisplayIFace frame      = null;
    protected MultiView             multiView  = null;

    protected FishBaseInfoGetter getter;

    /**
     *
     */
    public FishBase()
    {
    }


    /**
     * Creates a Dialog (non-modl) that will display detail information
     * for the object in the text field.
     */
    protected void createInfoFrame()
    {
        String species = taxon.getName();
        String genus   = taxon.getParent().getName();

        frame = UIRegistry.getViewbasedFactory().createDisplay(UIHelper.getWindow(this),
                                                                   "FishBase",
                                                                   "Fish Base Information",
                                                                   getResourceString("CLOSE"),
                                                                   false,
                                                                   MultiView.NO_OPTIONS,
                                                                   null,
                                                                   ViewBasedDialogFactoryIFace.FRAME_TYPE.FRAME); // false means View mode
        frame.setData(null);
        frame.showDisplay(true);
        frame.setCloseListener(new ViewBasedDisplayActionAdapter()
        {
            public boolean okPressed(@SuppressWarnings("unused") ViewBasedDisplayIFace vbd)
            {
                if (multiView != null)
                {
                    multiView.unregisterDisplayFrame(frame);
                }
                frame.dispose();
                frame = null;
                return true;
            }
        });
        
        multiView = frame.getMultiView();

        if (multiView != null)
        {
            multiView.registerDisplayFrame(frame);
            progress = multiView.getCurrentView().getCompById("progress");
            //System.out.println(progress);
            progress.setIndeterminate(true);
            progress.setValue(50);
        }

        if (frame instanceof JFrame)
        {
            ((JFrame)frame).setIconImage(IconManager.getIcon("FishBase", IconManager.IconSize.Std16).getImage());
        }

        if (getter == null)
        {
            getter = new FishBaseInfoGetter(this, FishBaseInfoGetter.InfoType.Summary, genus, species);
        }
        getter.start();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#canCarryForward()
     */
    @Override
    public boolean canCarryForward()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getCarryForwardFields()
     */
    @Override
    public String[] getCarryForwardFields()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
     */
    @Override
    public String getTitle()
    {
        return "Fish Base";
    }


    /* (non-Javadoc)
     * @see java.awt.Component#requestFocus()
     */
    public void requestFocus()
    {
        textField.requestFocus();
    }

    /**
     * @param dom
     */
    protected void setDataIntoframe(@SuppressWarnings("unused") final Element dom)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                progress.setIndeterminate(false);
                progress.setVisible(false);
                frame.setData(getter.getDom());

            }
        });
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        throw new NotImplementedException("isNotEmpty not implement!");
    }

    //--------------------------------------------------------
    //-- FishBaseInfoGetterListener
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.extras.FishBaseInfoGetterListener#infoArrived(edu.ku.brc.specify.extras.FishBaseInfoGetter)
     */
    public void infoArrived(FishBaseInfoGetter getterArg)
    {
        setDataIntoframe(getterArg.getDom());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.extras.FishBaseInfoGetterListener#infoGetWasInError(edu.ku.brc.specify.extras.FishBaseInfoGetter)
     */
    public void infoGetWasInError(FishBaseInfoGetter getterArg)
    {
        setDataIntoframe(null);
    }

    //--------------------------------------------------------
    //-- UIPluginable
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(final Properties properties, final boolean isViewMode)
    {
        textField = new JTextField();
        Insets insets = textField.getBorder().getBorderInsets(textField);
        textField.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        textField.setForeground(Color.BLACK);
        textField.setEditable(false);

        ColorWrapper viewFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        if (viewFieldColor != null)
        {
            textField.setBackground(viewFieldColor.getColor());
        }

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("f:p:g,1px,p", "c:p"), this);
        CellConstraints cc         = new CellConstraints();

        builder.add(textField, cc.xy(1,1));

        infoBtn = new JButton(IconManager.getIcon("FishBase", IconManager.IconSize.Std16));
        infoBtn.setFocusable(false);
        infoBtn.setMargin(new Insets(1,1,1,1));
        infoBtn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        builder.add(infoBtn, cc.xy(3,1));

        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        infoBtn.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run()
                            {
                                createInfoFrame();
                            }
                        });
                    }
                });
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        // TODO Auto-generated method stub
        
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(ChangeListener listener)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"taxon"};
    }
    
    //--------------------------------------------------------
    //-- GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        if (value != null && value instanceof Taxon)
        {
            taxon = (Taxon)value;
            infoBtn.setEnabled(taxon.getRankId() == 220);
            textField.setText(taxon.getFullName());//taxon.getFullName());
        } else
        {
            textField.setText("");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return taxon;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        multiView = null;
        getter    = null;
        frame     = null;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setViewable(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#carryForwardStateChange()
     */
    @Override
    public void carryForwardStateChange()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setNewObj(boolean)
     */
    @Override
    public void setNewObj(boolean isNewObj)
    {
        // no op
    }
}
