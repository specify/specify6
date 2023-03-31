/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.ui.DBObjSearchDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * NOTE: Thiks is an example of how to do the Search dialog usin express search instead of a regular search.
 * (I think one of the bggest reasons NOT to do it this way is because the Express MUST be update  before it is used.
 * so if the user was entering in a lot of new data they may forget to update the express search as they went and then wouldn't
 * find thinkgs they just entered)
 *
 * I am leaving this checked in for a while
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class AgentSearchDialogES extends JDialog implements ActionListener, ExpressSearchResultsPaneIFace
{
    private static final Logger log  = Logger.getLogger(DBObjSearchDialog.class);

    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JTextField     searchText;

    protected JPanel         contentPanel;
    protected JScrollPane    scrollPane;
    protected JTable         table;

    protected JButton        searchBtn;
    protected Color          textBGColor    = null;
    protected Color          badSearchColor = new Color(255,235,235);

    protected ESResultsTablePanelIFace  etrb;

    protected int            tableId        = -1;
    protected RecordSetIFace      recordSet      = null;

    // Form Stuff
    protected ViewIFace           formView = null;
    protected Viewable   form     = null;
    //protected Agent          agent    = new Agent();

    protected Map<String, String> dataMap              = UIHelper.createMap();
    protected Map<String, String> formFieldToColumnMap = UIHelper.createMap();

    /**
     *
     * @throws HeadlessException
     */
    public AgentSearchDialogES(final Frame parent) throws HeadlessException
    {
        super(parent, getResourceString("AgentSearchTitle"), true);
        tableId    = DBTableIdMgr.getInstance().getIdByShortName("agent");

        String[] mappings = {"lastName", "lastname", "firstName", "firstname"};
        for (int i=0;i<mappings.length;i++)
        {
            formFieldToColumnMap.put(mappings[i], mappings[i+1]);
            i++;
        }
        createUI();
        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
    }

    /**
     * Creates the Default UI for Lable task
     *
     */
    protected void createUI()
    {
        searchText = createTextField(30);
        searchBtn  = createButton(getResourceString("SEARCH"));
        ActionListener doQuery = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                form.getDataFromUI();

                StringBuilder strBuf = new StringBuilder(128);
                for (String key : formFieldToColumnMap.keySet())
                {
                  String value  = dataMap.get(key);
                  if (isNotEmpty(value))
                  {
                      if (strBuf.length() > 0)
                      {
                          strBuf.append(" OR ");
                      }
                      strBuf.append(formFieldToColumnMap.get(key)+":"+value);
                  }

                }
                searchText.setText(strBuf.toString());
                doQuery();
            }
        };

        searchBtn.addActionListener(doQuery);
        searchText.addActionListener(doQuery);
        searchText.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (searchText.getBackground() != textBGColor)
                {
                    searchText.setBackground(textBGColor);
                }
            }
        });

        String viewName = "test";
        String name = null; // use the default

        formView = AppContextMgr.getInstance().getView(name, viewName);
        if (formView != null)
        {
            form = ViewFactory.createFormView(null, formView, null, dataMap, MultiView.NO_OPTIONS, null);
            if (form != null)
            {
                add(form.getUIComponent(), BorderLayout.CENTER);
            } else
            {
                return;
            }
        } else
        {
            log.info("Couldn't load form with name ["+name+"] Id ["+viewName+"]");
        }
        //form.setDataObj(prefNode);
        //form.getValidator().validateForm();


        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,1dlu,p", "p,2dlu,p,2dlu,p"));
        CellConstraints cc         = new CellConstraints();

        //builder.addSeparator(getResourceString("AgentSearchTitle"), cc.xywh(1,1,3,1));
        builder.add(form.getUIComponent(), cc.xy(1,1));
        builder.add(searchBtn, cc.xy(3,1));

        panel.add(builder.getPanel(), BorderLayout.NORTH);
        contentPanel = new JPanel(new NavBoxLayoutManager(0,2));
        //contentPanel.setMaximumSize(new Dimension(300,300));
        //contentPanel.setPreferredSize(new Dimension(300,300));

        scrollPane = new JScrollPane(contentPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        //scrollPane.setMaximumSize(new Dimension(300,300));
        scrollPane.setPreferredSize(new Dimension(300,200));

        // Bottom Button UI
        cancelBtn = createButton(getResourceString("CANCEL"));
        okBtn = createButton(getResourceString("OK"));

        okBtn.addActionListener(this);
        getRootPane().setDefaultButton(okBtn);

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGlue();
        btnBuilder.addGriddedButtons(new JButton[] { cancelBtn, okBtn });

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                setVisible(false);
            }
        });

        panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        updateUI();
    }

    /**
     *  Updates the OK button.
     */
    protected void updateUI()
    {
        okBtn.setEnabled(recordSet != null && recordSet.getNumItems() == 1);
    }

    /**
     * Performs the express search and returns the results
     */
    public void doQuery()
    {
        String searchTerm = searchText.getText();
        if (isNotEmpty(searchTerm))
        {
            contentPanel.removeAll();
            //ExpressSearchTask.doQuery(searchText, null, badSearchColor, this);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addSearchResults(edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace)
     */
    public void addSearchResults(final QueryForIdResultsIFace results)
    {
        /* XYZ
        //System.out.println(tableInfo.getTitle()+"  "+tableInfo.getTableId() + "  " + tableId);
        if (Integer.parseInt(results.getTableInfo().getTableId()) == tableId)
        {
            recordSet = null;
            updateUI();

            if (results.getTableInfo().isUseHitsCache())
            {
                contentPanel.add(etrb = new ExpressTableResultsHitsCache(this, results, false, hits));
            } else
            {
                contentPanel.add(etrb = new ExpressTableResults(this, results, false));
            }
            
            table = etrb.getTable();
            table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                    {public void valueChanged(ListSelectionEvent e)
                    {
                        if (etrb != null && !e.getValueIsAdjusting())
                        {
                            recordSet = etrb.getRecordSet(false);
                        } else
                        {
                            recordSet = null;
                        }
                        updateUI();
                    }});
            repaint();
        }
        */
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#removeTable(edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase)
     */
    public void removeTable(ESResultsTablePanelIFace expTblRes)
    {
        expTblRes.cleanUp();
        
        contentPanel.remove(expTblRes.getUIComponent());
        contentPanel.invalidate();
        contentPanel.doLayout();
        contentPanel.repaint();

        scrollPane.revalidate();
        scrollPane.doLayout();
        scrollPane.repaint();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addTable(edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase)
     */
    public void addTable(ESResultsTablePanelIFace expTblRes)
    {
        // it has already been added so don't do anything
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#revalidateScroll()
     */
    public void revalidateScroll()
    {
        contentPanel.invalidate();
        scrollPane.revalidate();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        // Handle clicks on the OK and Cancel buttons.
       setVisible(false);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#hasResults()
     */
    public boolean hasResults()
    {
        return etrb.hasResults();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#doQueriesSynchronously()
     */
    public boolean doQueriesSynchronously()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#done()
     */
    public void done()
    {
        // not needed
    }

}
