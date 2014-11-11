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
 *
 */
package edu.ku.brc.specify.plugins.ipadexporter;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.plugins.ipadexporter.ExportPaleo.RelationshipType;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * April 6, 2014
 *
 */
public class VerifyCollectionDlg extends CustomDialog 
{
    private static final String  VERIFY_XML      = "verify.xml";
    
	private JEditorPane                   htmlPane;
	private VerifyCollectionListener      listener;
	private SwingWorker<Integer, Integer> worker;
	private iPadDBExporter                ipadExporter;
	private File                          cacheDir;
	private String                        reportPath;
	private boolean                       isCollectionPaleo;
	
	private static final Map<String, RelationshipType> paleoLookupHash = 
	        Collections.unmodifiableMap(new HashMap<String, RelationshipType>() {{ 
                put("ce", RelationshipType.eColCE);
                put("co", RelationshipType.eColObj);
                put("loc", RelationshipType.eLocality);
	        }});
	
    /**
     * @param cloudHelper
     * @throws HeadlessException
     */
    public VerifyCollectionDlg(final iPadDBExporter ipadExpoter, 
    						   final File           cacheDir,
    		                   final VerifyCollectionListener listener) throws HeadlessException
    {
        super((Frame)getTopWindow(), "", true, OKCANCELHELP, null);
        setAlwaysOnTop(true);
        
        this.listener    = listener;
        this.ipadExporter = ipadExpoter;
        this.cacheDir    = cacheDir;
        this.reportPath  = this.cacheDir + "/VerifyReport.html"; 
        
        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
        if (discipline != null)
        {
            DisciplineType dt = DisciplineType.getByName(discipline.getType());
            this.isCollectionPaleo = dt != null && dt.isPaleo();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @SuppressWarnings({ "unchecked"})
    @Override
    public void createUI()
    {
        loadAndPushResourceBundle(iPadDBExporterPlugin.RESOURCE_NAME);
        setTitle(getResourceString("VERIFY_TITLE"));
        try
        {
            htmlPane = new JEditorPane("text/html", ""); //$NON-NLS-1$
            final JScrollPane scrollPane = UIHelper.createScrollPane(htmlPane);
            //this.add(scrollPane, BorderLayout.CENTER);
            htmlPane.setEditable(false);
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:700px:g", "f:500px:g"));
            contentPanel       = pb.getPanel();

            pb.add(scrollPane, cc.xy(1, 1));
            pb.setDefaultDialogBorder();
            
            setOkLabel("Continue");
            setCancelLabel("Close");
            setHelpLabel("To Browser");
            
            worker = new SwingWorker<Integer, Integer>()
            {
                @Override
                protected Integer doInBackground() throws Exception
                {
                	processResults();
                    return 0;
                }
                @Override
                protected void done()
                {
                    super.done();
                    
                    popResourceBundle();
                }
            };
            //addProgressListener(worker);
            worker.execute();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            popResourceBundle();
        }
        
        setSize(800, 650);
        setBounds(0, 0, 650, 650);
        super.createUI();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Must be called at the end 'createUI'
    }
    
    /**
     * @return
     */
    public static Map<String, RelationshipType> getPaleoLookupHash()
    {
        return paleoLookupHash;
    }
    
    private Integer cnvToInt(final Object rv)
    {
        if (rv instanceof String)
        {
        	return Integer.parseInt(((String)rv));
        }
        if (rv instanceof Double)
        {
        	return ((Double)rv).intValue();
        }
        if (rv instanceof Float)
        {
        	return((Float)rv).intValue();
        }
        if (rv instanceof Long) 
        {
        	return((Long)rv).intValue();
        }
        if (rv instanceof BigDecimal) 
        {
        	return ((BigDecimal)rv).intValue();
        }
        return null;
    }
    
    
    private void listMsgs(final StringBuilder sb, 
    		              final String title, 
    		              final ArrayList<String> items, 
    		              final String color,
    		              final boolean isHTML3)
    {
    	if (items.size() > 0)
    	{
    		String htmlTitle;
    		if (isHTML3)
    		{
    			htmlTitle = String.format("<BR><b><font color='%s'>%s</font></b><br><font color='black'><UL>", color, title);
    		} else
    		{
    			htmlTitle = String.format("<h2 style='color:%s'>%s</h2><UL>", color, title);
    		}
	    	sb.append(htmlTitle);
	    	for (String s : items)
	    	{
	    		//String clsStr = String.format(" class='%s'", cls);
	    		//sb.append(String.format("<LI%s>%s</LI>", (isHTML3 ? "" : clsStr), s));
	    		sb.append(String.format("<LI>%s</LI>", s));
	    	}
	    	sb.append("</UL>");
    	}
    	if (isHTML3) sb.append("</font>");
    }
    
    /**
     * 
     */
    private void processResults()
    {
        //loadAndPushResourceBundle("stats");
        
        RelationshipType paleoRelType = isCollectionPaleo ? ExportPaleo.discoverPaleRelationshipType() : RelationshipType.eTypeError; 
    	
        boolean hasCritical = false;

        UIRegistry.setDoShowAllResStrErors(false);
        //logMsg("Verifying the Collection...");
        
        File tmpFile = ipadExporter.getConfigFile(VERIFY_XML);
        if (tmpFile != null && tmpFile.exists())
        {
            Statement stmt0  = null;
            try
            {
                Element root = XMLHelper.readFileToDOM4J(tmpFile);
                if (root != null)
                {
                	ArrayList<String> okMsgs       = new ArrayList<String>(); 
                	ArrayList<String> warnMsgs     = new ArrayList<String>(); 
                	ArrayList<String> criticalMsgs = new ArrayList<String>(); 
                	int issueCnt = 0;
                	
                	String mainFont = "<font face='verdana' color='black'>";
                	String headHTML = "<htmL><head></head><body bgcolor='#EEEEEE'>" + mainFont;
                	StringBuilder sb = new StringBuilder(headHTML);
                	htmlPane.setText(sb.toString()+"<BR><BR>Verifying collection...</font></body></html>");
                	
                    List<?> items = root.selectNodes("eval"); //$NON-NLS-1$
        
                    stmt0  = DBConnection.getInstance().getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    stmt0.setFetchSize(Integer.MIN_VALUE);
                                        
                    for (Iterator<?> capIter = items.iterator(); capIter.hasNext(); )
                    {
                        Element fieldNode = (Element)capIter.next();
                        //String  name      = fieldNode.attributeValue("name"); //$NON-NLS-1$
                        String  desc      = fieldNode.attributeValue("desc"); //$NON-NLS-1$
                        String  sql       = fieldNode.getTextTrim();
                        String  cond      = fieldNode.attributeValue("cond");
                        String  vStr      = fieldNode.attributeValue("val");
                        String  isFmt     = fieldNode.attributeValue("fmt");
                        String  stop      = fieldNode.attributeValue("stop");
                        boolean doStop    = stop != null && stop.equals("true");
                        
                        String  display   = fieldNode.attributeValue("display");
                        boolean doDsp     = display == null || display.equals("true");
                        
                        String  paleo     = fieldNode.attributeValue("isPaleo");
                        boolean isPaleo   = paleo != null && paleo.equals("true");
                        if (isPaleo && !isCollectionPaleo) continue;
                        
                        String  paleoTypeStr = fieldNode.attributeValue("paleotype"); //$NON-NLS-1$
                        if (isCollectionPaleo && StringUtils.isNotEmpty(paleoTypeStr))
                        {
                            if (paleoRelType !=  paleoLookupHash.get(paleoTypeStr))
                            {
                                continue;
                            }
                        }
                        
                        sql = ipadExporter.adjustSQL(sql);
                        
                        Object  rv     = BasicSQLUtils.querySingleObj(sql);
                        Integer retVal = cnvToInt(rv);

                        boolean isError = false;
                        if (retVal != null && StringUtils.isNotEmpty(cond) && StringUtils.isNotEmpty(vStr))
                        {
                        	Integer value = cnvToInt(vStr);
                        	if (value != null)
                        	{
                        		if (cond.equals(">"))
                        		{
                        			isError = retVal.intValue() > value.intValue();
                        		} else if (cond.equals("="))
                        		{
                        			isError = retVal.intValue() == value.intValue();
                        		} else if (cond.equals("<"))
                        		{
                        			isError = retVal.intValue() < value.intValue();
                        		}
                        	}
                        }
/*
                        String fontSt = isError ? "<font color='"+(doStop ? "red" : "orange")+"'>" : "";
                        String fontEn = isError ? "</font>" : "";
                        if (StringUtils.isNotEmpty(isFmt) && isFmt.equalsIgnoreCase("true"))
                        {
                    		sb.append(String.format("<LI>%s%s%s</LI>", fontSt, String.format(desc,  retVal), fontEn));
                    		issueCnt++;
                        } else
                        {
                        	sb.append(String.format("<LI>%s%s%s</LI>", fontSt, desc, fontEn));
                        	issueCnt++;
                        }                        
 */
                        String fullMsg;
                        if (StringUtils.isNotEmpty(isFmt) && isFmt.equalsIgnoreCase("true"))
                        {
                        	fullMsg = String.format(desc,  retVal);
                        } else
                        {
                        	fullMsg = desc;
                        }
                        
                        if (isError)
                        {
                        	if (doStop)
                        	{
                        		criticalMsgs.add(fullMsg);
                        		hasCritical = true;
                        	} else
                        	{
                        		warnMsgs.add(fullMsg);
                        	}
                        	
                        } else if (doDsp)
                        {
                        	okMsgs.add(fullMsg);
                        }

                        issueCnt++;
                        //worker.firePropertyChange(PROGRESS, 0, cnt);
                    }
                    stmt0.close();
                    
                    sb = new StringBuilder(headHTML);
                    if (issueCnt == 0)
                    {
                    	sb.append("<BR><BR>There were no issues to report.");
                    } else
                    {
                    	listMsgs(sb, "Passed", okMsgs, "green", true);
                    	listMsgs(sb, "Warnings", warnMsgs, "yellow", true);
                    	listMsgs(sb, "Critical Errors - Cannot proceed.", criticalMsgs, "red", true);
                    }
                    sb.append(mainFont + "<BR>Verification Complete.<BR><BR></font></body></html>");
                    
                    htmlPane.setText(sb.toString());
                    
                    // For external report
                    sb = new StringBuilder("<htmL><head><title>Collection Verification</title></head><body>");
                	listMsgs(sb, "Passed", okMsgs, "green", false);
                	listMsgs(sb, "Warnings", warnMsgs, "yellow", false);
                	listMsgs(sb, "Critical Errors - Cannot proceed.", criticalMsgs, "red", false);
                	sb.append("</body></html>");
                	try
                    {
                		TableWriter tblWriter = new TableWriter(reportPath, "Collection Verification Report", true);
                		tblWriter.println(sb.toString());
                		tblWriter.close();
                		
                    } catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
            	try
            	{
            		if (stmt0 != null)  stmt0.close();
            	} catch (Exception ex) {}
            }
        }
        okBtn.setEnabled(!hasCritical);
    }
    
    /**
     * Performs cancel action.
     */
    protected void cancelButtonPressed()
    {
        super.cancelButtonPressed();
        if (listener != null)
        {
        	listener.cancelPressed();
        }
    }

    /**
     * Performs ok action.
     */
    protected void okButtonPressed()
    {
    	super.okButtonPressed();
        if (listener != null)
        {
        	listener.okPressed();
        }
    }
    
    /**
     * Performs help action.
     */
    protected void helpButtonPressed()
    {
        try
        {
            AttachmentUtils.openURI((new File(reportPath)).toURI());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            
        } 
    }
 }
