package edu.ku.brc.specify.prefs;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import edu.ku.brc.specify.ui.forms.FormViewable;
import edu.ku.brc.specify.ui.forms.ViewFactory;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.persist.FormView;

/**
 * Preference Panel for setting EMail Preferences.
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class EMailPrefsPanel extends JPanel
{

    /**
     * 
     */
    public EMailPrefsPanel()
    {
        super(new BorderLayout());
        
        FormView formView = ViewMgr.getView("Preferences", 1);
        
         FormViewable form = ViewFactory.createView(formView);
        
        JComponent comp = form.getUIComponent();
        if (comp != null)
        {
            comp.invalidate(); 
            add(comp, BorderLayout.CENTER);
        }
        ViewMgr.clearAll();
    }

}
