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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createI18NCheckBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.utilapps.TaxonFileDesc;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This is the configuration window for preloading taxon.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 21, 2009
 *
 */
public class TaxonLoadSetupPanel extends BaseSetupPanel
{
    //private static final Logger log           = Logger.getLogger(TaxonLoadSetupPanel.class);
    private static final String XLS              = "xls";
    private static final String DWNLD_TAX_URL    = "DWNLD_TAX_URL";
    private static final String BAD_TAXON_DEF_NC = "BAD_TAXON_DEF_NC";
    private static final String BAD_TAXON_URL    = "BAD_TAXON_URL";
    private static final String BAD_TAXON_DEF_DL = "BAD_TAXON_DEF_DL";
    
    
    protected JCheckBox          preloadChk;
    
    protected JLabel             fileLbl;
    protected JComboBox          fileCBX;
    
    protected JLabel             srcLbl;
    protected JTextField         srcTF;
    
    protected JLabel             coverageLbl;
    protected JTextField         coverageTF;
    
    protected JLabel             descLbl;
    protected JTextArea          descTA;
    
    protected JLabel             otherLbl;
    protected ValBrowseBtnPanel  otherBrw;
    protected ValTextField       otherTF;
    
    protected Component          stdSep;
    protected Component          othSep;
    
