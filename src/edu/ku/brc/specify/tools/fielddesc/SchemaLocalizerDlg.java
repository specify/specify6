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
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.lowagie.text.pdf.SpotColor;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 27, 2007
 *
 */
public class SchemaLocalizerDlg extends CustomDialog implements LocalizableIOIFace
{
    private static final Logger log = Logger.getLogger(SchemaLocalizerDlg.class);
    
    protected SchemaLocalizerPanel schemaLocPanel;
    protected LocalizableIOIFace   localizableIOIFace;
    
    protected Vector<SpLocaleContainer>                    tables     = new Vector<SpLocaleContainer>();
    protected Hashtable<String, LocalizableContainerIFace> tableHash  = new Hashtable<String, LocalizableContainerIFace>();
    
    protected Vector<LocalizableJListItem>                 tableDisplayItems;
    protected Hashtable<String, LocalizableJListItem>      tableDisplayItemsHash = new Hashtable<String, LocalizableJListItem>();
    
    protected Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>> itemJListItemsHash = new Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>>();

    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public SchemaLocalizerDlg(Dialog dialog, String title, boolean isModal, int whichBtns, Component contentPanel) throws HeadlessException
    {
        super(dialog, title, isModal, whichBtns, contentPanel);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param frame
     * @param title
     * @param isModal
     * @param contentPanel
     * @throws HeadlessException
     */
    public SchemaLocalizerDlg(Frame frame, String title, boolean isModal, Component contentPanel) throws HeadlessException
    {
        super(frame, title, isModal, contentPanel);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param frame
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @param defaultBtn
     * @throws HeadlessException
     */
    public SchemaLocalizerDlg(Frame frame, String title, boolean isModal, int whichBtns, Component contentPanel, int defaultBtn) throws HeadlessException
    {
        super(frame, title, isModal, whichBtns, contentPanel, defaultBtn);
    }

    /**
     * @param frame
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public SchemaLocalizerDlg(Frame frame, String title, boolean isModal, int whichBtns, Component contentPanel) throws HeadlessException
    {
        super(frame, title, isModal, whichBtns, contentPanel);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        LocalizerBasePanel.setLocalizableStrFactory(new LocalizableStrFactory() {
            public LocalizableStrIFace create()
            {
                SpLocaleItemStr str = new SpLocaleItemStr();
                str.initialize();
                return str;
            }
            public LocalizableStrIFace create(String text, Locale locale)
            {
                return new SpLocaleItemStr(text, locale); // no initialize needed for this constructor
            }
        });
        
        //DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        //List<SpLocaleContainer> list = session.getDataList(SpLocaleContainer.class);
        
        localizableIOIFace = this;//new SchemaLocalizerXMLHelper();
        localizableIOIFace.load();
        
        schemaLocPanel = new SchemaLocalizerPanel();
        schemaLocPanel.setLocalizableIO(localizableIOIFace);
        
        
        schemaLocPanel.setStatusBar(UIRegistry.getStatusBar());
        schemaLocPanel.buildUI();
        schemaLocPanel.setHasChanged(localizableIOIFace.didModelChangeDuringLoad());
        
        contentPanel   = schemaLocPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        save();
        super.okButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#createResourceFiles()
     */
    public boolean createResourceFiles()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#didModelChangeDuringLoad()
     */
    public boolean didModelChangeDuringLoad()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.fielddesc.LocalizableJListItem)
     */
    public LocalizableContainerIFace getContainer(LocalizableJListItem item)
    {
        LocalizableContainerIFace tmpContainer = tableHash.get(item.getName());
        if (tmpContainer == null)
        {
            if (item.getId() != null)
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                SpLocaleContainer container = session.load(SpLocaleContainer.class, item.getId());
                session.close();
                
                tables.add(container);
                tableHash.put(container.getName(), container);
                
                return container;
                
            } else // use the name 
            {
                // to be implemented.
            }
        }
        return tmpContainer;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#getContainerDisplayItems()
     */
    public Vector<LocalizableJListItem> getContainerDisplayItems()
    {
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            tableDisplayItems = new Vector<LocalizableJListItem>();
            
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery("select SpLocaleContainerID, Name from splocalecontainer order by name");
            
            while (rs.next())
            {
                tableDisplayItems.add(new LocalizableJListItem(rs.getString(2), rs.getInt(1), null));
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return tableDisplayItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#getDisplayItems(edu.ku.brc.specify.tools.fielddesc.LocalizableJListItem)
     */
    public Vector<LocalizableJListItem> getDisplayItems(LocalizableJListItem container)
    {
       
        Vector<LocalizableJListItem> items = itemJListItemsHash.get(container);
        if (items == null)
        {
            LocalizableContainerIFace cont = tableHash.get(container.getName());
            if (cont != null)
            {
                SpLocaleContainer contr = (SpLocaleContainer)cont;
                // Make sure the items are loaded before getting them.
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                session.attach(contr);
                contr.getItems();
               
                for (SpLocaleItemStr sItem : contr.getNames())
                {
                    System.out.println(sItem.getText());
                }
                for (SpLocaleItemStr sItem : contr.getDescs())
                {
                    System.out.println(sItem.getText());
                }
                
                items = new Vector<LocalizableJListItem>();
                for (LocalizableItemIFace item : cont.getContainerItems())
                {
                    SpLocaleContainerItem cItem = (SpLocaleContainerItem)item;
                    cItem.getNames();
                    cItem.getDescs();
                    
                    for (SpLocaleItemStr sItem : cItem.getNames())
                    {
                        System.out.println(sItem.getText());
                    }
                    for (SpLocaleItemStr sItem : cItem.getDescs())
                    {
                        System.out.println(sItem.getText());
                    }
                    items.add(new LocalizableJListItem(cItem.getName(), cItem.getId(), null));
                    //System.out.println(cItem.getName());
                }
                itemJListItemsHash.put(container, items);
                Collections.sort(items);
                
                session.close();
                
            } else
            {
                log.error("Couldn't find container ["+container.getName()+"]");
            }
        }
       
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#getItem(edu.ku.brc.specify.tools.fielddesc.LocalizableContainerIFace, edu.ku.brc.specify.tools.fielddesc.LocalizableJListItem)
     */
    public LocalizableItemIFace getItem(LocalizableContainerIFace container, LocalizableJListItem item)
    {
        SpLocaleContainer contr = (SpLocaleContainer)container;
        
        // Make sure the items are loaded before getting them.
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(contr);
        contr.getItems();
        contr.getNames();
        contr.getDescs();
        
        for (LocalizableItemIFace cItem : container.getContainerItems())
        {
            if (cItem.getName().equals(item.getName()))
            {
                session.close();
                return cItem;
            }
        }
        session.close();
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#load()
     */
    public boolean load()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#save()
     */
    public boolean save()
    {
        schemaLocPanel.getAllDataFromUI();
        try
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            for (SpLocaleContainer container : tables)
            {
                SpLocaleContainer c = session.merge(container);
                session.saveOrUpdate(c);
                for (SpLocaleItemStr str : c.getNames())
                {
                    session.saveOrUpdate(session.merge(str));
                    System.out.println(str.getText());
                }

                
                for (SpLocaleContainerItem item : c.getItems())
                {
                    SpLocaleContainerItem i = session.merge(item);
                    session.saveOrUpdate(i);
                    for (SpLocaleItemStr str : i.getNames())
                    {
                        session.saveOrUpdate(session.merge(str));
                        System.out.println(str.getText());
                    }
                }
            }
            session.commit();
            
            session.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return true;
    }
    
    

}
