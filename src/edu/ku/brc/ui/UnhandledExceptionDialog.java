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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 9, 2007
 *
 */
public class UnhandledExceptionDialog extends JDialog
{
    /**
     * @param message
     * @param exception
     */
    public UnhandledExceptionDialog(final String message, final Exception exception)
    {
        this(message, (Throwable)exception);
    }

    /**
     * @param message
     * @param exception
     */
    public UnhandledExceptionDialog(final String message, final Throwable exception)
    {
        super(UIRegistry.getTopWindow() instanceof Frame ? (Frame)UIRegistry.getTopWindow() : null, getResourceString("UnhandledExceptionTitle"), true);
        
        createUI(message, exception);
        
        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setAlwaysOnTop(true);
    }

    /**
     * @param message
     */
    public UnhandledExceptionDialog(final String message)
    {
        super(UIRegistry.getTopWindow() instanceof Frame ? (Frame)UIRegistry.getTopWindow() : null, getResourceString("UnhandledExceptionTitle"), true);
        
        createUI(message, null);
        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setAlwaysOnTop(true);
    }

    /**
     * @param throwable
     */
    public UnhandledExceptionDialog(final Throwable throwable)
    {
        super(UIRegistry.getTopWindow() instanceof Frame ? (Frame)UIRegistry.getTopWindow() : null, getResourceString("UnhandledExceptionTitle"), true);
        
        createUI(throwable.getMessage(), throwable);
        
        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setAlwaysOnTop(true);
    }

    /**
     * Creates the Default UI for Lable task
     * @param message the message
     * @param throwable the exception that caused it (might be null)
     */
    protected void createUI(final String message, final Throwable throwable)
    {
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }

        //meg fixed merge from WB
        //setDefaultCloseOperation(HIDE_ON_CLOSE);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.setModal(false);

        int          height  = 100;
        PanelBuilder builder = new PanelBuilder(new FormLayout("p:g,1dlu,r:p:g", 
                                  "p,"+(throwable != null ? "10px,p,2px,f:p:g," : "")+"5px,p"));
        CellConstraints cc   = new CellConstraints();

        int rowIndex = 1;
        
        JTextArea   messageTA            = new JTextArea(message);
        messageTA.setEditable(false);
        builder.add(new JScrollPane(messageTA, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), cc.xyw(1, 1, 3));
        rowIndex += 2;
        
        JTextArea   stackTraceTA         = null;
        JScrollPane stackTraceScrollPane = null;
        if (throwable != null)
        {
            StringWriter strWriter = new StringWriter();
            PrintWriter  pw        = new PrintWriter(strWriter);

            throwable.printStackTrace(pw);
            if (throwable.getCause() != null)
            {
                throwable.getCause().printStackTrace(pw);    
            }
            createLabel("");

            stackTraceTA         = new JTextArea(strWriter.getBuffer().toString().replace("\t", "    "));
            stackTraceTA.setEditable(false);
            stackTraceScrollPane = new JScrollPane(stackTraceTA, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            stackTraceTA.setRows(15);
            builder.add(createLabel("Stack Trace", SwingConstants.CENTER), cc.xyw(1, rowIndex, 3));
            rowIndex += 2;
            builder.add(stackTraceScrollPane, cc.xyw(1, rowIndex, 3));
            rowIndex += 2;
            height += 300;
        }
        

        // Bottom Button UI
        JButton okBtn = createButton(getResourceString("OK"));
        builder.add(okBtn, cc.xyw(3, rowIndex, 1));

        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                setVisible(false);
            }
        });
        getRootPane().setDefaultButton(okBtn);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        panel.add(builder.getPanel(), BorderLayout.CENTER);
        setContentPane(panel);

        setSize(new Dimension(500, height));

    }
}
