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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
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
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.helpers.ImageFilter;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
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
    protected static final String BNR_ICON_SIZE           = "banner.icon.size"; //$NON-NLS-1$
    
    protected final String INNER_APPICON_NAME = "InnerAppIcon";
    protected final int[]  pixelSizes         = {16, 20, 24, 32};
    
    protected final int BASE_FONT_SIZE = 6;

    protected JComboBox    fontNames    = null;
    protected JComboBox    fontSizes    = null;
    protected JComboBox    controlSizes = null;
    protected ValComboBox  formTypesCBX = null;
    protected JTextField   testField    = null;
    protected ValComboBox  disciplineCBX;
    protected ValComboBox  appIconCBX;
    protected String       newAppIconName = null;
    protected ValComboBox  dateFieldCBX;
    protected boolean      clearFontSettings = false;
    protected ValComboBox  bnrIconSizeCBX;
    
    protected Hashtable<String, UIHelper.CONTROLSIZE> controlSizesHash = new Hashtable<String, UIHelper.CONTROLSIZE>();
    protected Hashtable<String, String> formTypeHash = new Hashtable<String, String>();
    
    /**
     * Constructor.
     */
    public FormattingPrefsPanel()
    {
        createUI();
    }
    
    /**
     * @param formats
     * @param ch
     */
    protected void addFormats(final Vector<String> formats, final Character ch)
    {
        formats.add("MM"+ch+"dd"+ch+"yyyy");
        formats.add("dd"+ch+"MM"+ch+"yyyy");
        formats.add("yyyy"+ch+"MM"+ch+"dd");
        formats.add("yyyy"+ch+"dd"+ch+"MM");
    }
    
    /**
     * 
     */
    protected void fillDateFormat()
    {
        
        String currentFormat = AppPreferences.getRemote().get("ui.formatting.scrdateformat", null);
        
        TimeZone tz = TimeZone.getDefault();
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        dateFormatter.setTimeZone(tz);
        String dateStr = dateFormatter.format(Calendar.getInstance().getTime());
        Character ch = null;
        for (int i=0;i<10;i++)
        {
            if (!StringUtils.isNumeric(dateStr.substring(i,i+1)))
            {
                ch = dateStr.charAt(i);
                break;
            }
        }
        
        if (ch != null)
        {
            boolean skip = false;
            Vector<String> formats = new Vector<String>();
            if (ch == '/')
            {
                addFormats(formats, '/');
                skip = true;
            }
            if (ch != '.')
            {
                addFormats(formats, '.');
                skip = true;
            }
            if (ch != '-')
            {
                addFormats(formats, '-');
                skip = true;
            }
            
            if (!skip)
            {
                addFormats(formats, ch);
            }
            
            int selectedInx = 0;
            int inx        = 0;
            DefaultComboBoxModel model = (DefaultComboBoxModel)dateFieldCBX.getModel();
            for (String fmt : formats)
            {
                model.addElement(fmt);
                if (currentFormat != null && currentFormat.equals(fmt))
                {
                    selectedInx = inx;
                }
                inx++;
            }
            dateFieldCBX.getComboBox().setSelectedIndex(selectedInx);
        }
    }
    
    /**
     * 
     */
    protected void fillFormTypes()
    {
        String[] formTypeArray = {"win", "lnx", "mac", "exp"};
        String[] formTypeDesc = {"Small Font Format (ideal for Windows)", "Medium Font Format (ideal for Linux)", "Large Font Format (ideal for Mac)", "Elastic Layout"}; // I18N
        
        String curFormType = AppPreferences.getLocalPrefs().get("ui.formatting.formtype", UIHelper.getOSTypeAsStr());
        
        int selectedInx = 0;
        int inx        = 0;
        DefaultComboBoxModel model = (DefaultComboBoxModel)formTypesCBX.getModel();
        for (String type : formTypeArray)
        {
            model.addElement(formTypeDesc[inx]);
            formTypeHash.put(formTypeDesc[inx], type);
            if (curFormType != null && curFormType.equals(type))
            {
                selectedInx = inx;
            }
            inx++;
        }
        formTypesCBX.getComboBox().setSelectedIndex(selectedInx);
        
        formTypesCBX.getComboBox().addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            public void actionPerformed(ActionEvent e)
            {
                form.getValidator().dataChanged(null, null, null);
            }
        });
    }


    /**
     * Create the UI for the panel
     */
    protected void createUI()
    {
        createForm("Preferences", "Formatting"); //$NON-NLS-1$ //$NON-NLS-2$
        
        UIValidator.setIgnoreAllValidation(this, true);
        
        JLabel      fontNamesLabel = form.getLabelFor("fontNames"); //$NON-NLS-1$
        ValComboBox fontNamesVCB   = form.getCompById("fontNames"); //$NON-NLS-1$
        
        JLabel      fontSizesLabel = form.getLabelFor("fontSizes"); //$NON-NLS-1$
        ValComboBox fontSizesVCB   = form.getCompById("fontSizes"); //$NON-NLS-1$
        
        JLabel      controlSizesLabel = form.getLabelFor("controlSizes"); //$NON-NLS-1$
        ValComboBox controlSizesVCB   = form.getCompById("controlSizes"); //$NON-NLS-1$
        
        formTypesCBX = form.getCompById("formtype"); //$NON-NLS-1$
        
        fontNames    = fontNamesVCB.getComboBox();
        fontSizes    = fontSizesVCB.getComboBox();
        controlSizes = controlSizesVCB.getComboBox();
        
        testField = form.getCompById("fontTest"); //$NON-NLS-1$
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
                String titleStr = getResourceString(cs.toString());
                controlSizeTitles.add(titleStr); 
                controlSizesHash.put(titleStr, cs);
                controlSizes.addItem(titleStr);
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
            
            Hashtable<String, Boolean> namesUsed = new Hashtable<String, Boolean>();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (Font font : ge.getAllFonts())
            {
                if (namesUsed.get(font.getFamily()) == null)
                {
                    fontNames.addItem(font.getFamily());
                    namesUsed.put(font.getFamily(), true); //$NON-NLS-1$
                }
            }
            for (int i=BASE_FONT_SIZE;i<22;i++)
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
                            testField.setFont(new Font((String)fontNames.getSelectedItem(), Font.PLAIN, fontSizes.getSelectedIndex()+BASE_FONT_SIZE)); 
                            form.getUIComponent().validate();
                            clearFontSettings = false;
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
        
        final JLabel dispLabel = form.getCompById("disciplineIcon"); //$NON-NLS-1$
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
        // Date Field
        //-----------------------------------
        dateFieldCBX = form.getCompById("scrdateformat"); //$NON-NLS-1$
        fillDateFormat();
        
        //-----------------------------------
        // FormType
        //-----------------------------------
        fillFormTypes();
        
        //-----------------------------------
        // Do App Icon
        //-----------------------------------
        
        final JButton getIconBtn      = form.getCompById("GetIconImage"); //$NON-NLS-1$
        final JButton clearIconBtn    = form.getCompById("ClearIconImage"); //$NON-NLS-1$
        final JLabel  appLabel        = form.getCompById("appIcon"); //$NON-NLS-1$
        final JButton resetDefFontBtn = form.getCompById("ResetDefFontBtn"); //$NON-NLS-1$
        
        String    imgEncoded      = AppPreferences.getRemote().get(iconImagePrefName, ""); //$NON-NLS-1$
        ImageIcon innerAppImgIcon = null;
        if (StringUtils.isNotEmpty(imgEncoded))
        {
            innerAppImgIcon = GraphicsUtils.uudecodeImage("", imgEncoded); //$NON-NLS-1$
            if (innerAppImgIcon != null && innerAppImgIcon.getIconWidth() != 32 || innerAppImgIcon.getIconHeight() != 32)
            {
                innerAppImgIcon = null;
                clearIconBtn.setEnabled(false);
            } else
            {
                clearIconBtn.setEnabled(true);
            }
        }
        
        if (innerAppImgIcon == null)
        {
            innerAppImgIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
            clearIconBtn.setEnabled(false);
        } else
        {
            clearIconBtn.setEnabled(true);
        }
        appLabel.setIcon(innerAppImgIcon);
        
        getIconBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                chooseToolbarIcon(appLabel, clearIconBtn);
            }
        });
        
        clearIconBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                ImageIcon appIcon = IconManager.getIcon("AppIcon");
                IconEntry entry = IconManager.getIconEntryByName(INNER_APPICON_NAME);
                entry.setIcon(appIcon);
                if (entry.getIcons().get(IconManager.IconSize.Std32) != null)
                {
                    entry.getIcons().get(IconManager.IconSize.Std32).setImageIcon(appIcon);
                }
                
                appLabel.setIcon(IconManager.getIcon("AppIcon")); //$NON-NLS-1$
                clearIconBtn.setEnabled(false);
                AppPreferences.getRemote().remove(iconImagePrefName);
                form.getValidator().dataChanged(null, null, null);
            }
        });
        
        resetDefFontBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                Font sysDefFont = UIRegistry.getDefaultFont();
                ComboBoxModel model = fontNames.getModel();
                for (int i=0;i<model.getSize();i++)
                {
                    //System.out.println("["+model.getElementAt(i).toString()+"]["+sysDefFont.getFamily()+"]");
                    if (model.getElementAt(i).toString().equals(sysDefFont.getFamily()))
                    {
                        fontNames.setSelectedIndex(i);
                        clearFontSettings = true;
                        break;
                    }
                }
                
                if (clearFontSettings)
                {
                    fontSizes.setSelectedIndex(sysDefFont.getSize() - BASE_FONT_SIZE);
                    clearFontSettings = true; // needs to be redone 
                }
                
                form.getValidator().dataChanged(null, null, null);
            }
        });
        
        //-----------------------------------
        // Do Banner Icon Size
        //-----------------------------------
        
        String fmtStr = "%d x %d pixels";//getResourceString("BNR_ICON_SIZE");
        bnrIconSizeCBX = form.getCompById("bnrIconSizeCBX"); //$NON-NLS-1$
        
        int size = AppPreferences.getLocalPrefs().getInt(BNR_ICON_SIZE, 20);
        inx = 0;
        cnt = 0;
        for (int pixelSize : pixelSizes)
        {
            ((DefaultComboBoxModel)bnrIconSizeCBX.getModel()).addElement(String.format(fmtStr, pixelSize, pixelSize));
            if (pixelSize == size)
            {
                inx = cnt;
            }
            cnt++;
        }
        
        bnrIconSizeCBX.getComboBox().addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            public void actionPerformed(ActionEvent e)
            {
                form.getUIComponent().validate();
            }
        });
        
        bnrIconSizeCBX.getComboBox().setSelectedIndex(inx);
        
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

                ImageIcon appIcon;
                if (newIcon != null)
                {
                    appLabel.setIcon(newIcon);
                    clearIconBtn.setEnabled(true);
                    String imgBufStr = GraphicsUtils.uuencodeImage(newAppIconName, newIcon);
                    AppPreferences.getRemote().put(iconImagePrefName, imgBufStr);
                    appIcon = newIcon;

                } else
                {
                    appIcon = IconManager.getIcon("AppIcon");
                    appLabel.setIcon(appIcon); //$NON-NLS-1$
                    clearIconBtn.setEnabled(false);
                    AppPreferences.getRemote().remove(iconImagePrefName);
                }
                
                IconEntry entry = IconManager.getIconEntryByName(INNER_APPICON_NAME);
                entry.setIcon(appIcon);
                if (entry.getIcons().get(IconManager.IconSize.Std32) != null)
                {
                    entry.getIcons().get(IconManager.IconSize.Std32).setImageIcon(appIcon);
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
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        if (collection != null)
        {
            return iconImageDiscipPrefName + "." + collection.getCollectionName(); //$NON-NLS-1$
        }
        return null;
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
            changeHash.remove("controlSizes"); //$NON-NLS-1$
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
            super.savePrefs(); // gets data from the UI
            
            Pair<String, ImageIcon> item = (Pair<String, ImageIcon>)disciplineCBX.getComboBox().getSelectedItem();
            if (item != null)
            {
                AppPreferences.getRemote().put(getDisciplineImageName(), item.first); //$NON-NLS-1$
                
                IconManager.aliasImages(item.first,           // Source
                                        "collectionobject");  // Dest //$NON-NLS-1$
                IconManager.aliasImages(item.first,           // Source
                                        "CollectionObject");  // Dest //$NON-NLS-1$

            }
            
            int inx = bnrIconSizeCBX.getComboBox().getSelectedIndex();
            if (inx > -1)
            {
                AppPreferences.getLocalPrefs().putInt(BNR_ICON_SIZE, pixelSizes[inx]);    
            }
            
            AppPreferences local = AppPreferences.getLocalPrefs();
            
            if (!(UIHelper.isMacOS_10_5_X()))
            {
                String key = "ui.formatting.controlSizes"; //$NON-NLS-1$
                if (clearFontSettings)
                {
                    local.remove(key+".FN");
                    local.remove(key+".SZ");
                    
                    UIRegistry.setBaseFont(UIRegistry.getDefaultFont());
                    BaseTask.setToolbarBtnFont(UIRegistry.getBaseFont()); // For ToolbarButtons
                    RolloverCommand.setDefaultFont(UIRegistry.getBaseFont());

                    
                } else
                {
                    Font baseFont = UIRegistry.getBaseFont();
                    if (!baseFont.getFamily().equals(fontNames.getSelectedItem()) ||
                        baseFont.getSize() != fontSizes.getSelectedIndex()+BASE_FONT_SIZE)
                    {
                        Font newBaseFont = UIRegistry.adjustPerDefaultFont(new Font((String)fontNames.getSelectedItem(), Font.PLAIN, fontSizes.getSelectedIndex()+BASE_FONT_SIZE));
                        UIRegistry.setBaseFont(newBaseFont);
                        BaseTask.setToolbarBtnFont(newBaseFont); // For ToolbarButtons
                        RolloverCommand.setDefaultFont(newBaseFont);
    
                        local.put(key+".FN", (String)fontNames.getSelectedItem());
                        local.putInt(key+".SZ", fontSizes.getSelectedIndex()+BASE_FONT_SIZE);
                    }
                }
                
            } else
            {
                String key = "ui.formatting.controlSizes"; //$NON-NLS-1$
                UIHelper.setControlSize(controlSizesHash.get(controlSizes.getSelectedItem()));
                local.put(key, controlSizesHash.get(controlSizes.getSelectedItem()).toString());
            }
            
            String fType =  formTypeHash.get(formTypesCBX.getComboBox().getSelectedItem()).toString();
            local.put("ui.formatting.formtype", fType);
        }
    }

    
}
