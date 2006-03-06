package edu.ku.brc.specify.tests;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.ui.DbStoreTreeCellRenderer;
import edu.ku.brc.specify.ui.db.DbStoreTreeModel;
import edu.ku.brc.specify.ui.dnd.TreeableTransferHandler;

public class PopulateJTreeFromDbTable
{
	public static void main( String[] args ) throws SQLException
	{
		SessionFactory sf = HibernateUtil.getSessionFactory();
		
		Session session = sf.openSession();
		
		Query query = session.createQuery("from edu.ku.brc.specify.datamodel.GeographyTreeDef as def where def.treeDefId = 781");
		
		GeographyTreeDef treeDef = (GeographyTreeDef)(query.list().get(0));
		
		final DbStoreTreeModel dbtm = new DbStoreTreeModel(Geography.class,treeDef);

		JTree jt1 = new JTree(dbtm);
		jt1.setTransferHandler(new TreeableTransferHandler());
		jt1.setDragEnabled(true);
		jt1.setCellRenderer(new DbStoreTreeCellRenderer());
		
		JTree jt2 = new JTree(dbtm);
		jt2.setTransferHandler(new TreeableTransferHandler());
		jt2.setDragEnabled(true);
		jt2.setCellRenderer(new DbStoreTreeCellRenderer());

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JSplitPane sp = new JSplitPane(SwingConstants.HORIZONTAL,new JScrollPane(jt1),new JScrollPane(jt2));
		
		JButton commitButton = new JButton("commit changes");
		commitButton.addActionListener(new ActionListener(){
			public void actionPerformed( ActionEvent e )
			{
				dbtm.commitChangesToDb();
			}
		});
		
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(sp,BorderLayout.CENTER);
		p.add(commitButton,BorderLayout.SOUTH);
		
		f.add( p );
		
		f.setSize(800,800);
		f.setVisible(true);
		sp.setDividerLocation(0.50);
	}
}
