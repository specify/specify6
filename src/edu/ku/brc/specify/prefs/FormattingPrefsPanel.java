package edu.ku.brc.specify.prefs;

import java.awt.BorderLayout;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.FormViewable;
import edu.ku.brc.specify.ui.forms.ViewFactory;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.validation.FormValidator;

/**
 * This panel will handle all the various options for formatting of data.
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class FormattingPrefsPanel extends JPanel implements PrefsPanelIFace, PrefsSavable
{
    private static Log log  = LogFactory.getLog(FormattingPrefsPanel.class);
    
    protected Preferences  prefNode = null;
    protected FormView     formView = null;
    protected FormViewable form     = null;
    
    public FormattingPrefsPanel()
    {
        Preferences appsNode = UICacheManager.getAppPrefs();
        prefNode = appsNode.node("ui/formatting");
        if (prefNode == null)
        {
            throw new RuntimeException("Could find pref for formatting!");
        }
        
        createUI();

    }
    
    /**
     * Create the UI for the panel
     */
    protected void createUI()
    {
        
        int id = 2;
        String name = "Preferences";
        
        formView = ViewMgr.getView(name, id);

        if (formView != null)
        {
            form = ViewFactory.createView(formView, prefNode);
            add(form.getUIComponent(), BorderLayout.CENTER);
            
        } else
        {
            log.info("Couldn't load form with name ["+name+"] Id ["+id+"]");
        }
        
        form.setDataObj(prefNode);
        
        form.getValidator().validateForm();

        
    }
    
    //--------------------------------------------------------------------
    // PrefsSavable Interface
    //--------------------------------------------------------------------
   
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.prefs.PrefsSavable#savePrefs()
     */
    public void savePrefs()
    {
        form.getDataFromUI();
    }
    

    //---------------------------------------------------
    // PrefsPanelIFace
    //---------------------------------------------------
    public FormValidator getValidator()
    {
        return form.getValidator();
    }

}