    protected boolean            firstTime = true;
    protected String             downloadedFileName = null;
   
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public TaxonLoadSetupPanel(final String helpContext, final JButton nextBtn, final JButton prevBtn)
    {
        super("PRELOADTXN", helpContext, nextBtn, prevBtn);
        
        String header = getResourceString("PRELOADTXN_INFO");

        CellConstraints cc = new CellConstraints();
        
        
        String rowDef = "p,10px," + createDuplicateJGoodiesDef("p", "2px", 8) + ",p:g";
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", rowDef), this);
        int row = 1;
        
        stdSep = builder.add(createLabel(header, SwingConstants.CENTER), cc.xywh(1,row,3,1));
        row += 2;
        
        fileCBX    = createComboBox();
        srcTF      = createTextField("");
        coverageTF = createTextField("");
        descTA     = createTextArea();
        otherBrw   = new ValBrowseBtnPanel(otherTF = new ValTextField(), false, true);
        otherBrw.setUseNativeFileDlg(true);
        
        descTA.setEditable(false);
        descTA.setColumns(30);
        descTA.setRows(5);
        descTA.setWrapStyleWord(true);
        descTA.setLineWrap(true);
        
        ViewFactory.changeTextFieldUIForDisplay(srcTF, false);
        ViewFactory.changeTextFieldUIForDisplay(coverageTF, false);
        
        builder.add(preloadChk = createI18NCheckBox("TFD_LOAD_TAXON"), cc.xy(1, row));
        row += 2;
        
        builder.addSeparator(UIRegistry.getResourceString("TFD_SEP_STD"), cc.xyw(1, row, 3));
        row += 2;
        
        builder.add(fileLbl = createI18NFormLabel("TFD_FILE_LBL"), cc.xy(1, row));
        builder.add(fileCBX,          cc.xy(3, row));
        row += 2;
        
        builder.add(srcLbl = createI18NFormLabel("TFD_SRC_LBL"), cc.xy(1, row));
        builder.add(srcTF,          cc.xy(3, row));
        row += 2;
        
        builder.add(coverageLbl = createI18NFormLabel("TFD_CVRG_LBL"), cc.xy(1, row));
        builder.add(coverageTF,       cc.xy(3, row));
        row += 2;
        
        builder.add(descLbl = createI18NFormLabel("TFD_DESC_LBL"), cc.xy(1, row));
        builder.add(createScrollPane(descTA),          cc.xy(3, row));
        row += 2;
        
        othSep = builder.addSeparator(UIRegistry.getResourceString("TFD_SEP_OTH"), cc.xyw(1, row, 3));
        row += 2;
        
        builder.add(otherLbl = createI18NFormLabel("TFD_OTHER_LBL"), cc.xy(1, row));
        builder.add(otherBrw,        cc.xy(3, row));
        row += 2;
        
        updateBtnUI();
        
        otherTF.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                updateBtnUI();
            }
        });
        

        preloadChk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean checked = preloadChk.isSelected();
                if (checked)
                {
                    if (fileCBX.getModel().getSize() > 0 && fileCBX.getSelectedIndex() == -1)
                    {
                        fileCBX.setSelectedIndex(0);
                    }
                    enableUI(otherTF.getText().isEmpty(), true, true); 
                    
                } else
                {
                    enableUI(false, true, false);
                }
            }
        });
        
        otherTF.addFocusListener(new FocusAdapter() {

            /* (non-Javadoc)
             * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
             */
            @Override
            public void focusLost(FocusEvent e)
            {
                updateBtnUI();
            }
            
        });
        
        otherBrw.setNativeDlgFilter(new FilenameFilter() {

            /* (non-Javadoc)
             * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
             */
            @Override
            public boolean accept(File dir, String name)
            {
                return name.toLowerCase().endsWith(XLS);
            }
        });
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        String fileName = otherTF.getText();
        if (fileName.isEmpty())
        {
            TaxonFileDesc tfd = (TaxonFileDesc)fileCBX.getSelectedItem();
            if (tfd != null && FilenameUtils.isExtension(tfd.getFileName().toLowerCase(), XLS))
            {
                fileName = downloadedFileName;
            }
        }
        
        if (!otherTF.getText().isEmpty() || StringUtils.isNotEmpty(fileName))
        {
            props.put("othertaxonfile", !otherTF.getText().isEmpty());
            props.put("taxonfilename", fileName != null ? fileName : "");
            props.put("preloadtaxon", preloadChk.isSelected());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        super.setValues(values);
        updateBtnUI();
    }
    
    /**
     * @return
     */
    private String getTaxonDOMStr()
    {
        URL url;
        try
        {
            String downloadHTTP = UIRegistry.getResourceString(DWNLD_TAX_URL);
            if (StringUtils.isNotEmpty(downloadHTTP))
            {
                url = new URL(downloadHTTP + "/taxonfiles.xml");
                URLConnection  conn = url.openConnection();
                BufferedReader in   = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
    
                StringBuilder sb = new StringBuilder();
                while ((line = in.readLine()) != null)
                {
                    sb.append(line);
                }
                in.close();
               
                return sb.toString();
                
            } else
            {
                UIRegistry.showLocalizedError(BAD_TAXON_URL);
            }
           
        } catch (java.net.SocketException e)
        {
            UIRegistry.showLocalizedError(BAD_TAXON_DEF_NC);
            
        } catch (java.net.UnknownHostException e)
        {
            UIRegistry.showLocalizedError(BAD_TAXON_DEF_NC);
            
        } catch (Exception e)
        {
            e.printStackTrace();
            UIRegistry.showLocalizedError(BAD_TAXON_DEF_DL);
        }
        return null;
    }
   
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    private Vector<TaxonFileDesc> readTaxonLoadFiles()
    {
        String taxonXML = getTaxonDOMStr();
        while (taxonXML == null || taxonXML.length() < 1024)
        {
            int rv = UIRegistry.askYesNoLocalized("DWNLD_TRY_AGAIN", "SKIP", UIRegistry.getResourceString(BAD_TAXON_DEF_DL), "WARNING");
            if (rv == JOptionPane.NO_OPTION)
            {
                return null;
            }
            taxonXML = getTaxonDOMStr();
        }
       
        XStream xstream = new XStream();
        TaxonFileDesc.configXStream(xstream);
       
        return (Vector<TaxonFileDesc>)xstream.fromXML(taxonXML);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        super.doingNext();
        
        DisciplineType disciplineType = (DisciplineType)properties.get("disciplineType");
        if (disciplineType != null)
        {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            Vector<TaxonFileDesc> taxonFileDescs = readTaxonLoadFiles();
            if (taxonFileDescs != null)
            {
                for (TaxonFileDesc tfd : taxonFileDescs)
                {
                    if (tfd.getDiscipline().equals(disciplineType.getName()))
                    {
                        model.addElement(tfd);
                    }
                }
                
                fileCBX.setModel(model);
                if (model.getSize() > 0)
                {
                    fileCBX.setSelectedIndex(0);
                }
                
                if (firstTime)
                {
                    firstTime = false;
                    fileCBX.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            fileSelected();
                        }
                    });
                }
                fileSelected();
                
            } else
            {
                fileCBX.setEnabled(false);
                preloadChk.setEnabled(false);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingPrev()
     */
    @Override
    public void doingPrev()
    {
        super.doingPrev();
        
        otherBrw.setValue(null, null);
        fileCBX.setSelectedIndex(fileCBX.getModel().getSize() > 0 ? 0 : -1);
        coverageTF.setText("");
        srcTF.setText("");
        descTA.setText("");
        
        properties.remove("othertaxonfile");
        properties.remove("taxonfilename");
        properties.remove("preloadtaxon");
        
        if (preloadChk.isSelected())
        {
            preloadChk.doClick();
        }
    }
    
    /**
     * 
     */
    private void fileSelected()
    {
        TaxonFileDesc tfd = (TaxonFileDesc)fileCBX.getSelectedItem();
        if (tfd != null)
        {
            srcTF.setText(tfd.getSrc());
            coverageTF.setText(tfd.getCoverage());
            descTA.setText(tfd.getDescription());
        }
        updateBtnUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#aboutToLeave()
     */
    @Override
    public void aboutToLeave()
    {
        if (preloadChk.isSelected())
        {
            TaxonFileDesc tfd = (TaxonFileDesc)fileCBX.getSelectedItem();
            if (tfd != null && FilenameUtils.isExtension(tfd.getFileName().toLowerCase(), XLS))
            {
                File txFile = getFileForTaxon(tfd.getFileName(), false);
                if (txFile == null || !txFile.exists())
                {
                    File userDataFile = new File(UIRegistry.getAppDataDir() + File.separator + tfd.getFileName());
                    if (!userDataFile.exists())
                    {
                        doDownloadTaxonFile(tfd.getFileName(), userDataFile, tfd.getSize());
                    }
                }
            }
        }
    }
    
    /**
     * @param fileName
     * @param outFile
     * @param fileSize
     */
    private void doDownloadTaxonFile(final String fileName,
                                     final File outFile,
                                     final int  fileSize)
    {
        try
        {
            String downloadHTTP = UIRegistry.getResourceString(DWNLD_TAX_URL);
            if (StringUtils.isNotEmpty(downloadHTTP))
            {
                final URL url = new URL(downloadHTTP + "/" + fileName);
                
                TaxonDownloadDlg dlg = new TaxonDownloadDlg(url, outFile, fileSize);
                dlg.setModal(true);
                UIHelper.centerAndShow(dlg);
                
                if (dlg.getStatus() == TaxonDownloadDlg.StatusType.eOK)
                {
                    downloadedFileName = fileName;
                } else
                {
                    downloadedFileName = null;
                    try
                    {
                        File file = new File(fileName);
                        if (file.exists())
                        {
                            file.delete();
                        }
                    } catch (Exception ex) {}
                }
            } else
            {
                UIRegistry.showLocalizedError(BAD_TAXON_URL);
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param fileName
     * @param doUserProvidedFile
     * @return
     */
    public static File getFileForTaxon(final String fileName, final boolean doUserProvidedFile)
    {
        if (!doUserProvidedFile)
        {
            // Check for a previously down loaded file.
            File taxonXLSFile = new File(UIRegistry.getAppDataDir() + File.separator + fileName);
            if (taxonXLSFile.exists())
            {
                return taxonXLSFile;
            }
            // This means we need to download one from the specify site.
            return null;
        } 
        
        // This is a user Provided File
        File file = new File(fileName);
        return file.exists() ? file : null;
    }

    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public void updateBtnUI()
    {
        boolean isValid = isUIValid();
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isValid);
        }
        
        boolean checked = preloadChk.isSelected();
        enableUI(otherTF.getText().isEmpty() && isValid && checked, true, checked);
    }
    
    /**
     * @param enabled
     * @param doAll
     * @param otherEnabled
     */
    private void enableUI(final boolean enabled, 
                          final boolean doAll,
                          final boolean otherEnabled)
    {
        fileLbl.setEnabled(enabled);
        fileCBX.setEnabled(enabled);
        srcLbl.setEnabled(enabled);
        srcTF.setEnabled(enabled);
        coverageLbl.setEnabled(enabled);
        coverageTF.setEnabled(enabled);
        descLbl.setEnabled(enabled);
        descTA.setEnabled(enabled);
        stdSep.setEnabled(enabled);
        
        if (doAll)
        {
            othSep.setEnabled(otherEnabled);
            otherLbl.setEnabled(otherEnabled);
            otherBrw.setEnabled(otherEnabled);
        }
    }
    
    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public boolean isUIValid()
    {
        if (preloadChk.isSelected() && otherBrw.isEnabled() && !otherTF.isFocusOwner())
        {
            String filePath = otherTF.getText();
            if (!filePath.isEmpty() && FilenameUtils.isExtension(filePath.toLowerCase(), XLS))
            {
                File f = new File(filePath);
                return f.exists();
            }
            return StringUtils.isEmpty(otherTF.getText());
        }
        return true;
    }
    
    // Getters 
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        list.add(new Pair<String, String>(getResourceString("DSP_TYPE"), getResourceString(preloadChk.isSelected() ? "YES" : "NO")));
        return list;
    }
    
}
