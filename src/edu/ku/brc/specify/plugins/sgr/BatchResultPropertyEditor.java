/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
/**
 * 
 */
package edu.ku.brc.specify.plugins.sgr;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import java.awt.Frame;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.sgr.datamodel.MatchConfiguration;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author ben
 * 
 * @code_status Alpha
 * 
 *              Created Date: Jun 2, 2011
 * 
 */
public class BatchResultPropertyEditor extends CustomDialog
{
    private UIPanel             uiPanel;
    private BatchMatchResultSet resultSet;
    private NavBoxItemIFace     nbi;

    public BatchResultPropertyEditor(NavBoxItemIFace nbi)
    {
        super((Frame) UIRegistry.getTopWindow(), "", true, CustomDialog.OKCANCELHELP, new UIPanel());

        this.nbi = nbi;
        this.helpContext = "sgr_matchresults_properties";

        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        setTitle(getResourceString("SGR_EDIT_BATCH_RESULTS"));
        resultSet = (BatchMatchResultSet) nbi.getData();
        MatchConfiguration matcher = resultSet.getMatchConfiguration();

        uiPanel = (UIPanel) getContentPanel();
        uiPanel.name.setText(resultSet.name());
        uiPanel.matcher.setText(matcher.name());
        uiPanel.remarks.setText(resultSet.remarks());
        pack();
        UIRegistry.popResourceBundle();
    }

    @Override
    protected void okButtonPressed()
    {
        String name = uiPanel.name.getText();
        if (StringUtils.isBlank(name))
        {
            UIRegistry.loadAndPushResourceBundle("specify_plugins");
            UIRegistry.showLocalizedError("SGR_BLANK_RESULTSET_NAME_ERROR");
            UIRegistry.popResourceBundle();
            return;
        }
        resultSet.updateProperties(name, uiPanel.remarks.getText());
        ((NavBoxButton) nbi).setLabelText(name);
        ((NavBoxButton) nbi).getParent().repaint();
        dispose();
    }

    private static class UIPanel extends JPanel
    {
        JTextField name    = new JTextField();
        JTextField matcher = new JTextField();
        JTextArea  remarks = new JTextArea(10, 20);

        public UIPanel()
        {
            super();
            remarks.setLineWrap(true);
            remarks.setWrapStyleWord(true);

            FormLayout layout = new FormLayout("right:max(50dlu;p), 4dlu, 150dlu:grow",
                    "p, 2dlu, p, 2dlu, p");

            PanelBuilder builder = new PanelBuilder(layout, this);
            builder.setDefaultDialogBorder();
            CellConstraints cc = new CellConstraints();

            UIRegistry.loadAndPushResourceBundle("specify_plugins");

            builder.addLabel(getResourceString("SGR_BATCH_RESULTS_NAME"), cc.xy(1, 1));
            builder.add(name, cc.xy(3, 1));

            builder.addLabel(getResourceString("SGR_MATCHER"), cc.xy(1, 3));
            builder.add(matcher, cc.xy(3, 3));
            matcher.setEditable(false);

            builder.addLabel(getResourceString("SGR_REMARKS"), cc.xy(1, 5));
            builder.add(remarks, cc.xy(3, 5));
            UIRegistry.popResourceBundle();
        }
    }
}
