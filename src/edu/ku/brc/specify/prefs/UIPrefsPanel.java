package edu.ku.brc.specify.prefs;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.ui.forms.FormViewable;
import edu.ku.brc.specify.ui.forms.ViewFactory;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.forms.persist.ViewSet;

/**
 * Preference Panel for setting the UI Preferences.
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class UIPrefsPanel extends JPanel
{

    public UIPrefsPanel()
    {
        super(new BorderLayout());
        
        // XXX Temporary load of form because now forma er being loaded right now
        try
        {
            ViewMgr.loadViewFile(XMLHelper.getConfigDirPath("fish_forms.xml"));
            
        } catch (Exception ex)
        {
            //log.fatal(ex);
            ex.printStackTrace();
        }

        // temp for testing 
        List<ViewSet> viewSets = ViewMgr.getViewSets();
        ViewSet viewSet = viewSets.get(0);
        List<FormView> forms = viewSet.getViews();
        
        FormViewable form = ViewFactory.createView(forms.get(0));
        
        JComponent comp = form.getUIComponent();
        if (comp != null)
        {
            comp.invalidate(); 
            add(comp, BorderLayout.CENTER);
        }
        ViewMgr.clearAll();
    }

}
