package edu.ku.brc.specify.prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.validation.FormValidator;
import edu.ku.brc.ui.forms.validation.UIValidatable;
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
public class FormattingPrefsPanel extends JPanel implements PrefsPanelIFace, PrefsSavable
{
    private static final Logger log  = Logger.getLogger(FormattingPrefsPanel.class);

    protected ViewIFace    formView  = null;
    protected Viewable     form      = null;
    protected JComboBox    fontNames = null;
    protected JComboBox    fontSizes = null;
    protected JTextField   testField = null;

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
        
        String viewName = "Formatting";
        String name     = "Preferences";

        formView = AppContextMgr.getInstance().getView(name, viewName);

        if (formView != null)
        {
            
            ViewBasedDisplayPanel vbp = new ViewBasedDisplayPanel(null, name, viewName, "XXX", "java.util.Hashtable", "", true, MultiView.IS_EDITTING);
            add(vbp, BorderLayout.CENTER);
            form = vbp.getMultiView().getCurrentViewAsFormViewObj();
                
        } else
        {
            log.error("Couldn't load form with name ["+name+"] Id ["+viewName+"]");
        }

        form.setDataObj(AppPreferences.getRemote());
        
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
        final ValComboBox  dispVCB   = (ValComboBox)form.getCompById("disciplineIconCBX");
        JComboBox    comboBox  = dispVCB.getComboBox();
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
                    /*form.getValidator().setHasChanged(true);
                    form.getValidator().validateForm();
                    System.out.println(form.getValidator().isFormValid());
                    */
                }
            }
        });
        
        comboBox.setSelectedIndex(inx);
        form.getValidator().validateForm();


    }

    //--------------------------------------------------------------------
    // PrefsSavable Interface
    //--------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.prefs.PrefsSavable#savePrefs()
     */
    @SuppressWarnings("unchecked")
    public void savePrefs()
    {
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            form.getDataFromUI();
            
            Pair<String, ImageIcon> item = (Pair<String, ImageIcon>)((ValComboBox)form.getCompById("disciplineIconCBX")).getComboBox().getSelectedItem();
            if (item != null)
            {
                AppPreferences.getRemote().put("ui.formatting.disciplineicon", item.first);
                
                IconManager.aliasImages(item.first,           // Source
                                        "collectionobject");  // Dest
            }
            
            UIRegistry.setBaseFont(new Font((String)fontNames.getSelectedItem(), Font.PLAIN, fontSizes.getSelectedIndex()+6));
        }
    }


    //---------------------------------------------------
    // PrefsPanelIFace
    //---------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getValidator()
     */
    public FormValidator getValidator()
    {
        return form.getValidator();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#isFormValid()
     */
    public boolean isFormValid()
    {
        return form.getValidator().getState() == UIValidatable.ErrorType.Valid;
    }

}
