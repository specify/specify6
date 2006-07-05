package edu.ku.brc.specify.appui;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;
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
import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.search.Hits;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.NavBoxLayoutManager;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.specify.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.tasks.ExpressResultsTableInfo;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressTableResults;
import edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase;
import edu.ku.brc.specify.tasks.subpane.ExpressTableResultsHitsCache;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.GenericSearchDialog;
import edu.ku.brc.specify.ui.forms.ViewFactory;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.Viewable;
import edu.ku.brc.specify.ui.forms.persist.View;

/**
 * NOTE: Thiks is an example of how to do the Search dialog usin express search instead of a regular search.
 * (I think one of the bggest reasons NOT to do it this way is because the Express MUST be update  before it is used.
 * so if the user was entering in a lot of new data they may forget to update the express search as they went and then wouldn't
 * find thinkgs they just entered)
 *
 * I am leaving this checked in for a while
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class AgentSearchDialogES extends JDialog implements ActionListener, ExpressSearchResultsPaneIFace
{
    private static final Logger log  = Logger.getLogger(GenericSearchDialog.class);

    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JTextField     searchText;

    protected JPanel         contentPanel;
    protected JScrollPane    scrollPane;
    protected JTable         table;

    protected Analyzer       analyzer       = new SimpleAnalyzer();//WhitespaceAnalyzer();
    protected File           lucenePath     = null;
    protected JButton        searchBtn;
    protected Color          textBGColor    = null;
    protected Color          badSearchColor = new Color(255,235,235);

    protected Hashtable<String, ExpressResultsTableInfo> tables = null;
    ExpressTableResultsBase  etrb;

    protected int            tableId        = -1;
    protected RecordSet      recordSet      = null;

    // Form Stuff
    protected View           formView = null;
    protected Viewable   form     = null;
    //protected Agent          agent    = new Agent();

    protected Map<String, String> dataMap              = UIHelper.createMap();
    protected Map<String, String> formFieldToColumnMap = UIHelper.createMap();

    /**
     *
     * @throws HeadlessException
     */
    public AgentSearchDialogES() throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), getResourceString("AgentSearchTitle"), true);
        tables     = ExpressSearchTask.intializeTableInfo();
        lucenePath = ExpressSearchTask.getIndexDirPath();
        tableId    = DBTableIdMgr.lookupIdByShortName("agent");

        String[] mappings = {"lastName", "lastname", "firstName", "firstname"};
        for (int i=0;i<mappings.length;i++)
        {
            formFieldToColumnMap.put(mappings[i], mappings[i+1]);
            i++;
        }
        createUI();
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setAlwaysOnTop(true);


    }

    /**
     * Creates the Default UI for Lable task
     *
     */
    protected void createUI()
    {
        searchText = new JTextField(30);
        searchBtn  = new JButton(getResourceString("Search"));
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

        formView = ViewMgr.getView(name, viewName);
        if (formView != null)
        {
            form = ViewFactory.createFormView(null, formView, null, dataMap);
            add(form.getUIComponent(), BorderLayout.CENTER);
            //getter = new DataGetterForObj();

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
        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn = new JButton(getResourceString("OK"));

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

    protected void updateUI()
    {
        okBtn.setEnabled(recordSet != null && recordSet.getItems().size() == 1);
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
            ExpressSearchTask.doQuery(lucenePath, analyzer, searchText, badSearchColor, tables, this);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addSearchResults(edu.ku.brc.specify.tasks.ExpressResultsTableInfo, org.apache.lucene.search.Hits)
     */
    public void addSearchResults(final ExpressResultsTableInfo tableInfo, final Hits hits)
    {
        //System.out.println(tableInfo.getTitle()+"  "+tableInfo.getTableId() + "  " + tableId);
        if (Integer.parseInt(tableInfo.getTableId()) == tableId)
        {
            recordSet = null;
            updateUI();

            if (tableInfo.isUseHitsCache())
            {
                contentPanel.add(etrb = new ExpressTableResultsHitsCache(this, tableInfo, false, hits));
            } else
            {
                contentPanel.add(etrb = new ExpressTableResults(this, tableInfo, false));
            }
            table = etrb.getTable();
            table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                    {public void valueChanged(ListSelectionEvent e)
                    {
                        if (etrb != null && !e.getValueIsAdjusting())
                        {
                            recordSet = etrb.getRecordSet(false);
                            for (Object obj : recordSet.getItems())
                            {
                                RecordSetItem rsi = (RecordSetItem)obj;
                                //System.out.println(rsi.getRecordId());
                            }

                        } else
                        {
                            recordSet = null;
                        }
                        updateUI();
                    }});
            repaint();
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#removeTable(edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase)
     */
    public void removeTable(ExpressTableResultsBase table)
    {
        contentPanel.remove(table);
        contentPanel.invalidate();
        contentPanel.doLayout();
        contentPanel.repaint();

        scrollPane.revalidate();
        scrollPane.doLayout();
        scrollPane.repaint();
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

}
