/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.prefs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 12, 2008
 *
 */

public class GenericPrefsPanel extends JPanel implements PrefsSavable, PrefsPanelIFace
{
    private static final Logger log  = Logger.getLogger(GenericPrefsPanel.class);
    
    private static final String  securityPrefix    = "Prefs."; //$NON-NLS-1$

    protected String    name;
    protected String    title;
    protected ViewIFace formView  = null;
    protected Viewable  form      = null;
    protected String    hContext  = null;
    protected Color     shadeColor = null;
    
    protected PrefsPanelMgrIFace mgr = null;
    
    // Security
    protected PermissionIFace permissions = null;

    /**
     * Constructor.
     */
    public GenericPrefsPanel()
    {
        super(new BorderLayout());
        setOpaque(false);
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getTitle()
     */
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setTitle(java.lang.String)
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @param viewSetName
     * @param viewName
     */
    public void createForm(final String viewSetName, 
                           final String viewName)
    {
        formView = AppContextMgr.getInstance().getView(viewSetName, viewName);
        if (formView != null)
        {
            UIValidator.setIgnoreAllValidation(this, true);
            
            ViewFactory.setFormTransparent(true);
            form = ViewFactory.createFormView(null, formView, null, null, MultiView.NO_OPTIONS, null);
            if (form != null)
            {
                ViewFactory.setFormTransparent(false);
                form.setDataObj(AppPreferences.getRemote());
                UIValidator.setIgnoreAllValidation(this, false);
                
                if (form.getUIComponent() instanceof JPanel)
                {
                    ((JPanel)form.getUIComponent()).setOpaque(false);
                }
                add(form.getUIComponent(), BorderLayout.CENTER);
                
                form.getValidator().validateForm();
            }

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }  
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getChangedFields(java.util.Hashtable)
     */
    public void getChangedFields(final Properties changeHash)
    {
        FormViewObj fvo = (FormViewObj)form;
        Hashtable<String, String> idToNameHash = fvo.getIdToNameHash();
        
        Vector<String> ids = new Vector<String>();
        fvo.getFieldIds(ids);
        for (String id : ids)
        {
            Component comp = fvo.getCompById(id);
            if (comp instanceof UIValidatable && ((UIValidatable)comp).isChanged())
            {
                changeHash.put(idToNameHash.get(id), comp instanceof GetSetValueIFace ? ((GetSetValueIFace)comp).getValue() : ""); //$NON-NLS-1$
            }
            //System.err.println("ID: "+id+"  Name: "+idToNameHash.get(id)+" changed: "+(comp instanceof UIValidatable && ((UIValidatable)comp).isChanged()));
            /*Object newVal = FormViewObj.getValueFromComponent(comp, false, false, id);
            Object oldVal = oldValues.get(id);
            System.err.println("["+newVal.toString()+"]["+oldVal.toString()+"] "+(!newVal.toString().equals(oldVal.toString())));
            if (!newVal.toString().equals(oldVal.toString()))
            {
                System.err.println("id: "+id+" changed.");
            }*/
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (shadeColor != null)
        {
            Dimension size = getSize();
            g.setColor(shadeColor);
            g.fillRect(0, 0, size.width, size.height);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsSavable#savePrefs()
     */
    public void savePrefs()
    {
        if (form != null && form.getValidator() == null || form.getValidator().hasChanged())
        {
            form.getDataFromUI();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getValidator()
     */
    public FormValidator getValidator()
    {
        return form != null ? form.getValidator() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#isFormValid()
     */
    public boolean isFormValid()
    {
        return form != null ? form.getValidator().getState() == UIValidatable.ErrorType.Valid : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#isOKToLoad()
     */
    @Override
    public boolean isOKToLoad()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getPermissions()
     */
    public PermissionIFace getPermissions()
    {
        if (permissions == null)
        {
            permissions = SecurityMgr.getInstance().getPermission(securityPrefix + getPermissionName());
        }
        return permissions;
    }

    /**
     * @return name to be used when getting permissions from SecurityMgr.
     */
    protected String getPermissionName()
    {
        return name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setPermissions(edu.ku.brc.af.core.PermissionIFace)
     */
    public void setPermissions(PermissionIFace permissions)
    {
        this.permissions = permissions;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setPrefsPanelMgr(edu.ku.brc.af.prefs.PrefsPanelMgrIFace)
     */
    @Override
    public void setPrefsPanelMgr(PrefsPanelMgrIFace mgrArg)
    {
        this.mgr = mgrArg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#getHelpContext()
     */
    public String getHelpContext()
    {
        return hContext;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setHelpContext(java.lang.String)
     */
    public void setHelpContext(String context)
    {
        hContext = context;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#setShadeColor(java.awt.Color)
     */
    @Override
    public void setShadeColor(Color color)
    {
        shadeColor = color;
    }
    
    
}
