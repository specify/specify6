/**
 * 
 */
package utils.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @author timo
 *
 */
public class DerivationTree implements Cloneable
{
	protected Symbol value;
	protected Vector<DerivationTree> children = new Vector<DerivationTree>();
	protected DerivationTree parent = null;
	
	/**
	 * @param value
	 */
	public DerivationTree(Symbol value) 
	{
		super();
		this.value = value;
	}
	
	public DerivationTree(Symbol value, DerivationTree parent)
	{
		super();
		this.value = value;
		this.parent = parent;
	}
	
	public DerivationTree addChild(Symbol child)
	{
		DerivationTree result = new DerivationTree(child, this);
		if (children.add(result))
		{
			return result;
		}
		return null;
	}
	
	public int getChildCount()
	{
		return children.size();
	}
	
	public DerivationTree getChild(int child)
	{
		return children.get(child);
	}
	
	public Symbol getChild(Symbol symbol)
	{
		DerivationTree child = getChildNode(symbol);
		if (child != null)
		{
			return child.value;
		}
		return null;
	}
	
	public DerivationTree getChildNode(Symbol symbol)
	{
		for (DerivationTree child : children)
		{
			if (child.value.getName().equals(symbol.getName()))
			{
				return child;
			}
		}
		return null;
	}
	
	/**
	 * @param toRemove
	 * @return
	 */
	public boolean removeChild(DerivationTree toRemove)
	{
		return children.remove(toRemove);
	}
	
	/**
	 * @param depth
	 */
	public void print(int depth)
	{
		String line = "";
		for (int d = 0; d < depth; d++) line += " ";
		System.out.println(line + value);
		for (DerivationTree child : children)
		{
			child.print(depth + 1);
		}
	}

	/**
	 * @return
	 */
	public List<Record> getRecords()
	{
		List<DerivationTree> records = new Vector<DerivationTree>();
		getRecordRoots(records);
		List<Record> result = new Vector<Record>();
		for (DerivationTree record : records)
		{
			result.add(new Record(record.getValue().getTblName(), record.getFieldValues()));
		}
		return result;
	}
	
	/**
	 * @param roots
	 */
	protected void getRecordRoots(List<DerivationTree> roots)
	{
		if (value != null && value.getTblName() != null && value.getFldName() == null)
		{
			roots.add(this);
		} else //I think it's safe to assume no children can also be roots
		{
			for (DerivationTree child : children)
			{
				child.getRecordRoots(roots);
			}
		}
	}
	
	/**
	 * @return
	 */
	protected List<BaseFieldValue> getFieldValues()
	{
		List<BaseFieldValue> result = new ArrayList<BaseFieldValue>();
		getFieldValues(result);
		return result;
	}
	
	/**
	 * @param vals
	 */
	protected void getFieldValues(List<BaseFieldValue> vals)
	{
		for (DerivationTree child : children)
		{
			child.getFieldValues(vals);
		}
		if (value != null && value.getAttribute() != null)
		{
			addFieldAttribute(this, vals, value.getAttribute());
		}
		
	}
	
	/**
	 * @param node
	 * @param vals
	 * @param value
	 */
	protected void addFieldAttribute(DerivationTree node, List<BaseFieldValue> vals, Token attribute)
	{
		if (node == null || node.getValue() == null)
		{
			//throw new Exception("Unable to add Field attribute " + attribute)
			//System.out.println("Unable to add field attribute " + attribute);
		}
		else if (node.getValue().getTblName() != null && node.getValue().getFldName() != null)
		{
			FieldValue fv = (FieldValue) getFieldValue(node, vals);
			fv.getAttributes().add(attribute);
		} else
		{
			addFieldAttribute(node.getParent(), vals, attribute);
		}
	}
	
	/**
	 * @param node
	 * @param vals
	 * @return
	 */
	protected BaseFieldValue getFieldValue(DerivationTree node, List<BaseFieldValue> vals)
	{
		BaseFieldValue result = null;
		for (BaseFieldValue fv : vals)
		{
			if (fv.getTable().equals(node.getValue().getTblName())
					&& fv.getField().equals(node.getValue().getFldName()))
			{
				result = fv;
				break;
			}
		}
		if (result == null)
		{
			result = new FieldValue(node.getValue().getTblName(), node.getValue().getFldName(), node.getValue().getRecordType());
			vals.add(result);
		}
		return result;
	}
	
	/**
	 * @param value
	 * @return
	 */
//	public DerivationTree getNode(Symbol value)
//	{
//		if (this.value == value) //using == on purpose
//		{
//			return this;
//		}
//		for (DerivationTree child : children)
//		{
//			DerivationTree result = child.getNode(value);
//			if (result != null)
//			{
//				return result;
//			}
//		}
//		return null;
//	}
	
	
	/**
	 * @return the parent
	 */
	public DerivationTree getParent() 
	{
		return parent;
	}

	/**
	 * @return the value;
	 */
	public Symbol getValue()
	{
		return value;
	}
	
	public DerivationTree getEquivalentNode(DerivationTree tree)
	{
		Vector<PathNode> path = tree.getPathToRoot(null);
		Collections.reverse(path);
		DerivationTree currentNode = this;
		for (PathNode link : path)
		{
			String myName = currentNode.value == null ? null : currentNode.value.getName();
			String nodeName = link.getSymbol() == null ? null : link.getSymbol().getName();
			if (!(myName == nodeName || (myName != null && myName.equals(nodeName))))
			{
				return null;
			}
			if (link.childNum == null)
			{
//				System.out.println();
//				tree.print(0);
//				System.out.println();
//				currentNode.print(0);
	
				return currentNode;
			}
			if (currentNode.children.size() <= link.childNum) return null;
			currentNode = currentNode.children.get(link.childNum);
		}
		return null;
	}
	
	protected Vector<PathNode> getPathToRoot(DerivationTree child)
	{
		Vector<PathNode> result = new Vector<PathNode>();
		result.add(new PathNode(child == null ? null : children.indexOf(child), value));
		if (parent != null)
		{
			
			result.addAll(parent.getPathToRoot(this));
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException 
	{
		DerivationTree result = new DerivationTree(value);
		for (DerivationTree child : children)
		{
			DerivationTree clonee = (DerivationTree )child.clone();
			clonee.parent = result;
			result.children.add(clonee);
		}
		return result;
	}
	
	private class PathNode
	{
		Integer childNum;
		Symbol symbol;
		/**
		 * @param childNum
		 * @param symbol
		 */
		public PathNode(Integer childNum, Symbol symbol) {
			super();
			this.childNum = childNum;
			this.symbol = symbol;
		}
		
		/**
		 * @return the childNum
		 */
		@SuppressWarnings("unused")
		public Integer getChildNum() {
			return childNum;
		}
		
		/**
		 * @return the symbol
		 */
		public Symbol getSymbol() {
			return symbol;
		}
		
		
	}
}
