package edu.ku.brc.specify.prefs;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.ui.forms.ViewFactory;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.validation.FormValidator;

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

    protected View         formView = null;
    protected Viewable     form     = null;

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
        String name = "Preferences";

        formView = AppContextMgr.getInstance().getView(name, viewName);

        if (formView != null)
        {
            form = ViewFactory.createFormView(null, formView, null, AppPreferences.getRemote());
            add(form.getUIComponent(), BorderLayout.CENTER);

        } else
        {
            log.error("Couldn't load form with name ["+name+"] Id ["+viewName+"]");
        }

        form.setDataObj(AppPreferences.getRemote());

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
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            form.getDataFromUI();
        }
    }


    //---------------------------------------------------
    // PrefsPanelIFace
    //---------------------------------------------------
    public FormValidator getValidator()
    {
        return form.getValidator();
    }

}
