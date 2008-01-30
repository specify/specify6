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
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.helpers.ImageFilter;
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
@SuppressWarnings("serial")
public class FormattingPrefsPanel extends GenericPrefsPanel implements PrefsPanelIFace, PrefsSavable
{
    protected static final String iconPrefName      = "ui.formatting.user_icon_path";
    protected static final String iconImagePrefName = "ui.formatting.user_icon_image";
    
    protected JComboBox    fontNames = null;
    protected JComboBox    fontSizes = null;
    protected JTextField   testField = null;
    protected ValComboBox  disciplineCBX;
    protected ValComboBox  appIconCBX;
    protected String       newAppIconName = null;
    
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
        createForm("Preferences", "Formatting");
        
        UIValidator.setIgnoreAllValidation(this, true);
        
        ValComboBox fontNamesVCB = (ValComboBox)form.getCompById("fontNames");
        ValComboBox fontSizesVCB = (ValComboBox)form.getCompById("fontSizes");
        
        fontNames = fontNamesVCB.getComboBox();
        fontSizes = fontSizesVCB.getComboBox();
        
        testField = (JTextField)form.getCompById("fontTest");
        if (testField != null)
        {
            testField.setText("This is a Test");
        }
        
        Hashtable<String, String> namesUsed = new Hashtable<String, String>();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (Font font : ge.getAllFonts())
        {
            if (namesUsed.get(font.getFamily()) == null)
            {
                fontNames.addItem(font.getFamily());
                namesUsed.put(font.getFamily(), "X");
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
        
        //-----------------------------------
        // Do Discipline Icons
        //-----------------------------------

        String iconName = AppPreferences.getRemote().get("ui.formatting.disciplineicon", "CollectionObject");
        
        List<Pair<String, ImageIcon>> list = IconManager.getListByType("disciplines", IconManager.IconSize.Std16);
        
        final JLabel dispLabel = (JLabel)form.getCompById("disciplineIcon");
        disciplineCBX = (ValComboBox)form.getCompById("disciplineIconCBX");
        JComboBox          comboBox  = disciplineCBX.getComboBox();
        comboBox.setRenderer(new DefaultListCellRenderer()
        {
            @SuppressWarnings("unchecked")
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
        Pair<String, ImageIcon> colObj = new Pair<String, ImageIcon>("colobj_backstop", 
                                                                     IconManager.getIcon("colobj_backstop", IconManager.IconSize.Std16));
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
            @SuppressWarnings("unchecked")
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
        
        final JButton getIconBtn    = (JButton)form.getCompById("GetIconImage");
        final JButton clearIconBtn  = (JButton)form.getCompById("ClearIconImage");
        final JLabel  appLabel      = (JLabel)form.getCompById("appIcon");
        
        String imgEncoded = AppPreferences.getRemote().get(iconImagePrefName, "");
        ImageIcon appImgIcon = null;
        if (StringUtils.isNotEmpty(imgEncoded))
        {
            appImgIcon = GraphicsUtils.uudecodeImage("", imgEncoded);
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
            appImgIcon = IconManager.getIcon("AppIcon");
            clearIconBtn.setEnabled(false);
        } else
        {
            clearIconBtn.setEnabled(true);
        }
        appLabel.setIcon(appImgIcon);
        
        getIconBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                FileDialog fileDialog = new FileDialog((Frame) UIRegistry.get(UIRegistry.FRAME),
                                   getResourceString("PREF_CHOOSE_APPICON_TITLE"), FileDialog.LOAD);
                fileDialog.setFilenameFilter(new ImageFilter());
                UIHelper.centerAndShow(fileDialog);
                fileDialog.dispose();

                String path = fileDialog.getDirectory();
                if (StringUtils.isNotEmpty(path))
                {
                    String fullPath = path + File.separator + fileDialog.getFile();
                    File imageFile = new File(fullPath);
                    if (imageFile.exists())
                    {
                        ImageIcon newIcon = null;
                        ImageIcon icon = new ImageIcon(fullPath);
                        if (icon.getIconWidth() != -1 && icon.getIconHeight() != -1)
                        {
                            if (icon.getIconWidth() >32 || icon.getIconHeight() > 32)
                            {
                                Image img = GraphicsUtils.getScaledImage(icon, 32, 32);
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
                            AppPreferences.getRemote().put("ui.formatting.user_icon_image", imgBufStr);
                            
                        } else
                        {
                            appLabel.setIcon(IconManager.getIcon("AppIcon"));
                            clearIconBtn.setEnabled(false);
                            AppPreferences.getRemote().remove("ui.formatting.user_icon_image");
                        }
                        //((FormViewObj)form).getMVParent().set
                        form.getValidator().dataChanged(null, null, null);
                    }
                }
            }
        });
        
        clearIconBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                appLabel.setIcon(IconManager.getIcon("AppIcon"));
                clearIconBtn.setEnabled(false);
                AppPreferences.getRemote().remove("ui.formatting.user_icon_image");
                form.getValidator().dataChanged(null, null, null);
            }
        });
        
        UIValidator.setIgnoreAllValidation(this, false);
        fontNamesVCB.setChanged(false);
        fontSizesVCB.setChanged(false);

        form.getValidator().validateForm();
    }

    //--------------------------------------------------------------------
    // PrefsSavable Interface
    //--------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getChangedFields(java.util.Properties)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void getChangedFields(final Properties changeHash)
    {
        super.getChangedFields(changeHash);
        
        if (disciplineCBX.isChanged())
        {
            Pair<String, ImageIcon> item = (Pair<String, ImageIcon>)disciplineCBX.getComboBox().getSelectedItem();
            if (item != null)
            {
                changeHash.put("ui.formatting.disciplineicon", item.first);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.prefs.PrefsSavable#savePrefs()
     */
    @SuppressWarnings("unchecked")
    public void savePrefs()
    {
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            super.savePrefs();
            
            Pair<String, ImageIcon> item = (Pair<String, ImageIcon>)disciplineCBX.getComboBox().getSelectedItem();
            if (item != null)
            {
                AppPreferences.getRemote().put("ui.formatting.disciplineicon", item.first);
                
                IconManager.aliasImages(item.first,           // Source
                                        "collectionobject");  // Dest
            }
            
            UIRegistry.setBaseFont(new Font((String)fontNames.getSelectedItem(), Font.PLAIN, fontSizes.getSelectedIndex()+6));
        }
    }
}
