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
package edu.ku.brc.specify.prefs;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewFactory;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.validation.FormValidator;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Dec 14, 2006
 *
 */
public class MiscPrefsPanel extends JPanel implements PrefsSavable, PrefsPanelIFace
{
    private static final Logger log  = Logger.getLogger(MiscPrefsPanel.class);
    
    protected View         formView = null;
    protected Viewable     form     = null;
    
    /**
     * 
     */
    public MiscPrefsPanel()
    {
        super(new BorderLayout());

        String viewName    = "Misc";
        String viewSetName = "Preferences";

        formView = AppContextMgr.getInstance().getView(viewSetName, viewName);

        if (formView != null)
        {
            form = ViewFactory.createFormView(null, formView, null, AppPreferences.getRemote(), MultiView.NO_OPTIONS);
            add(form.getUIComponent(), BorderLayout.CENTER);

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        
        String luceneLocPref = AppPreferences.getLocalPrefs().get("ui.misc.luceneLocation", UIRegistry.getDefaultWorkingPath());
        AppPreferences.getLocalPrefs().put("ui.misc.luceneLocation", luceneLocPref);

        form.setDataObj(AppPreferences.getLocalPrefs());

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
        CommandDispatcher.dispatch(new CommandAction("Express_Search", "CheckIndexerPath", null));
    }

     //---------------------------------------------------
    // PrefsPanelIFace
    //---------------------------------------------------
    public FormValidator getValidator()
    {
        return form.getValidator();
    }

}
