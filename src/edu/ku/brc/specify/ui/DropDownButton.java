package edu.ku.brc.specify.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

//@author  santhosh kumar - santhosh@in.fiorano.com 
public abstract class DropDownButton extends JButton implements ChangeListener,
        PopupMenuListener, ActionListener, PropertyChangeListener
{
    private final JButton mainButton   = this;

    private JButton arrowButton  = null;

    private boolean popupVisible = false;
    
    private String statusBarHintText = null;
    

    public DropDownButton()
    {
        super();
        init();
    }

    public DropDownButton(String aLabel, Icon aIcon, int aTextPosition)
    {
        super(aLabel, aIcon);
        init();
        setVerticalTextPosition(aTextPosition);
        setHorizontalTextPosition(JButton.CENTER);
    }

    public DropDownButton(Icon aIcon)
    {
        super(aIcon);
        init();
    }

    protected void init()
    {
        arrowButton  = new JButton(IconManager.getInstance().createAndPutIcon("dropdownarrow", "dropdownarrow.gif"));
        
        Insets insets = new Insets(4,4,4,4);//mainButton.getBorder().getBorderInsets(mainButton);
        mainButton.setBorder(new EmptyBorder(insets));
        arrowButton.setBorder(new EmptyBorder(4,4,4,4));//arrowButton.getBorder().getBorderInsets(arrowButton)));
        mainButton.setIconTextGap(1); 
        mainButton.setMargin(new Insets(0,0,0,0));

        
        mainButton.getModel().addChangeListener(this);
        mainButton.addPropertyChangeListener("enabled", this); // NOI18N
        
        arrowButton.getModel().addChangeListener(this);
        arrowButton.addActionListener(this);
        arrowButton.setMargin(new Insets(3, 3, 3, 3));
        arrowButton.setFocusPainted(false); 
        arrowButton.setFocusable(false);            
    }
    

    /*------------------------------[ PropertyChangeListener ]---------------------------------------------------*/

    public void propertyChange(PropertyChangeEvent evt)
    {
        arrowButton.setEnabled(mainButton.isEnabled());
    }

    /*------------------------------[ ChangeListener ]---------------------------------------------------*/

    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == mainButton.getModel())
        {
            if (popupVisible && !mainButton.getModel().isRollover())
            {
                mainButton.getModel().setRollover(true);
                return;
            }
            arrowButton.getModel().setRollover(
                    mainButton.getModel().isRollover());
            arrowButton.setSelected(mainButton.getModel().isArmed()
                    && mainButton.getModel().isPressed());
        } else
        {
            if (popupVisible && !arrowButton.getModel().isSelected())
            {
                arrowButton.getModel().setSelected(true);
                return;
            }
            mainButton.getModel().setRollover(
                    arrowButton.getModel().isRollover());
        }
    }

    /*------------------------------[ ActionListener ]---------------------------------------------------*/

    public void actionPerformed(ActionEvent ae)
    {
        JPopupMenu popup = getPopupMenu();
        popup.addPopupMenuListener(this);
        popup.show(mainButton, 0, mainButton.getHeight());
    }

    /*------------------------------[ PopupMenuListener ]---------------------------------------------------*/

    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
        popupVisible = true;
        mainButton.getModel().setRollover(true);
        arrowButton.getModel().setSelected(true);
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
        popupVisible = false;

        mainButton.getModel().setRollover(false);
        arrowButton.getModel().setSelected(false);
        ((JPopupMenu) e.getSource()).removePopupMenuListener(this); // act as
                                                                    // good
                                                                    // programmer
                                                                    // :)
    }

    public void popupMenuCanceled(PopupMenuEvent e)
    {
        popupVisible = false;
    }

    /*------------------------------[ Other Methods ]---------------------------------------------------*/

    protected abstract JPopupMenu getPopupMenu();

    public JButton addToToolBar(JToolBar toolbar)
    {
        GridBagLayout      gridbag = new GridBagLayout();
        GridBagConstraints c       = new GridBagConstraints();
        
        JPanel panel = new BtnPanel(gridbag, mainButton, arrowButton);
        c.fill = GridBagConstraints.VERTICAL;
        gridbag.setConstraints(mainButton, c);
        panel.add(mainButton);
        
        gridbag.setConstraints(arrowButton, c);
        panel.add(arrowButton);
        
        toolbar.add(panel);
        return mainButton;
    }
    
    class BtnPanel extends JPanel
    {
        protected EmptyBorder     emptyBorder;
        protected SoftBevelBorder raisedBorder;
        
        protected JButton mainBtn;
        protected JButton arrowButton;
        
        public BtnPanel(LayoutManager aLM, JButton aMainBtn, JButton aArrowBtn)
        {
            super(aLM);
            
            mainBtn     = aMainBtn;
            arrowButton = aArrowBtn;
            
            raisedBorder = new SoftBevelBorder(SoftBevelBorder.RAISED);
            emptyBorder  = new EmptyBorder(raisedBorder.getBorderInsets(this));
            setBorder(emptyBorder);
            
            MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
                public void mouseEntered(MouseEvent e) 
                {
                    setBorder(raisedBorder);
                    displayHintText(statusBarHintText);
                    repaint();
                }
                public void mouseExited(MouseEvent e) 
                {
                    setBorder(emptyBorder);
                    displayHintText("");
                    repaint();
                }
              };
              addMouseListener(mouseInputAdapter);
              addMouseMotionListener(mouseInputAdapter);
              
              aMainBtn.addMouseListener(mouseInputAdapter);
              aMainBtn.addMouseMotionListener(mouseInputAdapter);
              aArrowBtn.addMouseListener(mouseInputAdapter);
              aArrowBtn.addMouseMotionListener(mouseInputAdapter);
        }
        
        protected void displayHintText(String aText)
        {
            JTextField hint = UICacheManager.getInstance().getStatusBarTextField();
            if (hint != null)
            {
                hint.setText(aText != null ? aText : "");
            }
        }
        
        public int getPreferredWidth()
        {
            return getPreferredSize().width;
        }
        
        public void setSize(Dimension aDim)
        {
            aDim.width = getPreferredWidth();
            super.setSize(aDim);
        }
        
        public void setSize(int aX, int aY)
        {
            aX = getPreferredWidth();
            super.setSize(aX, aY);
        }
        
        public void setBounds(int x, int y, int width, int height)
        {
            width = getPreferredWidth();
            super.setBounds(x, y, width, height);
        }
        
        public void setBounds(Rectangle r)       
        {
            r.width = getPreferredWidth();
            super.setBounds(r);
        }
        
        public void paintComponent(Graphics g) 
        {
            mainBtn.setMargin(new Insets(0,0,0,0));
            
            super.paintComponent(g);
            if (getBorder() == raisedBorder)
            {
                Color highlight = raisedBorder.getHighlightInnerColor(mainBtn);
                Color shadow    = raisedBorder.getShadowInnerColor(mainBtn);
                
                g.setColor(shadow);
                Rectangle r = mainBtn.getBounds();
                int x = r.x + r.width ;
                int shrink = 0;
                g.drawLine(x, r.y+shrink, x, r.y+r.height-(shrink*2));
                x++;
                g.setColor(highlight);
                g.drawLine(x, r.y+shrink, x, r.y+r.height-(shrink*2));
            }
        }
    }

    /**
     * @return Returns the statusBarHintText.
     */
    public String getStatusBarHintText()
    {
        return statusBarHintText;
    }

    /**
     * @param statusBarHintText The statusBarHintText to set.
     */
    public void setStatusBarHintText(String statusBarHintText)
    {
        this.statusBarHintText = statusBarHintText;
    }
}