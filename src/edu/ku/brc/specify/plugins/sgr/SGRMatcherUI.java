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
package edu.ku.brc.specify.plugins.sgr;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.sgr.SGRMatcher;
import edu.ku.brc.sgr.datamodel.DataModel;
import edu.ku.brc.sgr.datamodel.MatchConfiguration;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.standard.StandardQueryParser;
import org.apache.lucene.search.Query;
/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: May 26, 2011
 *
 */
public class SGRMatcherUI extends CustomDialog
{

    public static enum WeightChoice
    {
        Ignore,
        Low,
        Normal,
        High,
    }
    
    public static final String [] availableFields = 
        {"collectors", "collector_number", "location", 
        "date_collected", "date_split", "scientific_name"};   
    
    private final SGRMatcherUIPanel                  uiPanel;
    private final Function<MatchConfiguration, Void> finished;
    private NavBoxItemIFace                          nbi             = null;
    
    public static SGRMatcherUI dialogForNewConfig(Frame parent, 
                                                  Function<MatchConfiguration, Void> onFinished)
    {
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        String title = UIRegistry.getResourceString("SGR_CREATE_MATCHER");
        UIRegistry.popResourceBundle();
        
        SGRMatcherUI dialog = new SGRMatcherUI(parent, title, new SGRMatcherUIPanel(null), onFinished);
        return dialog;
    }
    
    public static SGRMatcherUI dialogForEditing(Frame parent, NavBoxItemIFace nbi)
    {
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        String title = UIRegistry.getResourceString("SGR_EDIT_MATCHER");
        UIRegistry.popResourceBundle();

        SGRMatcherUI dialog = new SGRMatcherUI(parent, title, new SGRMatcherUIPanel(nbi), null);
        dialog.nbi = nbi;
        return dialog;
    }
    
    private SGRMatcherUI(Frame frame, String title, SGRMatcherUIPanel uiPanel,
                         Function<MatchConfiguration, Void> finished) 
        throws HeadlessException
    {
        super(frame, title, true, CustomDialog.OKCANCELHELP, uiPanel); 
        
        this.finished = finished;
        this.uiPanel = uiPanel;
        this.helpContext = "sgr_matcher_create";
        pack();
    }
    
    @Override
    protected void okButtonPressed() 
    {
        if (StringUtils.isBlank(uiPanel.name.getText()))
        {
            UIRegistry.loadAndPushResourceBundle("specify_plugins");
            UIRegistry.showLocalizedError("SGR_MATCHER_NAME_REQUIRED_ERROR");
            UIRegistry.popResourceBundle();
            return;
        }

        if (nbi == null)
        {
            SGRMatcher.Factory mf = SGRMatcher.getFactory();
            
            String name = uiPanel.name.getText();
            
            String index = (String) ((JComboBox) uiPanel.index).getSelectedItem();
            mf.serverUrl = uiPanel.indices.get(index);
            mf.nRows = ((Number) uiPanel.nRows.getValue()).intValue();
            mf.boostInterestingTerms = uiPanel.boost.isSelected();
            
            List<String> similarityFields = new LinkedList<String>();
            List<String> boosts = new LinkedList<String>();
            for (String field : availableFields)
            {
                // The date_split field is controlled by the same UI elements as date_collected
                String ctrlField = field.equals("date_split") ? "date_collected" : field;
                
                switch ((WeightChoice)uiPanel.similarityFields.get(ctrlField).getSelectedItem())
                {
                    case High:
                        boosts.add(field + "^5.0");
                        similarityFields.add(field);
                        break;
                    case Low:
                        boosts.add(field + "^0.2");
                        similarityFields.add(field);
                        break;
                    case Normal:
                        similarityFields.add(field);
                        break;
                }
            }
            mf.similarityFields = StringUtils.join(similarityFields.iterator(), ',');
            mf.queryFields = StringUtils.join(boosts.iterator(), ' ');
            
            List<String> filters = new LinkedList<String>();
            
            String institutionCode = uiPanel.institutionCode.getText(); 
            if (!StringUtils.isBlank(institutionCode))
                 filters.add("-institution_code:\"" + institutionCode + "\"");
            
            String collectionCode = uiPanel.collectionCode.getText(); 
            if (!StringUtils.isBlank(collectionCode))
                 filters.add("-collection_code:\"" + collectionCode + "\"");
            
            mf.filterQuery = StringUtils.join(filters.toArray(), ' ');
            
            finished.apply(
                    DataModel.persistMatchConfiguration(name, uiPanel.remarks.getText(), mf)
            );
        } else {
            MatchConfiguration matchConfig = (MatchConfiguration)nbi.getData();
            String name = uiPanel.name.getText();
            String remarks = uiPanel.remarks.getText();
            matchConfig.updateProperties(name, remarks);
            ((NavBoxButton) nbi).setLabelText(name);
            ((NavBoxButton) nbi).getParent().repaint();
        }
        dispose();
    }
    
