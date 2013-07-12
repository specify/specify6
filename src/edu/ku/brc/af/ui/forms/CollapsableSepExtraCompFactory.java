/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 20, 2011
 *
 */
public class CollapsableSepExtraCompFactory
{
    public static final String factoryName = "edu.ku.brc.af.core.CollapsableSepExtraBtnFactory"; //$NON-NLS-1$
    
    protected static CollapsableSepExtraCompFactory instance  = new CollapsableSepExtraCompFactory();
    
    // Data Members
    protected HashMap<String, CollapseLinkInfo> collLinkHash = new HashMap<String, CollapseLinkInfo>();
    //protected HashMap<String, ArrayList<CollapseLinkEntryInfo>> hash = new HashMap<String, ArrayList<CollapseLinkEntryInfo>>();

    
    /**
     * Constructor of singleton
     */
    protected CollapsableSepExtraCompFactory()
    {
        // do nothing
        loadConfig();
    }
    
    /**
     * @param name
     * @param subName
     * @return
     */
    private String mkKey(final String name, final String subName)
    {
        return String.format("%s__%s", name, subName);
    }
    
    /**
     * @param name
     * @return
     */
    public JComponent getComponent(final String categoryName, final String name)
    {
        return createBtn(mkKey(categoryName, name));
    }
    
    /**
     * @param lnkInfo
     * @return
     */
    private JButton createBtn(final String key)
    {
        final CollapseLinkInfo lnkInfo = collLinkHash.get(key);
        if (lnkInfo != null)
        {
            ActionListener al = lnkInfo instanceof CollapseLinkEntryInfo && ((CollapseLinkEntryInfo)lnkInfo).hasItems() ? 
                                    createChoosehLink((CollapseLinkEntryInfo)lnkInfo) :
                                    createLaunchLink(lnkInfo.getUrl()); 
            JButton btn = UIHelper.createIconBtn("video", IconManager.STD_ICON_SIZE.Std16, UIRegistry.getResourceString("CHSE_VIDEO_TT"), al);
            btn.setEnabled(true);
            return btn;
        }
        return null;
    }
    
    /**
     * @param clei
     * @return
     */
    private ActionListener createChoosehLink(final CollapseLinkEntryInfo clei)
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    JList list = new JList(new Vector<CollapseLinkInfo>(clei.getItems()));
                    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    JScrollPane sc = UIHelper.createScrollPane(list);
                    PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
                    pb.add(sc, (new CellConstraints()).xy(1,1));
                    pb.setDefaultDialogBorder();
                    
                    final CustomDialog dlg = new CustomDialog((Frame)null, UIRegistry.getResourceString("CHSE_VIDEO"), true, CustomDialog.OKCANCEL, pb.getPanel());
                    list.addMouseListener(new MouseAdapter()
                    {
                        @Override
                        public void mouseClicked(MouseEvent e)
                        {
                            if (e.getClickCount() == 2)
                            {
                                dlg.getOkBtn().doClick();
                            }
                        }
                    });
                    UIHelper.centerAndShow(dlg);
                    if (!dlg.isCancelled())
                    {
                        CollapseLinkInfo cli = (CollapseLinkInfo)list.getSelectedValue();
                        try
                        {
                            Desktop.getDesktop().browse(new URI(cli.getUrl()));
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        };
    }

    
    /**
     * @param url
     * @return
     */
    private ActionListener createLaunchLink(final String url)
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Desktop.getDesktop().browse(new URI(url));
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        };
    }
    
    /**
     * Loads the changes.
     */
    @SuppressWarnings("unchecked")
    protected void loadConfig()
    {
        XStream xstream = new XStream();
        CollapseLinkEntryInfo.configXStream(xstream);
        CollapseLinkInfo.configXStream(xstream);
        
        File file = new File(XMLHelper.getConfigDirPath("colextrabtn.xml"));
        if (file.exists())
        {
            try
            {
                HashMap<String, ArrayList<CollapseLinkEntryInfo>> xsHash = (HashMap<String, ArrayList<CollapseLinkEntryInfo>>)xstream.fromXML(new FileInputStream(file));
                for (Object keyObj : xsHash.keySet())
                {
                    String key = (String)keyObj;
                    ArrayList<CollapseLinkEntryInfo> items = xsHash.get(key);
                    for (CollapseLinkEntryInfo lei : items)
                    {
                        System.out.println("["+mkKey(key, lei.getName())+"]");
                        collLinkHash.put(mkKey(key, lei.getName()), lei);
                    }
                }
                
                /*PrintWriter pw = new PrintWriter(file);
                HashMap<String, ArrayList<CollapseLinkEntryInfo>> hash = new HashMap<String, ArrayList<CollapseLinkEntryInfo>>();
                ArrayList<CollapseLinkEntryInfo> items = new ArrayList<CollapseLinkEntryInfo>();
                CollapseLinkEntryInfo itm = new CollapseLinkEntryInfo("myname", "mytitle", "myurl");
                CollapseLinkInfo      kid = new CollapseLinkInfo("mynamekid", "mytitlekid", "myurlkid");
                ArrayList<CollapseLinkInfo> kidItems = new ArrayList<CollapseLinkInfo>();
                kidItems.add(kid);
                itm.setItems(kidItems);
                items.add(itm);
                hash.put("Forms", items);
                pw.print(xstream.toXML(hash));
                pw.close(); */
                
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Returns the instance of the CollapsableSepExtraBtnFactory.
     * @return the instance of the CollapsableSepExtraBtnFactory.
     */
    public static CollapsableSepExtraCompFactory getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryNameStr = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (factoryNameStr != null) 
        {
            try 
            {
                return instance = (CollapsableSepExtraCompFactory)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate CollapsableSepExtraBtnFactory factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }

}