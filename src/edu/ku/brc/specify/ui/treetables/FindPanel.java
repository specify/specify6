package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.tasks.DualViewSearchable;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateToggleButton;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;

public class FindPanel extends JPanel implements TimingTarget
{
    protected DualViewSearchable views;
    
    protected JButton closeButton;
    protected JLabel findLabel;
    protected JTextField entryField;
    protected JButton findButton;
    protected JButton nextButton;
    protected MultiStateToggleButton whereToggleButton;
    
    protected int mode;
    protected Dimension prefSize;
    protected Dimension contractedSize;
    protected boolean animationInProgress = false;
    protected boolean shrinking = false;
    protected boolean expanding = false;
    
    private static final int EXPANDED = 1;
    private static final int CONTRACTED = -1;
    
    public FindPanel(DualViewSearchable views)
    {
        this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
        this.views = views;
        
        String find = getResourceString("Find");
        String next = getResourceString("Next");

        IconManager.setApplicationClass(Specify.class);
        Icon up    = IconManager.getIcon("Top",IconSize.Std16);
        Icon down  = IconManager.getIcon("Bottom",IconSize.Std16);
        Icon both  = IconManager.getIcon("Both",IconSize.Std16);
        Icon close = IconManager.getIcon("Close");
        
        closeButton = new JButton();
        closeButton.setIcon(close);
        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                contract();
            }
        });
        closeButton.setBorder(null);
        
        findLabel = new JLabel(find + ": ");
        entryField = new JTextField(32);
        entryField.setMaximumSize(entryField.getPreferredSize());
        findButton = new JButton(find);
        findButton.setEnabled(false);
        nextButton = new JButton(next);
        nextButton.setEnabled(false);
        
        whereToggleButton = new MultiStateToggleButton(up,down,both);
        whereToggleButton.setStateIndex(0);
        
        add(closeButton);
        add(Box.createRigidArea(closeButton.getPreferredSize()));
        add(findLabel);
        add(entryField);
        add(findButton);
        add(nextButton);
        add(whereToggleButton);
        add(Box.createHorizontalGlue());
        
        ActionListener buttonListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (ae.getSource() == findButton || (ae.getSource() == entryField && entryField.getText().length() > 0))
                {
                    findClicked();
                }
                else if (ae.getSource() == nextButton)
                {
                    nextClicked();
                }
            }
        };
        
        entryField.addActionListener(buttonListener);
        findButton.addActionListener(buttonListener);
        nextButton.addActionListener(buttonListener);
        
        entryField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                boolean enable = entryField.getText().length() > 0;
                findButton.setEnabled(enable);
                nextButton.setEnabled(enable);
            }
        });
        
        mode = EXPANDED;
        prefSize = super.getPreferredSize();
        contractedSize = new Dimension(prefSize.width,0);
    }

    public void expand()
    {
        if (mode == EXPANDED || expanding || shrinking)
        {
            return;
        }
        
        expanding = true;
        
        // start animation to expand the panel
        Animator expander = new Animator(450,this);
        expander.start();
    }

    public void contract()
    {
        if (mode == CONTRACTED || shrinking || expanding)
        {
            return;
        }
        
        shrinking = true;

        // start animation to shrink the panel
        Animator expander = new Animator(450,this);
        expander.start();
    }
    
    protected void findClicked()
    {
        views.find(entryField.getText(), getWhere(), false);
    }
    
    protected void nextClicked()
    {
        views.findNext(entryField.getText(), getWhere(), false);
    }
    
    protected int getWhere()
    {
        switch (whereToggleButton.getStateIndex())
        {
            case 0:
            {
                return DualViewSearchable.TOPVIEW;
            }
            case 1:
            {
                return DualViewSearchable.BOTTOMVIEW;
            }
            case 2:
            {
                return DualViewSearchable.BOTHVIEWS;
            }
            default:
            {
                return DualViewSearchable.TOPVIEW;
            }
        }
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        if (shrinking || expanding)
        {
            return prefSize;
        }
        
        if (mode == CONTRACTED)
        {
            return contractedSize;
        }
        
        return super.getPreferredSize();
    }
    
    @Override
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
        
    }
    
    public void begin()
    {
        animationInProgress = true;
    }

    public void end()
    {
        animationInProgress = false;
        
        if (expanding)
        {
            mode = EXPANDED;
            expanding = false;
            entryField.requestFocus();
        }
        if (shrinking)
        {
            mode = CONTRACTED;
            shrinking = false;
        }
        
        Component c = getParent();
        c.invalidate();
        c.doLayout();
        c.repaint();
    }

    public void repeat()
    {
        // never gets called
    }

    public void timingEvent(float fraction)
    {
        float sizeFrac = fraction;
        
        if (shrinking)
        {
            sizeFrac = 1 - fraction;
        }
        
        prefSize.height = (int)(super.getPreferredSize().height * sizeFrac);
        System.out.println(fraction + ":" + prefSize.height);
        
        Component c = getParent();
        c.invalidate();
        c.doLayout();
        c.repaint();
        
        this.invalidate();
        this.repaint();
        this.validate();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        UIRegistry.register(UIRegistry.TOPFRAME, f);

        final FindPanel fp = new FindPanel(null);
        fp.expand();

        JButton expand = new JButton("Show find widget");
        expand.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                fp.expand();
            }
        });
        
        BoxLayout layout = new BoxLayout(f.getContentPane(),BoxLayout.PAGE_AXIS);
        f.setLayout(layout);
        f.getContentPane().add(expand);
        f.getContentPane().add(fp);
        
        f.pack();
        f.setVisible(true);
    }
}
