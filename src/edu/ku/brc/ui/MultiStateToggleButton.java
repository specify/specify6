package edu.ku.brc.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import edu.ku.brc.util.Pair;

public class MultiStateToggleButton extends JButton
{
    protected List<String> stateNames   = new Vector<String>();
    protected List<Icon>   icons        = new Vector<Icon>();
    protected int          currentIndex = 0;
    
    public MultiStateToggleButton(Pair<Icon,String>... iconsAndNames)
    {
        for (Pair<Icon,String> iconNamePair: iconsAndNames)
        {
            stateNames.add(iconNamePair.second);
            icons.add(iconNamePair.first);
        }
        setupInternalActionListener();
    }
    
    public MultiStateToggleButton(Icon... icons)
    {
        for (Icon icon: icons)
        {
            this.icons.add(icon);
            stateNames.add(null);
        }
        setupInternalActionListener();
    }
    
    protected void setupInternalActionListener()
    {
        addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                clicked();
            }
        });
    }
    
    protected void clicked()
    {
        currentIndex++;
        currentIndex %= stateNames.size();
        updateState();
    }
    
    protected void updateState()
    {
        setIcon(icons.get(currentIndex));
        
        String name = stateNames.get(currentIndex);
        if (name != null)
        {
            setText(name);
        }
    }
    
    public int getStateIndex()
    {
        return currentIndex;
    }
    
    public void setStateIndex(int index)
    {
        this.currentIndex = index;
        updateState();
    }
    
    public static void main(String[] args)
    {
        Icon i1 = new ImageIcon("/home/jstewart/Desktop/RightSideUp.png");
        Icon i2 = new ImageIcon("/home/jstewart/Desktop/LeftSideDown.png");
        Icon i3 = new ImageIcon("/home/jstewart/Desktop/UpAndDown.png");
        
//        Pair<Icon,String> p1 = new Pair<Icon,String>(i1,null);
//        Pair<Icon,String> p2 = new Pair<Icon,String>(i2,null);
//        Pair<Icon,String> p3 = new Pair<Icon,String>(i3,null);
        
        JFrame f = new JFrame();
//        MultiStateToggleButton btn = new MultiStateToggleButton(p1, p2, p3);
        MultiStateToggleButton btn = new MultiStateToggleButton(i1, i2, i3);
        f.add(btn);
        f.pack();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        btn.setStateIndex(0);
        btn.setText("Where");
        
        f.setVisible(true);
    }
}
