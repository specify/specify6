/* Filename:    $RCSfile: QueryTask.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.formatDate;
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;

import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.core.NavBoxItemIFace;
import edu.ku.brc.specify.core.NavBoxMgr;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.EMailHelper;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.DroppableFormObject;
import edu.ku.brc.specify.tasks.subpane.DroppableTaskPane;
import edu.ku.brc.specify.tasks.subpane.FormPane;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.RolloverCommand;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.Trash;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * Takes care of offering up record sets, updating, deleteing and creating them.
 * 
 * @author rods
 *
 */
public class InfoRequestTask extends BaseTask
{
    // Static Data Members
    private static Log log  = LogFactory.getLog(InfoRequestTask.class);
    
    public static final String     INFOREQUEST        = "InfoRequest";
    public static final DataFlavor INFOREQUEST_FLAVOR = new DataFlavor(RecordSetTask.class, INFOREQUEST);
    
    public static final String INFO_REQ_MESSAGE = "Specify Info Request";
    
    // Data Members
    public FormPane recentFormPane = null;    

    // Data Members
    protected NavBox navBox = null;
    
    /**
     * Default Constructor
     *
     */
    public InfoRequestTask()
    {
        super(INFOREQUEST, getResourceString(INFOREQUEST));
        CommandDispatcher.register(INFOREQUEST, this);
        
        subPaneClassFilter = FormPane.class;
    }
    
