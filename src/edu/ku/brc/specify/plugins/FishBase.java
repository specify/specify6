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
package edu.ku.brc.specify.plugins;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.extras.FishBaseInfoGetter;
import edu.ku.brc.specify.extras.FishBaseInfoGetterListener;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.MultiView;

/**
 * FishBase plugin For SPNHC Demo

 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class FishBase extends JPanel implements GetSetValueIFace, UIPluginable, PropertyChangeListener, FishBaseInfoGetterListener
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

        frame = UICacheManager.getViewbasedFactory().createDisplay(UIHelper.getFrame(this),
                                                                   "FishBase",
                                                                   "Fish Base Information",
                                                                   getResourceString("Close"),
                                                                   false,
                                                                   MultiView.NO_OPTIONS,
                                                                   ViewBasedDialogFactoryIFace.FRAME_TYPE.FRAME); // false means View mode
        frame.setCloseListener(this);
        frame.setData(null);
        frame.showDisplay(true);

        multiView = frame.getMultiView();

        if (multiView != null)
        {
            multiView.registerDisplayFrame(frame);
            progress = (JProgressBar)multiView.getCurrentView().getCompById("progress");
            System.out.println(progress);
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
     * @see java.awt.Component#requestFocus()
     */
    public void requestFocus()
    {
        textField.requestFocus();
    }

    protected void setDataIntoframe(final Element dom)
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

    public void initialize(Map<String, String> properties)
    {
        textField = new JTextField();
        Insets insets = textField.getBorder().getBorderInsets(textField);
        textField.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        textField.setForeground(Color.BLACK);
        textField.setEditable(false);
        textField.setFont(UICacheManager.getFont(JTextField.class));

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

    //--------------------------------------------------------
    // PropertyChangeListener
    //--------------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (multiView != null)
        {
            multiView.unregisterDisplayFrame(frame);
        }
        frame = null;
    }
}
