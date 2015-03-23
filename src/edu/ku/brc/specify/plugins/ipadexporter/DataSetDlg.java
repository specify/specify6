/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins.ipadexporter;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.HTTPGetter;
import edu.ku.brc.helpers.HTTPGetter.ErrorCode;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 28, 2012
 *
 */
public class DataSetDlg extends CustomDialog
{
    private IPadCloudIFace cloudHelper;
    
    private JTextField  instNameTF;
    private int         nameLen;
    private JLabel      statusLbl;
    private Institution inst;
    
    private String      guid;
    
    // Institution on Cloud
    private Integer cloudInstId = null;
    
    /**
     * @param cloudHelper
     * @throws HeadlessException
     */
    public DataSetDlg(final IPadCloudIFace cloudHelper) throws HeadlessException
    {
        super((Frame)getTopWindow(), getResourceString("INST_INFO"), true, OKCANCEL, null);
        
        inst = AppContextMgr.getInstance().getClassObject(Institution.class);
        guid = inst.getGuid();
        
        this.cloudHelper = cloudHelper;
        
        boolean doDebug = true;
        if (doDebug)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                inst = session.get(Institution.class, inst.getId());
                AppContextMgr.getInstance().setClassObject(Institution.class, inst);
            }
            catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                e.printStackTrace();
            }
            finally
            {
                if (session != null) session.close();
            }
        }
        
        cloudInstId = StringUtils.isNotEmpty(guid) ? cloudHelper.getInstId(guid) : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,2px,p,8px"));
        DBTableInfo     ti = DBTableIdMgr.getInstance().getInfoById(Institution.getClassTableId());
        
        DBFieldInfo fi = ti.getFieldByName("name");
        JLabel nmLabel = UIHelper.createFormLabel(fi.getTitle());
        instNameTF     = new ValTextField();
        nameLen        = fi.getLength();
        
        statusLbl = UIHelper.createLabel("");
        
        contentPanel = pb.getPanel();
        
        int y = 1;
        pb.add(nmLabel,    cc.xy(1, y)); 
        pb.add(instNameTF, cc.xy(3, y)); y+= 2;
        
        pb.add(statusLbl, cc.xyw(1, y, 3)); y+= 2;
        
        pb.setDefaultDialogBorder();
        
        KeyAdapter ka = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                okBtn.setEnabled(isValidInput(instNameTF.getText()));
            }
        };
        instNameTF.addKeyListener(ka);
        
        super.createUI();
        
        String title = cloudInstId == null ? inst.getName() : "";
        instNameTF.setText(title);
        
        okBtn.setEnabled(isValidInput(instNameTF.getText()));
    }
    
    /**
     * @return
     */
    public boolean isInstOK()
    {
        return cloudInstId != null;
    }
    
    /**
     * @return the instId
     */
    public Integer getInstId()
    {
        return cloudInstId;
    }
    
    /**
     * @return the guid
     */
    public String getGuid()
    {
        return guid;
    }

    /**
     * @param title
     * @param uri
     * @param code
     * @return
     */
    private boolean saveToLocalDB(final String title)
    {
        boolean           isOK  = false;
        Connection        conn  = DBConnection.getInstance().getConnection();
        PreparedStatement pStmt = null;
        try
        {
            String sql = "UPDATE institution SET Name=?, TimestampModified=? WHERE InstitutionID=?";
            pStmt = conn.prepareStatement(sql);
            pStmt.setString(1, title);
            pStmt.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            pStmt.setInt(3, inst.getId());

            isOK =  pStmt.executeUpdate() == 1;
            pStmt.close();
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                inst = session.get(Institution.class, inst.getId());
                AppContextMgr.getInstance().setClassObject(Institution.class, inst);
            }
            catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                e.printStackTrace();
            }
            finally
            {
                if (session != null) session.close();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return isOK;
    }
    
    /**
     * @param uri
     * @return
     */
    @SuppressWarnings("unused")
    private boolean doesWebSiteExists(final String uri)
    {
        try
        {
            HTTPGetter getter = new HTTPGetter();
            getter.setThrowingErrors(false);
            byte[] bytes = getter.doHTTPRequest(uri);
            return bytes != null && getter.getStatus() == ErrorCode.NoError;
        } catch (Exception ex) {}
        return false;
    }
    
    /**
     * @param title
     * @param webSite
     * @return
     */
    private boolean isValidInput(final String title)
    {
        String nmStr  = instNameTF.getText();
        if (StringUtils.isNotEmpty(nmStr) && StringUtils.isNotEmpty(nmStr))
        {
            statusLbl.setText("");
            return true;
        }
        return false;
    }
    
    /**
     * @param uri
     * @return
     */
//    private static boolean isValidWebAddr(final String uri)
//    {
//        final String prefix = "http://";
//        String uriStr = uri;
//        if (!uriStr.startsWith(prefix))
//        {
//            uriStr = prefix + uri;
//        }
//        String pattern = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
//                //"^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$";//"\\b(http)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]";
//        try
//        {
//            Pattern patt = Pattern.compile(pattern);
//            Matcher matcher = patt.matcher(uriStr);
//            System.out.println("URL: "+ matcher.matches()+"  "+uriStr);
//            return matcher.matches();
//        } catch (RuntimeException e)
//        {
//        }
//        return false;
//    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        String nmStr  = instNameTF.getText();
        if (nmStr.length() > nameLen)
        {
            nmStr = nmStr.substring(0, nameLen);
        }
        if (saveToLocalDB(nmStr))
        {
            statusLbl.setText("");
            
            boolean isOK           = false;
            Integer existingInstId = cloudHelper.getInstId(guid);
            if ((cloudInstId == null && existingInstId != null) || (cloudInstId != null && existingInstId != null && cloudInstId.equals(existingInstId)))
            {
                isOK = true;
                cloudInstId = existingInstId;
            } else
            {
                // Needs to be fixed
//                Integer newInstId = cloudHelper.saveInstitutionInfo(cloudInstId, nmStr, "", "", guid);
//                if ((cloudInstId == null && newInstId != null) || (cloudInstId != null && newInstId != null && cloudInstId.equals(newInstId)))
//                {
//                    isOK = true;
//                    cloudInstId = newInstId;
//                }
            }
            
            if (isOK)
            {
                super.okButtonPressed();   
            } else
            {
                statusLbl.setText("Unable to save the Institution Information.");
                okBtn.setEnabled(false);
            }
        } else
        {
            statusLbl.setText("Unable to save the Institution Information to local database.");
        }
    }
}
