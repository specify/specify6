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

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.help.HelpMgr;
import edu.ku.brc.specify.tasks.subpane.DbAdminPane;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
//import edu.ku.brc.ui.SearchReplacePanel.FindReplaceTextFieldKeyAdapter;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Mar 26, 2007
 *
 */
public class DataImportDialog extends JDialog // implements ChangeListener 
{
    // ConfigureCSV conf = new ConfigureCSV();

    JButton                     cancelBtn;
    JButton                     backBtn;
    JButton                     nextBtn;
    JButton                     finishBtn;
    JButton                     helpBtn;
    private JCheckBox           tab;
    private JCheckBox           space;
    private JCheckBox           semicolon;
    private JCheckBox           comma;
    private JCheckBox           other;
    private JTextField          otherText;
    private char                delimiterChar;
    private JSpinner spinner;
    
    private int importFromRowNum = -1;
    private static final Logger log = Logger.getLogger(DataImportDialog.class);

    /**
     * 
     */
    public DataImportDialog(final Frame frame, final String title)
    {
        setContentPane(createConfigPanel());
        setTitle(getResourceString("logintitle"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    public JPanel createConfigPanel()
    {
        JPanel configPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,3dlu", // columns
                "p,3dlu, p,3dlu, p,3dlu"), configPanel);// rows
        JLabel directions = new JLabel(getResourceString("DELIM_EXPLAIN"));
                builder.add(directions, cc.xy(1, 1));
        
        builder.add(createDelimiterPanel(), cc.xy(1, 3));
        builder.add(buildButtons(),          cc.xy(1,5)); 
        return configPanel;
    }

    public JPanel buildButtons()
    {

        cancelBtn = new JButton(getResourceString("Cancel"));
        backBtn = new JButton(getResourceString("Back"));
        nextBtn = new JButton(getResourceString("Next"));
        finishBtn = new JButton(getResourceString("Finish"));
        helpBtn = new JButton(getResourceString("Help"));

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("cacnel button clicked");
            }
        });

        backBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("back button clicked");
            }
        });

        nextBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("next button clicked");
            }
        });

        finishBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("finish button clicked");
            }
        });

        getRootPane().setDefaultButton(nextBtn);
        HelpMgr.registerComponent(helpBtn, "configcsv");
        return ButtonBarFactory.buildRightAlignedBar(helpBtn, cancelBtn, backBtn, nextBtn, finishBtn);
    }

    public JPanel createDelimiterPanel()
    {
        JPanel p = new FormDebugPanel();

        CellConstraints cc = new CellConstraints();
        FormLayout formLayout = new FormLayout("p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu", "p,3dlu, p,3dlu,p,3dlu, p,3dlu, p,3dlu ");
        PanelBuilder builder = new PanelBuilder(formLayout, p);

        builder.addSeparator(getResourceString("SELECT_DELIMS"), cc.xywh(1, 1, 6, 1));

        tab = new JCheckBox(getResourceString("TAB"));
        tab.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                updateTableDispaly();
            }
        });
        builder.add(tab, cc.xy(1, 3));

        space = new JCheckBox(getResourceString("SPACE"));
        space.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                updateTableDispaly();
            }
        });
        builder.add(space, cc.xy(3, 3));
        
        JPanel otherPanel = new JPanel();
        otherPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        other = new JCheckBox(getResourceString("OTHER"));
        other.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                updateTableDispaly();
            }
        });
        otherPanel.add(other);
        otherText = new JTextField();
        otherText.addKeyListener(new CharFieldKeyAdapter());
        otherText.setColumns(1);
        otherPanel.add(otherText);
        builder.add(otherPanel, cc.xy(5,3));
        

        comma = new JCheckBox(getResourceString("COMMA"));
        comma.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                updateTableDispaly();
            }
        });
        builder.add(comma, cc.xy(1, 5));
        semicolon = new JCheckBox(getResourceString("SEMICOLON"));
        semicolon.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                updateTableDispaly();
            }
        });
        ButtonGroup group = new ButtonGroup();
        group.add(tab);
        group.add(space);
        group.add(other);
        group.add(comma);
        group.add(semicolon);
        builder.add(semicolon, cc.xy(3, 5));

        //builder.addSeparator(getResourceString("START IMPORT AT ROW:"), cc.xywh(1, 7, 6, 1));
        //SpinnerModel rowNumber = new SpinnerNumberModel(1, //initial value
        //        1,      //min
        //        null,   //max
        //        1);     //step
        //spinner = new JSpinner(rowNumber);
        
        //spinner.addChangeListener(this);
        //builder.add(spinner, cc.xy(1, 9));
        return p;
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
//    public void stateChanged(ChangeEvent e) {
//        SpinnerModel dateModel = spinner.getModel();
//        if (dateModel instanceof SpinnerNumberModel) {
//            Number row = (((SpinnerNumberModel)dateModel).getNumber());
//            importFromRowNum = row.intValue();//value selected by user, indicates that import should begin from row x
//        }
//    }
    
    public void updateTableDispaly()
    {
        
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DataImportDialog dlg = new DataImportDialog((Frame) UICacheManager
                .get(UICacheManager.FRAME), "Column Mapper");

        UIHelper.centerAndShow(dlg);
        //DataImportDialog dlg = new DataImportDialog();

    }
    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     * Created Date: Mar 15, 2007
     *
     */
    private class CharFieldKeyAdapter extends KeyAdapter
    {
        /**
         * 
         */
        public CharFieldKeyAdapter()
        {
            super();
        }

        
        /* (non-Javadoc)
         * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
         */
        public void keyReleased(KeyEvent ke)
        {
            log.debug("character typed");
            // make sure the user has entered a text string in teh find box before enabling find buttons
            boolean charentered = (otherText.getText().length() == 1);
            delimiterChar = otherText.getText().toCharArray()[0];
            log.debug("char entered: " + delimiterChar);
            //if(replaceField!=null) replaceTextState = (replaceField.getText().length() > 0);
            //nextButton.setEnabled(findTextState);
            //memoryReplaceButton.setEnabled(findTextState && replaceTextState);
            //make sure the user has entered a text string in teh replace textfield before enabling replace buttons
            //if(replaceButton!=null)replaceButton.setEnabled(findTextState && replaceTextState);
            //if(replaceAllButton!=null)replaceAllButton.setEnabled(findTextState && replaceTextState);
        }
    }
}
