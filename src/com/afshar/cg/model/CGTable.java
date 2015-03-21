package com.afshar.cg.model;

import java.util.List;

public class CGTable
{
	// Name of database table
	private String tableName;		
	
	// Java class name for corresponding database table name, as per Java conventions ('_', '-' removed)
	private String javaClassName;	
	
	// Description name for corresponding database table name, as per Java conventions ('_', '-' removed & space added)
	private String descriptionName; 
	
	private List<CGColumn> columns;

	/**
	 * @return the tableName
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}


	/**
	 * @return the javaClassName
	 */
	public String getJavaClassName()
	{
		return javaClassName;
	}

	/**
	 * @param javaClassName the javaClassName to set
	 */
	public void setJavaClassName(String javaClassName)
	{
		this.javaClassName = javaClassName;
	}

	/**
	 * @return the columns
	 */
	public List<CGColumn> getColumns()
	{
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<CGColumn> columns)
	{
		this.columns = columns;
	}
	
	/**
	 * @return the descriptionName
	 */
	public String getDescriptionName() {
		return descriptionName;
	}

	/**
	 * @param descriptionName the descriptionName to set
	 */
	public void setDescriptionName(String descriptionName) {
		this.descriptionName = descriptionName;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "{ " );
		sb.append( "tableName="+tableName );
		sb.append( "javaClassName="+javaClassName );
		sb.append( "descriptionName="+descriptionName );
		sb.append( ", total columns="+(columns != null ? columns.size() : 0 ) );		
		sb.append( " } " );
		
		return super.toString();
	}

}
