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
package edu.ku.brc.specify.toycode.mexconabio;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import com.jgoodies.looks.plastic.theme.SkyKrupp;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr;
import edu.ku.brc.specify.config.SpecifyWebLinkMgr;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerFrame;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 3, 2010
 *
 */
public class AnalysisWithGBIFUIApp extends AnalysisWithGBIF
{
    protected CustomDialog dlg;
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public AnalysisWithGBIFUIApp()
    {
        super();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                
//              Set App Name, MUST be done very first thing!
                UIRegistry.setAppName("Specify");  //$NON-NLS-1$
                
                // Then set this
                IconManager.setApplicationClass(Specify.class);
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
                
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        //UIManager.setLookAndFeel(new WindowsLookAndFeel());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        //UIManager.setLookAndFeel(new GTKLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                        //PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new ExperienceRoyale());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                    }
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerFrame.class, e);
                    e.printStackTrace();
                }
                
                System.setProperty(AppContextMgr.factoryName,          "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
                System.setProperty(SchemaI18NService.factoryName,      "edu.ku.brc.specify.config.SpecifySchemaI18NService");  // Needed for Localization and Schema //$NON-NLS-1$
                System.setProperty(UIFieldFormatterMgr.factoryName,    "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");    // Needed for CatalogNumbering //$NON-NLS-1$
                System.setProperty(WebLinkMgr.factoryName,             "edu.ku.brc.specify.config.SpecifyWebLinkMgr");         // Needed for WebLnkButton //$NON-NLS-1$
                System.setProperty(DataObjFieldFormatMgr.factoryName,   "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");     // Needed for WebLnkButton //$NON-NLS-1$

                SpecifyDataObjFieldFormatMgr.setDoingLocal(true);
                SpecifyUIFieldFormatterMgr.setDoingLocal(true);
                SpecifyWebLinkMgr.setDoingLocal(true);
               
                AnalysisWithGBIF awgUI = new AnalysisWithGBIF();
                //awgUI.createDBConnection("lm2gbdb.nhm.ku.edu", "3399", "gbc20091216", "rods", "specify4us");
                awgUI.createSrcDBConnection("localhost", "3306", "mex", "root", "root");
                
                GBIFFindCleanupItems gbiffci = new GBIFFindCleanupItems(awgUI);
                gbiffci.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                UIHelper.centerAndShow(gbiffci);
                awgUI.cleanup();
                System.exit(0);
                
            }
        });

    }

}