    /**
     * Returns a title for the InfoRequest
     * @param infoRequest the infor request to construct a title for
     * @return Returns a title for the InfoRequest
     */
    protected String getTitle(final InfoRequest infoRequest)
    {
        return infoRequest.getFirstName() + " " + infoRequest.getLastName() + " - " + formatDate(infoRequest.getRequestDate());
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(InfoRequest.class);
            List infoRequests = criteria.list();
              
            navBox = new NavBox(title);
            
            for (Iterator iter=infoRequests.iterator();iter.hasNext();)
            {
                addInfoRequest((InfoRequest)iter.next());
                
            }          
            navBoxes.addElement(navBox);
            HibernateUtil.closeSession();
        }
    }
    
    /**
     * Add an InfoRequest Item to the box
     * @param infoRequest the infoRequest to be added
     */
    protected void addInfoRequest(final InfoRequest infoRequest)
    {
        // These value should not be hard coded here
        DroppableFormObject dfo = new DroppableFormObject("view valid", 80, infoRequest);
        NavBoxItemIFace     nbi = addNavBoxItem(navBox, getTitle(infoRequest), INFOREQUEST, "Delete", dfo);
        RolloverCommand     roc = (RolloverCommand)nbi;
        roc.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                createFormPanel((RolloverCommand)ae.getSource());
            }
        });
        addDraggableDataFlavors(roc);
    }
    
    /**
     * Looks up a SubPane by the viewset name and form id and data
     * @param viewSetName the view set name
     * @param formId the form id
     * @return the subpane that matches
     */
    protected FormPane getFormPane(final String viewSetName, final int formId, final Object data)
    {
        for (SubPaneIFace sp : subPanes)
        {
            if (sp instanceof FormPane) // should always a FormPane
            {
                FormPane fp = (FormPane)sp;
                System.out.println(viewSetName+" "+fp.getViewSetName());
                System.out.println(formId+" "+fp.getFormId());
                System.out.println(data+" "+fp.getData());
                if (viewSetName.equals(fp.getViewSetName()) && 
                    formId == fp.getFormId() && 
                    data == fp.getData())
                {
                    return fp;
                }
            }
        }
        return null;
    }
    
    /**
     * Looks to see if a form already exists for this request and show it
     * otherwise it creates a form and add it to the SubPaneMgr
     */
    protected void createFormPanel(RolloverCommand roc)
    {
        DroppableFormObject dfo = (DroppableFormObject)roc.getData();
        if (recentFormPane.getComponentCount() == 0)
        {
            recentFormPane.createForm(dfo.getViewSetName(), dfo.getFormId(), dfo.getData());
        } else
        {
            FormPane fp = getFormPane(dfo.getViewSetName(), dfo.getFormId(), dfo.getData());
            System.out.println(UICacheManager.formatDate(((InfoRequest)dfo.getData()).getRequestDate()));
            if (fp != null)
            {
                UICacheManager.getSubPaneMgr().showPane(fp.getName());
                
            } else
            {
                recentFormPane = new FormPane(name, this, dfo.getViewSetName(), dfo.getFormId(), dfo.getData());            
                addSubPaneToMgr(recentFormPane);
            }
        }
    }
    
    /**
     * Adds the appropriate flavors to make it draggable
     * @param nbi the item to be made draggable
     */
    protected void addDraggableDataFlavors(RolloverCommand roc)
    {
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(DroppableTaskPane.DROPPABLE_PANE_FLAVOR);
    }
    
    /**
     * Save a info request
     * @param infoRequest the ir to be saved
     */
    /**
     * Save a info request
     * @param infoRequest the ir to be saved
     * @param recordSet the recordSet to be saved with it
     */
    public void saveInfoRequest(final InfoRequest infoRequest, final RecordSet recordSet)
    {
        addInfoRequest(infoRequest);

        infoRequest.setTimestampCreated(Calendar.getInstance().getTime());
        infoRequest.setTimestampModified(Calendar.getInstance().getTime());
        
        // save to database
        HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();
        HibernateUtil.getCurrentSession().saveOrUpdate(infoRequest);
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();
        
        NavBoxMgr.getInstance().addBox(navBox);
        
        // XXX This needs to be made generic
        navBox.invalidate();
        navBox.doLayout();
        navBox.repaint();
        
        // ?? 
        //CommandDispatcher.dispatch(new CommandAction("Labels", "NewRecordSet", nbi));
        
    }
    
    /**
     * Delete a record set
     * @param rs the recordSet to be deleted
     */
    protected void deleteInfoRequest(final InfoRequest infoRequest)
    {
        // delete from database
        HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();
        HibernateUtil.getCurrentSession().delete(infoRequest);
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();
         
    }
    
    /**
     * Return a NavBoxItem by name
     * @param boxName the name of the NavBoxItem
     * @return Return a NavBoxItem by name
     */
    protected NavBoxItemIFace getBoxByName(final String boxName)
    {
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            if (((RolloverCommand)nbi).getLabelText().equals(boxName))
            {
                return nbi;
            }
        }
        return null;
    }
    
    /**
     * Delete the RecordSet from the UI, which really means remove the NavBoxItemIFace. 
     * This method first checks to see if the boxItem is not null and uses that, i
     * f it is null then it looks the box up by name ans used that
     * @param boxItem the box item to be deleted
     * @param recordSet the record set that is "owned" by some UI object that needs to be deleted (used for secodary lookup
     */
    protected void deleteInfoRequestFromUI(final NavBoxItemIFace boxItem, final InfoRequest infoRequest)
    {
        
        Component comp = boxItem != null ? boxItem.getUIComponent() : getBoxByName(getTitle(infoRequest)).getUIComponent(); 
        if (comp != null)
        {
            navBox.remove(comp);
            
            // XXX this is pathetic and needs to be generized
            navBox.invalidate();
            navBox.setSize(navBox.getPreferredSize());
            navBox.doLayout();
            navBox.repaint();
            NavBoxMgr.getInstance().invalidate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            UICacheManager.forceTopFrameRepaint();
        }
    }
    
    /**
     * @param commentId
     * @param content
     * @return
     */
    public static String getCommentValueAsStr(final String commentId, final String content)
    {
        int inx = content.indexOf(commentId);
        if (inx > -1)
        {
            inx += commentId.length() + 1;
            int eInx = content.substring(inx).indexOf(" -->") + inx;
            return content.substring(inx, eInx);
        }
        return null;
    }
    
    /**
     * @param commentId
     * @param content
     * @return
     */
    public static int getCommentValueAsInt(final String commentId, final String content)
    {
        String valueStr = getCommentValueAsStr(commentId, content);
        if (valueStr != null)
        {
            return Integer.parseInt(valueStr.trim());
        }
        return -1;
    }
    
    /**
     * @param commentId
     * @param content
     * @return
     */
    public static boolean getCommentValueAsCheck(final String commentId, final String content)
    {
        int inx = content.indexOf(commentId);
        if (inx > -1)
        {
            inx += commentId.length() + 1;
            String subString = content.substring(inx);
            int sInx = subString.indexOf("-->")+1;
            int eInx = subString.indexOf("<!--");
            String boxStr = subString.substring(sInx, eInx).trim().toLowerCase();
            return boxStr.indexOf("x") > -1;
        }
        return false;
    }
    
    /**
     * @param content
     * @return
     */
    public static List<String> parseForCollectionObjects(final String content)
    {
        List<String> list = new ArrayList<String>();
        
        int numItems = getCommentValueAsInt("<!-- NUM_ITEMS", content);
        System.out.println("Num: " + numItems);
        for (int i=0;i<numItems;i++)
        {
            System.out.println("ID: " + getCommentValueAsInt("<!-- ITEM"+i, content));
            System.out.println("Checked: " + getCommentValueAsCheck("<!-- ITEM"+i, content));
            if (getCommentValueAsCheck("<!-- ITEM"+i, content))
            {
                list.add(getCommentValueAsStr("<!-- ITEM"+i, content));
            }
        }
        return list;
    }
    

    /**
     * @param host
     * @param username
     * @param password
     */
    public static void findRepliesFromResearch(String host, String username, String password)
    {
        if (true)
        {
            parseForCollectionObjects("<!-- NUM_ITEMS 2 --><form name=\"form\"><table><tr><td><!-- ITEM0 101 -->[  ]<!-- END --></td><td>Megalotis</td></tr><tr><td><!-- ITEM1 102 -->[  X]<!-- END --></td><td>Megalotis</td></tr></table><form>");
            System.out.println("Should be false "+getCommentValueAsCheck("<!-- ITEM0", "<!-- ITEM0 101 -->[  ]<!-- END -->"));
            System.out.println("Should be true  "+getCommentValueAsCheck("<!-- ITEM0", "<!-- ITEM0 101 -->[X  ]<!-- END -->"));
            System.out.println("Should be true  "+getCommentValueAsCheck("<!-- ITEM0", "<!-- ITEM0 101 -->[  X]<!-- END -->"));
            System.out.println("Should be true  "+getCommentValueAsCheck("<!-- ITEM0", "<!-- ITEM0 101 -->[  X  ]<!-- END -->"));
            return;
        }
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(props, null);

        session.setDebug(false);

        try
        {
            Store store = session.getStore("imap");
            // Store store = session.getStore("pop3");
            store.connect(host, username, password);
            Folder folder = store.getFolder("Test");
            folder.open(Folder.READ_ONLY);

            Message message[] = folder.getMessages();
            for (Message msg : message)
            {
                String subject = msg.getSubject().toLowerCase();
                if (subject.indexOf("re: research request") != -1)
                {
                    String mimeType = msg.getContentType().toLowerCase();
                    if (mimeType.indexOf(EMailHelper.PLAIN_TEXT) != -1)
                    {
                        
                    } else if (mimeType.indexOf(EMailHelper.HTML_TEXT) != -1)
                    {
                        Object contentObj = msg.getContent();
                        if (contentObj instanceof String)
                        {
                            List<String> items = parseForCollectionObjects((String)contentObj);
                        } else
                        {
                            log.error("Not sure of the content's object"+contentObj.getClass().toString());
                        }

                    }
                }

            }
            folder.close(false);
            store.close();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @param host
     * @param username
     * @param password
     */
    public static void findReplies()
    {

        List<javax.mail.Message> msgList = new ArrayList<javax.mail.Message>();
        if (EMailHelper.getAvailableMsgs(msgList))
        {
            try 
            {
                for (Message msg : msgList)
                {
                    String subject = msg.getSubject();
                    if (subject != null && subject.indexOf(INFO_REQ_MESSAGE) > -1)
                    {
                        // Need to make sure it isn't a reply of a reply
                        // we do that by storing requests in the database and then check dates
                        
                        System.out.println(msg.getSubject());
                        System.out.println(msg.getContentType());
                        System.out.println(msg.getContent());
                    
                        if (msg.getContent() instanceof Multipart)
                        {
                            Multipart mp = (Multipart)msg.getContent(); 
                            for (int i=0, n=mp.getCount(); i<n; i++) 
                            { 
                                Part part = mp.getBodyPart(i);
                                for (Enumeration e=part.getAllHeaders();e.hasMoreElements();)
                                {
                                    Header hdr = (Header)e.nextElement();
                                    System.out.println(hdr.getName()+" = "+hdr.getValue());
                                }
                                System.out.println("FNAME: "+part.getFileName());
                                System.out.println("Part:  "+part.getContentType());
                                System.out.println("Desc:  "+part.getDescription());
                                System.out.println("Size:  "+part.getSize());
                                String disposition = part.getDisposition(); 
                                if ((disposition != null) &&  
                                    (disposition.equals(Part.ATTACHMENT) || (disposition.equals(Part.INLINE))))
                                { 
                                    System.out.println(part.getFileName()); 
                                    //part.getInputStream()
                                } else
                                {
                                    System.out.println(part.getContent());
                                }
                            }
                        }
                    }
                }

                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            EMailHelper.closeAllMailBoxes();
            
        } else
        {
            // Display EMailHelper.getLastErrorMsg(); // XXX FIXME
        }
    }
    
    /**
     * @param recordSet
     * @param toEMail
     * @return
     */
    public boolean sendRecordSetToResearcher(final RecordSet recordSet, final String toEMail)
    {
        /*
        Set setOfRecords = recordSet.getItems();
        {
            
        }*/
        return false;
    }    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        recentFormPane = new FormPane(name, this, "Drop Me");
        return recentFormPane;
    }
    
    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------
    
     /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        
        ToolBarDropDownBtn btn = createToolbarButton(INFOREQUEST,   "information.gif",    "inforequest_hint");      
        list.add(new ToolBarItemDesc(btn.getCompleteComp()));
        
        return list;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;
        
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals("Save"))
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                InfoRequest infoRequest = new InfoRequest();
                infoRequest.setEmail("rods@ku.edu");
                infoRequest.setFirstName("Rod");
                infoRequest.setLastName("Spears");
                infoRequest.setInstitution("KU");
                infoRequest.setRequestDate(Calendar.getInstance().getTime());
                
                // Get Info Request Information
                
                saveInfoRequest(infoRequest, (RecordSet)data);
            }
        } else if (cmdAction.getAction().equals("Delete") && cmdAction.getData() instanceof RecordSet)
        {
            InfoRequest inforRequest = (InfoRequest)cmdAction.getData();
            deleteInfoRequest(inforRequest);
            deleteInfoRequestFromUI(null, inforRequest);

        }
    }
    
}
