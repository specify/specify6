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
package edu.ku.brc.specify.extras;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.specify.utilapps.DataModelClassGenerator;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 16, 2010
 *
 */
public class FixSQLString extends CustomDialog
{
    private JTextArea srcTA;
    private JTextArea dstTA;
    
    /**
     * @throws HeadlessException
     */
    public FixSQLString() throws HeadlessException
    {
        super((JDialog)null, "SQL Fix", false, OK_BTN, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,10px,f:p:g")); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc = new CellConstraints();
        
        srcTA = new JTextArea(10, 80);
        dstTA = new JTextArea(10, 80);
        pb.add(UIHelper.createScrollPane(srcTA, true), cc.xy(1, 1));
        pb.add(UIHelper.createScrollPane(dstTA, true), cc.xy(1, 3));
        
        srcTA.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                String str = srcTA.getText();
                if (str.length() > 0)
                {
                    if (StringUtils.contains(str, "\""))
                    {
                        fixFromTextToSQL();
                    } else
                    {
                        fix();
                    }
                } else
                {
                    dstTA.setText("");
                }
            }
        });

        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        okBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

        pack();
    }
    
    /**
     * 
     */
    private void fixFromTextToSQL()
    {
        String srcStr = srcTA.getText();
        srcStr = StringUtils.replace(srcStr, "\"", "");
        srcStr = StringUtils.replace(srcStr, "+", "");
        srcStr = StringUtils.replace(srcStr, ";", "");
        
        while (srcStr.contains("  INNER"))
        {
            srcStr = StringUtils.replace(srcStr, "  INNER", " INNER");
        }
        
        while (srcStr.contains("  LEFT"))
        {
            srcStr = StringUtils.replace(srcStr, "  LEFT", " LEFT");
        }
        
        while (srcStr.contains("  WHERE"))
        {
            srcStr = StringUtils.replace(srcStr, "  WHERE", " WHERE");
        }
        
        while (srcStr.contains("\n"))
        {
            srcStr = StringUtils.replace(srcStr, "  \n", "");
        }
        
        dstTA.setText(srcStr);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                dstTA.requestFocus();
                dstTA.selectAll();
                UIHelper.setTextToClipboard(dstTA.getText());
            }
        });
        
    }

    /**
     * 
     */
    private void fix()
    {
        StringBuilder sb       = new StringBuilder("sql = \"");
        String        srcStr   = srcTA.getText();
        boolean       wasInner = false;
        for (String line : StringUtils.splitByWholeSeparator(srcStr, "\n"))
        {
            String str = line;//StringUtils.deleteWhitespace(line);
            
//            while (str.startsWith("  INNER"))
//            {
//                str = 
//            }
            
            if (str.toUpperCase().startsWith("INNER") || 
                    str.toUpperCase().startsWith("ORDER") || 
                    str.toUpperCase().startsWith("GROUP"))
            {
                if (!wasInner)
                {
                    sb.append(" \" +");
                    wasInner = false;
                }
                sb.append("\n    \""+line.trim() + " \" +");
                wasInner = true;
            } else
            {
                if (wasInner)
                {
                    sb.append("    \"");
                    wasInner = false;
                }
                sb.append(' ');
                sb.append(StringUtils.replace(line.trim(), "\n", " "));
                //sb.append(StringUtils.replace(line.trim(), "\r\n", " "));
                //sb.append(StringUtils.replace(line.trim(), "\r", " "));
            }
        }
        
        if (wasInner)
        {
            sb.setLength(sb.length()-3);
            sb.append("\";");
        } else
        {
            sb.append("\";");
        }
        dstTA.setText(sb.toString());
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                dstTA.requestFocus();
                dstTA.selectAll();
                UIHelper.setTextToClipboard(dstTA.getText());
            }
        });
        
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access")
          public void run()
            {
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                    }
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataModelClassGenerator.class, e);
                    //log.error("Can't change L&F: ", e);
                }
                FixSQLString fix = new FixSQLString();
                fix.setVisible(true);

            }
        });
    }

}
