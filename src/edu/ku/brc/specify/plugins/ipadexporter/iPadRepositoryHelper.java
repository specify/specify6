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
package edu.ku.brc.specify.plugins.ipadexporter;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;

import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.prefs.FormattingPrefsPanel;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Apr 30, 2012
 *
 */
public class iPadRepositoryHelper
{
    private static MessageDigest sha1       = null;
    //public static final String  baseURLStr = "http://anza.nhm.ku.edu/ipad/";
    public static final String  baseURLStr = "http://specify6-prod.nhm.ku.edu/ipad/";

    private boolean                 networkConnError   = false;
    private byte[]                  bytes              = new byte[100*1024];

    private String                  sha1Hash           = null;
    
    // URLs
    //private String                  readURLStr  = null;
    private String                  writeURLStr = baseURLStr + "handler.php";
    //private String                  delURLStr   = null;
    
    private String[]                symbols = {"coll", "disp", "div", "inst", "spuser", "agent", "colmgr", "icon", "curator", "disptype"};
    private String[]                values  = new String[symbols.length];
    
    static
    {
        try
        {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    public iPadRepositoryHelper()
    {
        super();
    }
    
    /**
     * @param ids
     * @return
     */
    private String getInClause(final List<Integer> ids)
    {
        StringBuffer inClause = new StringBuffer();
        inClause.append("(");
        boolean first = true;
        for (Integer id : ids)
        {
            if (!first) 
            {
                inClause.append(", ");
            }
            else 
            {
                first = false;
            } 
            inClause.append(id);
        }
        inClause.append(")");
        return inClause.toString();
    }
    
    /**
     * @param session
     * @param collection
     * @return
     */
    private Agent chooseContentMgr(DataProviderSessionIFace session, 
                                   final Collection collection,
                                   final Agent usrAgent)
    {
        String sql      = QueryAdjusterForDomain.getInstance().adjustSQL("SELECT a.AgentID FROM specifyuser su INNER JOIN agent a ON su.SpecifyUserID = a.SpecifyUserID WHERE a.DivisionID = DIVID");
        String inClause = getInClause(BasicSQLUtils.queryForInts(sql));
        sql = String.format("FROM Agent a WHERE a.id IN %s", inClause);
        
        @SuppressWarnings("unchecked")
        List<Agent> agents = (List<Agent>)session.getDataList(sql);
        if (agents != null)
        {
            if (agents.size() > 1)
            {
                Vector<Agent> agentsLst = new Vector<Agent>(agents);
                agentsLst.add(usrAgent);
                ChooseFromListDlg<Agent> dlg = new ChooseFromListDlg<Agent>((Frame)UIRegistry.getTopWindow(), "Choose Agent", agentsLst);
                UIHelper.centerAndShow(dlg);
                if (!dlg.isCancelled())
                {
                    return dlg.getSelectedObject();
                }
                
            } else if (agents.size() == 1)
            {
                return agents.get(0);
            }
            return usrAgent;
        }
        return null;
    }
    
    /**
     * @param collectionArg
     * @return
     */
    private Pair<String, String> getCuratorName(final Collection collection)
    {
        loadAndPushResourceBundle(iPadDBExporterPlugin.RES_NAME);   
        
        AppPreferences  remotePrefs = AppPreferences.getRemote();
        String          curatorPref = "IPAD_CURATOR_NAME_" + collection.getId();
        String          colMgrPref  = "IPAD_COLMGR_NAME_" + collection.getId();
        String          curatorName = remotePrefs.get(curatorPref, "");
        String          colMgrName  = remotePrefs.get(colMgrPref, "");
        CellConstraints cc          = new CellConstraints();
        PanelBuilder    pb          = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p"));
        
        final JTextField crTextFld = UIHelper.createTextField(curatorName);
        final JTextField cmTextFld = UIHelper.createTextField(colMgrName);
        pb.add(UIHelper.createI18NFormLabel("Curator"), cc.xy(1, 1));
        pb.add(crTextFld, cc.xy(3, 1));
        
        pb.add(UIHelper.createI18NFormLabel("COLMGR"), cc.xy(1, 3));
        pb.add(cmTextFld, cc.xy(3, 3));
        
        pb.setDefaultDialogBorder();
        
        final CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), getResourceString("CURATOR_NM"), true, CustomDialog.OKCANCEL, pb.getPanel());
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        popResourceBundle();

        KeyAdapter ka = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                //System.out.println(!crTextFld.getText().isEmpty() && !cmTextFld.getText().isEmpty());
                dlg.getOkBtn().setEnabled(!crTextFld.getText().isEmpty() && !cmTextFld.getText().isEmpty());
            }
        };
        crTextFld.addKeyListener(ka);
        cmTextFld.addKeyListener(ka);
        dlg.createUI();
        dlg.getOkBtn().setEnabled(!crTextFld.getText().isEmpty() && !cmTextFld.getText().isEmpty());
        UIHelper.centerAndShow(dlg, 500, null);
        
        if (!dlg.isCancelled())
        {
            curatorName = crTextFld.getText();
            remotePrefs.put(curatorPref, curatorName);
            
            colMgrName = cmTextFld.getText();
            remotePrefs.put(colMgrPref, colMgrName);
            
            try {
                remotePrefs.flush();
            } catch (BackingStoreException e1){}
            
            return new Pair<String, String>(curatorName, colMgrName);
        }
        return null;
    }
    
    /**
     * @param collectionArg
     * @return
     */
    private Agent getContentManager(final Collection collectionArg)
    {
        Set<Agent>               contentMgrs = null;
        DataProviderSessionIFace session     = null;
        Collection               collection  = collectionArg;
        try 
        {
            session = DataProviderFactory.getInstance().createSession();
            collection = session.get(Collection.class, collection.getId());
            
            contentMgrs = collection.getContentContacts();
            if (contentMgrs != null)
            {
                if (contentMgrs.size() == 0)
                {
                    Agent usrAgent = AppContextMgr.getInstance().getClassObject(Agent.class);
                    Agent agent    = chooseContentMgr(session, collection, usrAgent);
                    if (agent != null)
                    {
                        if (!agent.getId().equals(usrAgent))
                        {
                            contentMgrs.add(agent);
                            try
                            {
                                session.beginTransaction();
                                session.save(collection);
                                session.commit();
                                
                                AppContextMgr.getInstance().setClassObject(Collection.class, collection);
                                
                            } catch (Exception e)
                            {
                                session.rollback();
                            }
                        }
                        return agent;
                    }
                } else
                {
                    return contentMgrs.iterator().next();
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            if (session != null) session.close();
        }
        return null;
    }

    /**
     * 
     */
    public Map<String, String> getAuxilaryInfo()
    {
        AppContextMgr appMgr = AppContextMgr.getInstance();
        
        TreeMap<String, String> auxInfoMap = new TreeMap<String, String>();
        if (appMgr != null)
        {
            Collection  coll   = appMgr.getClassObject(Collection.class);
            Discipline  disp   = appMgr.getClassObject(Discipline.class);
            Division    div    = appMgr.getClassObject(Division.class);
            Institution inst   = appMgr.getClassObject(Institution.class);
            SpecifyUser spUser = appMgr.getClassObject(SpecifyUser.class);
            Agent       agent  = appMgr.getClassObject(Agent.class);
            
            /*Agent colMgr = getContentManager(coll);
            if (colMgr == null)
            {
                return null;
            }
            coll = appMgr.getClassObject(Collection.class);
            
            String curator = colMgr.toString();*/
            
            
            
            String iconName = AppPreferences.getRemote().get(FormattingPrefsPanel.getDisciplineImageName(), "CollectionObject"); //$NON-NLS-1$ //$NON-NLS-2$
            if (iconName.startsWith("Collection") || !iconName.endsWith("png"))
            {
                IconEntry entry = IconManager.getIconEntryByName(disp.getType());
                if (entry != null)
                {
                    iconName = IconManager.getIconEntryByName(disp.getType()).getUrl().toString();
                    iconName = iconName.substring(iconName.lastIndexOf('/')+1, iconName.length());
                }
            }
     
            Pair<String, String> names = getCuratorName(coll);
            if (names == null) return null;
            
            values[0] = coll.getCollectionName();
            values[1] = disp.getName();
            values[2] = div.getName();
            values[3] = inst.getName();
            values[4] = spUser.getName();
            values[5] = agent.toString();
            values[6] = StringUtils.isEmpty(names.second) ? agent.toString() : names.second;//colMgr != null ? colMgr.toString() : values[5];
            values[7] = iconName;
            values[8] = StringUtils.isEmpty(names.first) ? agent.toString() : names.first;
            values[9] = disp.getType();
            
            for (int i=0;i<values.length;i++)
            {
                auxInfoMap.put(symbols[i], values[i]);
            }
        }
        return auxInfoMap;
    }
    
    /**
     * @param file
     * @return
     * @throws Exception
     */
    private String calculateHash(final File file) throws Exception
    {
        if (sha1 != null)
        {
            FileInputStream     fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DigestInputStream   dis = new DigestInputStream(bis, sha1);
    
            // read the file and update the hash calculation
            while (dis.read() != -1)
                ;
    
            // get the hash value as byte array
            byte[] hash = sha1.digest();

            return byteArray2Hex(hash);
        }
        return null;
    }

    /**
     * @param hash
     * @return
     */
    private String byteArray2Hex(byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    /**
     * @return the sha1Hash
     */
    public String getSha1Hash()
    {
        return sha1Hash;
    }

    /**
     * @param targetFile
     * @param fileName
     * @return
     */
    public synchronized boolean sendFile(final File targetFile, 
                                         final String fileName)
    {
        return sendFile(targetFile, fileName, "");
    }
    
    /**
     * @param targetFile
     * @param fileName
     * @param dirName
     * @return
     */
    public synchronized boolean sendFile(final File   targetFile, 
                                         final String fileName,
                                         final String dirName)
    {
        String     targetURL = writeURLStr;
        PostMethod filePost  = new PostMethod(targetURL);

        try
        {
            sha1Hash = null;
            sha1Hash = calculateHash(targetFile);
            
            //System.out.println("Uploading " + targetFile.getName() + " to " + targetURL+ "Src Exists: "+targetFile.exists());
            //System.out.println("Hash [" + sha1Hash + "]");
                    
            Part[] parts = {
                    new FilePart(targetFile.getName(), targetFile),
                    new StringPart("store", fileName),
                    new StringPart("dir", dirName),
                    new StringPart("hash", sha1Hash == null ? "" : sha1Hash),
                    new StringPart("action", "upload"),
                };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            int status = client.executeMethod(filePost);
            
            //System.out.println(filePost.getResponseBodyAsString());

            if (status == HttpStatus.SC_OK)
            {
                System.err.println("HTTP Status: OK");
                return true;
            } else
            {
                System.err.println("HTTP Status: "+status);
                System.err.println(filePost.getResponseBodyAsString());
            }
            
        } catch (java.net.UnknownHostException uex)
        {
            networkConnError = true;
            
            
        } catch (Exception ex)
        {
            System.out.println("Error:  " + ex.getMessage());
            ex.printStackTrace();
            
        } finally
        {
            filePost.releaseConnection();
        }
        return false;
    }
    
    /**
     * @param urlStr
     * @param tmpFile
     * @return
     */
    public boolean fillFileFromWeb(final String urlStr, final File tmpFile)
    {
        networkConnError = false;
        try
        {
            URL url = new URL(urlStr);
            InputStream inpStream = url.openStream();
            if (inpStream != null)
            {
                BufferedInputStream  in  = new BufferedInputStream(inpStream);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
                
                do
                {
                    int numBytes = in.read(bytes);
                    if (numBytes == -1)
                    {
                        break;
                    }
                    bos.write(bytes, 0, numBytes);
                    
                } while(true);
                in.close();
                bos.close();
            
                return true;
            }
            
        } catch (java.net.UnknownHostException uex)
        {
            networkConnError = true;
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
            
            /*int inx = urlStr.lastIndexOf('/');
            File inFile = new File("/Users/rods/workspace/SQLiteExample/"+urlStr.substring(inx));
            try
            {
                FileUtils.copyFile(inFile, tmpFile);
            } catch (IOException e)
            {
                e.printStackTrace();
            }*/
        }
        
        return false;
    }

    /**
     * @return the networkConnError
     */
    public boolean isNetworkConnError()
    {
        return networkConnError;
    }
    
    /**
     * @param args
     */
    /*public static void main(String[] args)
    {
        String urlStr = "http://specifyassets.nhm.ku.edu/Informatics/web_asset_store.xml";
        iPadRepositoryHelper wst = new iPadRepositoryHelper();
        wst.sendFile(new File("tasks.xml"), "xxx.xml");
    }*/

}
