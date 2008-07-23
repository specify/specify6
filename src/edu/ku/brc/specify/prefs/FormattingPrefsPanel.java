package edu.ku.brc.specify.prefs;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.helpers.ImageFilter;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.forms.validation.ValComboBox;
import edu.ku.brc.util.Pair;

/**
 * This panel will handle all the various options for formatting of data.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class FormattingPrefsPanel extends GenericPrefsPanel implements PrefsPanelIFace, PrefsSavable
{
    protected static final String iconPrefName            = "ui.formatting.user_icon_path"; //$NON-NLS-1$
    protected static final String iconImagePrefName       = "ui.formatting.user_icon_image"; //$NON-NLS-1$
    protected static final String iconImageDiscipPrefName = "ui.formatting.disciplineicon"; //$NON-NLS-1$
    
    
    protected JComboBox    fontNames = null;
    protected JComboBox    fontSizes = null;
    protected JComboBox    controlSizes = null;
    protected JTextField   testField = null;
    protected ValComboBox  disciplineCBX;
    protected ValComboBox  appIconCBX;
    protected String       newAppIconName = null;
    protected Hashtable<String, UIHelper.CONTROLSIZE> controlSizesHash = new Hashtable<String, UIHelper.CONTROLSIZE>();
    
    /**
     * Constructor.
     */
    public FormattingPrefsPanel()
    {
        createUI();
    }

    /**
     * Create the UI for the panel
     */
    protected void createUI()
    {
        createForm("Preferences", "Formatting"); //$NON-NLS-1$ //$NON-NLS-2$
        
        UIValidator.setIgnoreAllValidation(this, true);
        
        JLabel      fontNamesLabel = (JLabel)form.getLabelFor("fontNames"); //$NON-NLS-1$
        ValComboBox fontNamesVCB   = (ValComboBox)form.getCompById("fontNames"); //$NON-NLS-1$
        
        JLabel      fontSizesLabel = (JLabel)form.getLabelFor("fontSizes"); //$NON-NLS-1$
        ValComboBox fontSizesVCB   = (ValComboBox)form.getCompById("fontSizes"); //$NON-NLS-1$
        
        JLabel      controlSizesLabel = (JLabel)form.getLabelFor("controlSizes"); //$NON-NLS-1$
        ValComboBox controlSizesVCB   = (ValComboBox)form.getCompById("controlSizes"); //$NON-NLS-1$
        
        fontNames    = fontNamesVCB.getComboBox();
        fontSizes    = fontSizesVCB.getComboBox();
        controlSizes = controlSizesVCB.getComboBox();
        
        testField = (JTextField)form.getCompById("fontTest"); //$NON-NLS-1$
        if (testField != null)
        {
            testField.setText(UIRegistry.getResourceString("FormattingPrefsPanel.THIS_TEST")); //$NON-NLS-1$
        }
        
        if (UIHelper.isMacOS_10_5_X())
        {
            fontNamesLabel.setVisible(false);
            fontNamesVCB.setVisible(false);
            fontSizesLabel.setVisible(false);
            fontSizesVCB.setVisible(false);
            testField.setVisible(false);
            
            int inx = -1;
            int i   = 0;
            Vector<String> controlSizeTitles = new Vector<String>();
            for (UIHelper.CONTROLSIZE cs : UIHelper.CONTROLSIZE.values())
            {
                String title = getResourceString(cs.toString());
                controlSizeTitles.add(title); 
                controlSizesHash.put(title, cs);
                controlSizes.addItem(title);
                if (cs == UIHelper.getControlSize())
                {
                    inx = i;
                }
                i++;
            }
            controlSizes.setSelectedIndex(inx);
            
            Font baseFont = UIRegistry.getBaseFont();
            if (baseFont != null)
            {
                fontNames.addItem(baseFont.getFamily());
                fontSizes.addItem(Integer.toString(baseFont.getSize()));
                fontNames.setSelectedIndex(0);
                fontSizes.setSelectedIndex(0);
            }
            
        } else
        {
            controlSizesLabel.setVisible(false);
            controlSizesVCB.setVisible(false);
            
            Hashtable<String, String> namesUsed = new Hashtable<String, String>();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (Font font : ge.getAllFonts())
            {
                if (namesUsed.get(font.getFamily()) == null)
                {
                    fontNames.addItem(font.getFamily());
                    namesUsed.put(font.getFamily(), "X"); //$NON-NLS-1$
                }
            }
            for (int i=6;i<22;i++)
            {
                fontSizes.addItem(Integer.toString(i));
            }
            
            Font baseFont = UIRegistry.getBaseFont();
            if (baseFont != null)
            {
                fontNames.setSelectedItem(baseFont.getFamily());
                fontSizes.setSelectedItem(Integer.toString(baseFont.getSize()));
                
                if (testField != null)
                {
                    ActionListener al = new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            testField.setFont(new Font((String)fontNames.getSelectedItem(), Font.PLAIN, fontSizes.getSelectedIndex()+6)); 
                            form.getUIComponent().validate();
                        }
                    };
                    fontNames.addActionListener(al);
                    fontSizes.addActionListener(al);
                }
            }
        }
        
        //-----------------------------------
        // Do DisciplineType Icons
        //-----------------------------------

        String iconName = AppPreferences.getRemote().get(getDisciplineImageName(), "CollectionObject"); //$NON-NLS-1$ //$NON-NLS-2$
        
        List<Pair<String, ImageIcon>> list = IconManager.getListByType("disciplines", IconManager.IconSize.Std16); //$NON-NLS-1$
        Collections.sort(list, new Comparator<Pair<String, ImageIcon>>() {
            public int compare(Pair<String, ImageIcon> o1, Pair<String, ImageIcon> o2)
            {
                String s1 = UIRegistry.getResourceString(o1.first);
                String s2 = UIRegistry.getResourceString(o2.first);
                return s1.compareTo(s2);
            }
        });
        
        disciplineCBX = (ValComboBox)form.getCompById("disciplineIconCBX"); //$NON-NLS-1$
        
        final JLabel dispLabel = (JLabel)form.getCompById("disciplineIcon"); //$NON-NLS-1$
        JComboBox    comboBox  = disciplineCBX.getComboBox();
        comboBox.setRenderer(new DefaultListCellRenderer()
        {
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            public Component getListCellRendererComponent(JList listArg, Object value,
                    int index, boolean isSelected, boolean cellHasFocus)
            {
                Pair<String, ImageIcon> item = (Pair<String, ImageIcon>)value;
                JLabel label = (JLabel)super.getListCellRendererComponent(listArg, value, index, isSelected, cellHasFocus);
                if (item != null)
                {
                    label.setIcon(item.second);
                    label.setText(UIRegistry.getResourceString(item.first));
                }
                return label;
            }
        });
        
        int inx = 0;
        Pair<String, ImageIcon> colObj = new Pair<String, ImageIcon>("colobj_backstop",  //$NON-NLS-1$
                                                                     IconManager.getIcon("colobj_backstop", IconManager.IconSize.Std16)); //$NON-NLS-1$
        comboBox.addItem(colObj);
        
        int cnt = 1;
        for (Pair<String, ImageIcon> item : list)
        {
            if (item.first.equals(iconName))
            {
                inx = cnt;
            }
            comboBox.addItem(item);
            cnt++;
        }
        
        comboBox.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            public void actionPerformed(ActionEvent e)
            {
                JComboBox cbx = (JComboBox)e.getSource();
                Pair<String, ImageIcon> item = (Pair<String, ImageIcon>)cbx.getSelectedItem();
                if (item != null)
                {
                    dispLabel.setIcon(IconManager.getIcon(item.first));
                    form.getUIComponent().validate();
                }
            }
        });
        
        comboBox.setSelectedIndex(inx);
        
        //-----------------------------------
        // Do App Icon
        //-----------------------------------
        
        final JButton getIconBtn    = (JButton)form.getCompById("GetIconImage"); //$NON-NLS-1$
        final JButton clearIconBtn  = (JButton)form.getCompById("ClearIconImage"); //$NON-NLS-1$
        final JLabel  appLabel      = (JLabel)form.getCompById("appIcon"); //$NON-NLS-1$
        
        String    imgEncoded = AppPreferences.getRemote().get(iconImagePrefName, ""); //$NON-NLS-1$
        ImageIcon appImgIcon = null;
        if (StringUtils.isNotEmpty(imgEncoded))
        {
            appImgIcon = GraphicsUtils.uudecodeImage("", imgEncoded); //$NON-NLS-1$
            if (appImgIcon != null && appImgIcon.getIconWidth() != 32 || appImgIcon.getIconHeight() != 32)
            {
                appImgIcon = null;
                clearIconBtn.setEnabled(false);
            } else
            {
                clearIconBtn.setEnabled(true);
            }
        }
        
        if (appImgIcon == null)
        {
            appImgIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
            clearIconBtn.setEnabled(false);
        } else
        {
            clearIconBtn.setEnabled(true);
        }
        appLabel.setIcon(appImgIcon);
        
        getIconBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                chooseToolbarIcon(appLabel, clearIconBtn);
            }
        });
        
        clearIconBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                appLabel.setIcon(IconManager.getIcon("AppIcon")); //$NON-NLS-1$
                clearIconBtn.setEnabled(false);
                AppPreferences.getRemote().remove(iconImagePrefName);
                form.getValidator().dataChanged(null, null, null);
            }
        });
        
        UIValidator.setIgnoreAllValidation(this, false);
        fontNamesVCB.setChanged(false);
        fontSizesVCB.setChanged(false);

        form.getValidator().validateForm();
    }
    
    /**
     * Method for enabling a user to choose a toolbar icon.
     * @param appLabel the label used to display the icon.
     * @param clearIconBtn the button used to clear the icon
     */
    protected void chooseToolbarIcon(final JLabel appLabel, final JButton clearIconBtn)
    {
        FileDialog fileDialog = new FileDialog((Frame) UIRegistry.get(UIRegistry.FRAME),
                                               getResourceString("PREF_CHOOSE_APPICON_TITLE"), 
                                               FileDialog.LOAD); //$NON-NLS-1$
        fileDialog.setFilenameFilter(new ImageFilter());
        UIHelper.centerAndShow(fileDialog);
        fileDialog.dispose();

        String path = fileDialog.getDirectory();
        if (StringUtils.isNotEmpty(path))
        {
            String fullPath  = path + File.separator + fileDialog.getFile();
            File   imageFile = new File(fullPath);
            if (imageFile.exists())
            {
                ImageIcon newIcon = null;
                ImageIcon icon    = new ImageIcon(fullPath);
                if (icon.getIconWidth() != -1 && icon.getIconHeight() != -1)
                {
                    if (icon.getIconWidth() > 32 || icon.getIconHeight() > 32)
                    {
                        Image img = GraphicsUtils.getScaledImage(icon, 32, 32, false);
                        if (img != null)
                        {
                            newIcon = new ImageIcon(img);
                        }
                    } else
                    {
                        newIcon = icon;
                    }
                }

                if (newIcon != null)
                {
                    appLabel.setIcon(newIcon);
                    clearIconBtn.setEnabled(true);
                    String imgBufStr = GraphicsUtils.uuencodeImage(newAppIconName, newIcon);
                    AppPreferences.getRemote().put(iconImagePrefName, imgBufStr);

                } else
                {
                    appLabel.setIcon(IconManager.getIcon("AppIcon")); //$NON-NLS-1$
                    clearIconBtn.setEnabled(false);
                    AppPreferences.getRemote().remove(iconImagePrefName);
                }
                //((FormViewObj)form).getMVParent().set
                form.getValidator().dataChanged(null, null, null);
            }
        }

    }
    
    /**
     * @return the name of the icon to use for the current collection.
     */
    public static String getDisciplineImageName()
    {
        return iconImageDiscipPrefName + "." + AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName(); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return "PrefsFormatting"; //$NON-NLS-1$
    }
    
    //--------------------------------------------------------------------
    // PrefsSavable Interface
    //--------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getChangedFields(java.util.Properties)
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    @Override
    public void getChangedFields(final Properties changeHash)
    {
        super.getChangedFields(changeHash);
        
        if (disciplineCBX.isChanged())
        {
            Pair<String, ImageIcon> item = (Pair<String, ImageIcon>)disciplineCBX.getComboBox().getSelectedItem();
            if (item != null)
            {
                changeHash.put(getDisciplineImageName(), item.first); //$NON-NLS-1$
            }
        }
        
        if (UIHelper.isMacOS_10_5_X())
        {
            changeHash.remove("fontSizes"); //$NON-NLS-1$
            changeHash.remove("fontNames");
        } else
        {
            changeHash.remove("controlSizes"); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.prefs.PrefsSavable#savePrefs()
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public void savePrefs()
    {
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            super.savePrefs();
            
            Pair<String, ImageIcon> item = (Pair<String, ImageIcon>)disciplineCBX.getComboBox().getSelectedItem();
            if (item != null)
            {
                AppPreferences.getRemote().put(getDisciplineImageName(), item.first); //$NON-NLS-1$
                
                IconManager.aliasImages(item.first,           // Source
                                        "collectionobject");  // Dest //$NON-NLS-1$
                IconManager.aliasImages(item.first,           // Source
                                        "CollectionObject");  // Dest //$NON-NLS-1$

            }
            
            if (!UIHelper.isMacOS_10_5_X())
            {
                UIRegistry.setBaseFont(new Font((String)fontNames.getSelectedItem(), Font.PLAIN, fontSizes.getSelectedIndex()+6));
            } else
            {
                String key = "ui.formatting.controlSizes"; //$NON-NLS-1$
                UIHelper.setControlSize(controlSizesHash.get(controlSizes.getSelectedItem()));
                AppPreferences.getRemote().put(key, controlSizesHash.get(controlSizes.getSelectedItem()).toString());
                AppPreferences.getLocalPrefs().put(key, controlSizesHash.get(controlSizes.getSelectedItem()).toString());
            }
        }
    }
}