    private static class SGRMatcherUIPanel extends JPanel
    {
        JTextField             name             = new JTextField("New matcher");

        JComponent             index;

        JFormattedTextField    nRows            = new JFormattedTextField(10);

        JCheckBox              boost            = new JCheckBox("Boost Interesting Terms", true);

        Map<String, JComboBox> similarityFields = new HashMap<String, JComboBox>(
                                                        availableFields.length);

        JTextField             institutionCode      = new JTextField();
        JTextField             collectionCode       = new JTextField();

        JTextArea              remarks          = new JTextArea(10, 20);
        
        Map<String, String> indices;
        
        public SGRMatcherUIPanel(NavBoxItemIFace nbi)
        {
            super();
            remarks.setLineWrap(true);
            
            for (String field : availableFields)
            {
                // The date_split field doesn't get its own controls.
                if (field.equals("date_split")) continue;
                
                JComboBox comboBox = new JComboBox(WeightChoice.values());
                comboBox.setSelectedItem(WeightChoice.Normal);
                similarityFields.put(field, comboBox);
            }
            
            if (nbi != null)
            {
                MatchConfiguration matchConfig = (MatchConfiguration)nbi.getData();
                name.setText(matchConfig.name());
                
                JTextField serverUrl;
                serverUrl = new JTextField();
                serverUrl.setText(matchConfig.serverUrl());
                serverUrl.setEditable(false);
                index = serverUrl;
                
                nRows.setText("" + matchConfig.nRows());
                
                boost.setSelected(matchConfig.boostInterestingTerms());
                boost.setEnabled(false);
                
                Set<String> selectedFields = 
                    parseSimilarityFields(matchConfig.similarityFields());
                
                Map<String, Float> boosts = parseBoosts(matchConfig.queryFields());
                
                for (String field : availableFields)
                {
                    // The date_split field doesn't get its own controls.
                    if (field.equals("date_split")) continue;
                    
                    if (!selectedFields.contains(field))
                    {
                        similarityFields.get(field).setSelectedItem(WeightChoice.Ignore);
                    }
                    else if (boosts.containsKey(field))
                    {
                        if (boosts.get(field) > 1.0f)
                            similarityFields.get(field).setSelectedItem(WeightChoice.High);
                        else if (boosts.get(field) < 1.0f)
                            similarityFields.get(field).setSelectedItem(WeightChoice.Low);
                        else
                            similarityFields.get(field).setSelectedItem(WeightChoice.Normal);
                    }
                    else
                        similarityFields.get(field).setSelectedItem(WeightChoice.Normal);
                }
                
                for (JComboBox cb : similarityFields.values())
                {
                    cb.setEnabled(false);
                }
                
                institutionCode.setText(
                        extractFieldFromQuery(matchConfig.filterQuery(), "institution_code"));
                institutionCode.setEditable(false);
                
                collectionCode.setText(
                        extractFieldFromQuery(matchConfig.filterQuery(), "collection_code"));
                collectionCode.setEditable(false);

                remarks.setText(matchConfig.remarks());
            }
            else
            {
                institutionCode.setText(
                        AppContextMgr.getInstance().getClassObject(Institution.class).getCode());
                
                collectionCode.setText(
                        AppContextMgr.getInstance().getClassObject(Collection.class).getCode());
                
                try
                {
                    indices = AvailableIndicesFetcher.getIndices();
                }
                catch (Exception e)
                {
                    throw new AvailableIndicesFetchException(e);
                }
                JComboBox availIndices = new JComboBox(indices.keySet().toArray());
                index = availIndices;
                
                Discipline disc = AppContextMgr.getInstance().getClassObject(Discipline.class);
                String discTitle = disc.getName();
                for (int i= 0; i < availIndices.getItemCount(); i++)
                {
                    if (availIndices.getItemAt(i).equals(discTitle))
                        availIndices.setSelectedIndex(i);
                }
            }
            
            int rows = similarityFields.size() + 7;
            StringBuilder colSpec = new StringBuilder();
            for (int i = 0; i < rows; i++) colSpec.append("p, 2dlu,");
            colSpec.append("p");
            
            FormLayout layout = 
                new FormLayout("right:max(50dlu;p), 4dlu, 150dlu:grow", colSpec.toString());
            
            PanelBuilder builder = new PanelBuilder(layout, this);
            builder.setDefaultDialogBorder();
            CellConstraints cc = new CellConstraints();
            
            UIRegistry.loadAndPushResourceBundle("specify_plugins");
            
            int y = 1;
            builder.add(UIHelper.createI18NFormLabel("SGR_MATCHER_NAME"), cc.xy(1, y));
            builder.add(name,                                             cc.xy(3, y));
            y += 2;
            
            builder.add(UIHelper.createI18NFormLabel("SGR_INDEX"), cc.xy(1, y));
            builder.add(index,                                     cc.xy(3, y));
            y += 2;
            
            builder.addSeparator(UIRegistry.getResourceString("SGR_EXCLUDE"), cc.xyw(1, y, 3));
            y += 2;
            
            builder.add(UIHelper.createI18NFormLabel("SGR_INST_CODE"), cc.xy(1, y));
            builder.add(institutionCode,                               cc.xy(3, y));
            y += 2;
            
            builder.add(UIHelper.createI18NFormLabel("SGR_COLL_CODE"), cc.xy(1, y));
            builder.add(collectionCode,                                cc.xy(3, y));
            y += 2;
            
            builder.addSeparator(UIRegistry.getResourceString("SGR_SIMILARITY"), cc.xyw(1, y, 3));
            y += 2;
            
            SGRColumnOrdering columnOrdering = SGRColumnOrdering.getInstance();
            
            for (String field : availableFields)
            {
                if (!similarityFields.containsKey(field)) continue;
                
                String label = columnOrdering.getHeadingFor(field);
                label = label == null ? WordUtils.capitalize(field) : label;
                
                builder.add(UIHelper.createFormLabel(label), cc.xy(1, y));
                builder.add(similarityFields.get(field),     cc.xy(3, y));
                y += 2;
            }
            
//            builder.addLabel("Number of Results", cc.xy(1, y));
//            builder.add(nRows,                    cc.xy(3, y));
//            y += 2;
            
//            builder.add(boost,              cc.xy(3, y));
//            y += 2;
            
//            builder.addLabel("Remarks",     cc.xy(1, y));
            
            builder.addSeparator(UIRegistry.getResourceString("SGR_REMARKS"), cc.xyw(1, y, 3));
            y += 2;
            
            JScrollPane scrollPane = new JScrollPane(remarks, 
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            builder.add(scrollPane,            cc.xyw(1, y, 3));
            y += 2;
            
            UIRegistry.popResourceBundle();
        }
        
        private String extractFieldFromQuery(String query, String field)
        {
            if (StringUtils.isBlank(query))
                return "";
            
            StandardQueryParser qp = new StandardQueryParser();
            Query q;
            try
            {
                q = qp.parse(query, "defaultField");
            } catch (QueryNodeException e) { throw new RuntimeException(e); }

            Set<Term> terms = new HashSet<Term>();
            q.extractTerms(terms);
            for (Term t : terms)
            if (t.field().equals(field))
            {
                return t.text();
            }
            return "";
        }

        private Set<String> parseSimilarityFields(String fields)
        {
            ImmutableSet.Builder<String> b = ImmutableSet.builder();
            
            for (String field : StringUtils.split(fields, ','))
                   b.add(field.trim());
            
            return b.build();
        }
        
        private Map<String, Float> parseBoosts(String queryFields)
        {
            ImmutableMap.Builder<String, Float> b = ImmutableMap.builder();
            
            for (String field : availableFields)
            {
                Pattern p = Pattern.compile(field + "\\^" + "([-+]?[0-9]*\\.?[0-9]+)");
                Matcher m = p.matcher(queryFields);
                if (m.find())
                {
                    b.put(field, Float.valueOf(m.group(1)));
                }
            }
            return b.build();
        }
    }

    public static class AvailableIndicesFetchException extends RuntimeException 
    {

        public AvailableIndicesFetchException()
        {
            super();
            // TODO Auto-generated constructor stub
        }

        public AvailableIndicesFetchException(String arg0, Throwable arg1)
        {
            super(arg0, arg1);
            // TODO Auto-generated constructor stub
        }

        public AvailableIndicesFetchException(String arg0)
        {
            super(arg0);
            // TODO Auto-generated constructor stub
        }

        public AvailableIndicesFetchException(Throwable arg0)
        {
            super(arg0);
            // TODO Auto-generated constructor stub
        }
       
    }
}
