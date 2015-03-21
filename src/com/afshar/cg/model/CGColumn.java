package com.afshar.cg.model;

public class CGColumn
{
	private String	columnName; //SQL column name

	private String	propertyName;

	private String	propertyType;
	
	private Integer maxLength;
	
	private String	javaClassNameForTable;


	/**
	 * @return the columnName
	 */
	public String getColumnName()
	{
		return columnName;
	}

	/**
	 * @param columnName
	 *            the columnName to set
	 */
	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}


	/**
	 * @return the propertyName
	 */
	public String getPropertyName()
	{
		return propertyName;
	}

	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(String propertyName)
	{
		this.propertyName = propertyName;
	}

	/**
	 * @return the propertyType
	 */
	public String getPropertyType()
	{
		return propertyType;
	}

	/**
	 * @param propertyType the propertyType to set
	 */
	public void setPropertyType(String propertyType)
	{
		this.propertyType = propertyType;
	}


	/**
	 * @return the maxLength
	 */
	public Integer getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength the maxLength to set
	 */
	public void setMaxLength(Integer maxLength) {
		this.maxLength = (maxLength == null ? 255 : maxLength);
	}

	/**
	 * @return the javaClassNameForTable
	 */
	public String getJavaClassNameForTable() {
		return javaClassNameForTable;
	}

	/**
	 * @param javaClassNameForTable the javaClassNameForTable to set
	 */
	public void setJavaClassNameForTable(String javaClassNameForTable) {
		this.javaClassNameForTable = javaClassNameForTable;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "{ " );
		sb.append( "columnName=" + columnName );
		sb.append( ", propertyName=" + propertyName );
		sb.append( ", propertyType=" + propertyType );
		sb.append( ", maxLength=" + maxLength );
		sb.append( ", javaClassNameForTable=" + javaClassNameForTable );
		sb.append( " } " );
		return sb.toString();
	}

}
