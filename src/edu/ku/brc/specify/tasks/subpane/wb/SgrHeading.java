package edu.ku.brc.specify.tasks.subpane.wb;

class SgrHeading implements GridTableHeader {
	private final Short viewOrder;

	public SgrHeading(short viewOrder)
	{
		this.viewOrder = viewOrder;
	}
	
	@Override
	public int compareTo(GridTableHeader o) 
	{
		return this.getViewOrder() - o.getViewOrder();
	}

	@Override
	public Short getViewOrder() 
	{
		return viewOrder;
	}

	@Override
	public String getTableName() 
	{
		return "N/A";
	}

	@Override
	public String getFieldName() 
	{
		return "N/A";
	}

	@Override
	public Class<?> getDataType() 
	{
		return Float.class;
	}

	@Override
	public Short getDataFieldLength() 
	{
		return 10;
	}

	@Override
	public String getCaption() {
		return "SGR Score";
	}
}