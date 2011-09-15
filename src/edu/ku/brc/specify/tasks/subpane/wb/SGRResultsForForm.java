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
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.sgr.Match;
import edu.ku.brc.sgr.MatchResults;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.plugins.sgr.SGRColors;
import edu.ku.brc.specify.plugins.sgr.SGRColumnOrdering;
import edu.ku.brc.specify.plugins.sgr.SGRPluginImpl;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Jun 10, 2011
 *
 */
@SuppressWarnings("serial")
public class SGRResultsForForm extends JPanel
{
    private static final Logger log = Logger.getLogger(SGRResultsForForm.class);
    private Workbench           workbench;
    private WorkbenchPaneSS     workbenchPaneSS;
    private final SGRPluginImpl sgrPlugin;

    private SGRColumnOrdering   columnOrdering = SGRColumnOrdering.getInstance();
    public final JScrollPane    scrollPane;
    private int                 currentIndex   = -1;

    public SGRResultsForForm(WorkbenchPaneSS workbenchPaneSS, Workbench workbench)
    {
        this.workbench = workbench;
        this.workbenchPaneSS = workbenchPaneSS;
        
        sgrPlugin = (SGRPluginImpl) workbenchPaneSS.getPlugin(SGRPluginImpl.class);
        
        scrollPane = new JScrollPane(this);
        setLayout(new BorderLayout());
    }

    public void setWorkbench(Workbench workbench)
    {
        this.workbench = workbench;        
    }

    public void newRecordAdded()
    {
        // TODO Auto-generated method stub
        
    }

    public void indexChanged(final int newIndex)
    {
        if (newIndex == currentIndex) return;
        currentIndex = newIndex;
        refresh();
    }
    
    private void showMessage(String key)
    {
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        String msg = UIRegistry.getResourceString(key);
        UIRegistry.popResourceBundle();
        
        setLayout(new BorderLayout());
        JLabel label = createLabel(msg, SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
        getParent().validate();
    }
    
    public void refresh()
    {
        removeAll();
        repaint();
        
        if (!sgrPlugin.isReady())
        {
            showMessage("SGR_NO_MATCHER");
            return;
        }
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        new SwingWorker<MatchResults, Void>()
        {
            private int index = currentIndex;

            @Override
            protected MatchResults doInBackground() throws Exception
            {
                int modelIndex = workbenchPaneSS.getSpreadSheet().convertRowIndexToModel(index);
                
                return sgrPlugin.doQuery(workbench.getRow(modelIndex));
            }
            
            @Override
            protected void done() 
            {
                // if we changed indexes in the meantime, don't show this result.
                if (index != currentIndex) return;
                //removeAll();
                
                MatchResults results;
                try { results = get(); } 
                catch (CancellationException e) { return; }
                catch (InterruptedException e) { return; } 
                catch (ExecutionException e)
                {
                    sgrFailed(e);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
                
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (results.matches.size() < 1)
                {
                    showMessage("SGR_NO_RESULTS");
                    return;                   
                }
                
                float maxScore = sgrPlugin.getColorizer().getMaxScore();
                if (maxScore == 0.0f) maxScore = 22.0f;
                
                StringBuilder columns = new StringBuilder("right:max(50dlu;p)");
                for (Match result: results)
                {
                    columns.append(", 4dlu, 150dlu:grow");
                }
                
                String [] fields = columnOrdering.getFields();
                StringBuilder rows = new StringBuilder();
                for (int i = 0; i < fields.length-1; i++)
                {
                    rows.append("p, 4dlu,");
                }
                rows.append("p");
                
                FormLayout layout = new FormLayout(columns.toString(), rows.toString());
                PanelBuilder builder = new PanelBuilder(layout, SGRResultsForForm.this);
                CellConstraints cc = new CellConstraints();
                
                int y = 1;
                for (String heading: columnOrdering.getHeadings())
                {
                    builder.addLabel(heading + ":", cc.xy(1, y));
                    y += 2;
                }
                
                int x = 3;
                for (Match result: results)
                {
                    y = 1;
                    for(String field: fields)
                    {
                        String value;
                        Color color;
                        if (field.equals("id"))
                        {
                            value = result.match.id;
                            color = SGRColors.colorForScore(result.score, maxScore);
                        }
                        else if (field.equals("score"))
                        {
                            value = "" + Math.round(100.0*result.score/maxScore);
                            color = SGRColors.colorForScore(result.score, maxScore);
                        }
                        else
                        {
                            value = StringUtils.join(result.match.getFieldValues(field).toArray(), "; ");
                            Float fieldContribution = result.fieldScoreContributions().get(field);
                            color = SGRColors.colorForScore(result.score, maxScore, fieldContribution);
                        }
                        
                        JTextField textField = new JTextField(value);
                        textField.setBackground(color);
                        //textField.setEditable(false);
                        textField.setCaretPosition(0);
                        builder.add(textField, cc.xy(x, y));
                        y += 2;
                    }
                    x += 2;
                }
                getParent().validate();                
            }
        }.execute();
        UsageTracker.incrUsageCount("SGR.MatchRow");
    }

    protected void sgrFailed(ExecutionException e)
    {
        log.error("Failed to get SGR results.", e);
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        UIRegistry.displayErrorDlg("SGR_ERROR_SERVER_FAIL");
        UIRegistry.popResourceBundle();
    }

    public void cleanup()
    {
        removeAll();
        scrollPane.removeAll();
        workbench = null;
        workbenchPaneSS = null;
    }
}
