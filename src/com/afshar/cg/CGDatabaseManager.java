/**
 * 
 */
package com.afshar.cg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.afshar.cg.model.CGColumn;
import com.afshar.cg.model.CGTable;
import com.afshar.cg.util.Conventions;
import com.afshar.cg.util.StringUtils;

/**
 * @author afshar.ahmed
 */
public class CGDatabaseManager
{
//	private final static Logger	logger	= Logger.getLogger( CGDatabaseManager.class.getName() );
	private static Connection	connection;
	private static ResultSet	resultSet, innerResultSet;
	private static String DB_SERVER_TYPE;
	private static String DB_SERVER_HOST_ADDRESS;
	private static String DB_SERVER_USERNAME;
	private static String DB_SERVER_PASSWORD;
	private static String DB_SERVER_DB_NAME;
	
	protected static List<CGTable> getTables(final Properties p)
	{
		List<CGTable> cgTables = new ArrayList<CGTable>();
		PreparedStatement ps = null;
		
		try
		{
			System.out.println( "<<Loading DB Properties>>" );
			DB_SERVER_TYPE 			= p.getProperty("dbServer.type");
			DB_SERVER_HOST_ADDRESS 	= p.getProperty("dbServer.hostAddress");
			DB_SERVER_USERNAME 		= p.getProperty("dbServer.username");
			DB_SERVER_PASSWORD 		= p.getProperty("dbServer.password");
			DB_SERVER_DB_NAME 		= p.getProperty("dbServer.dbName");
			
			System.out.println( "<<DB Properties loaded>>" );
			
			// Loading the driver
			if ( DB_SERVER_TYPE.equalsIgnoreCase( "mysql" ) )
			{
				Class.forName( "com.mysql.jdbc.Driver" ); // For MySQL Database
				connection = DriverManager.getConnection( "jdbc:mysql://" + DB_SERVER_HOST_ADDRESS + "/" + DB_SERVER_DB_NAME + "",
						DB_SERVER_USERNAME, DB_SERVER_PASSWORD );
				ps = connection.prepareStatement( "show tables" );
			}
			else if ( DB_SERVER_TYPE.equalsIgnoreCase( "msaccess" ) )
			{
				// Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"); //For MSAccess
			}
			else if ( DB_SERVER_TYPE.equalsIgnoreCase( "oracle" ) )
			{
				Class.forName( "oracle.jdbc.driver.OracleDriver" ); // For Oracle
				connection = DriverManager.getConnection( "jdbc:oracle:thin:@" + DB_SERVER_HOST_ADDRESS + ":" + DB_SERVER_DB_NAME
						+ ", " + DB_SERVER_USERNAME + ", " + DB_SERVER_PASSWORD );
				ps = connection.prepareStatement( "select * from user_tables" );
			}

			if ( ps != null )
			{
				// Fetch tables
				boolean isResultObtained = ps.execute();

				if ( isResultObtained )
				{
					resultSet = ps.getResultSet();
					Statement statement = connection.createStatement();
									
					System.out.println( "<<Retrieving DB tables>>" );
					
					while ( ( resultSet != null ) && ( resultSet.next() ) )
					{
						String tableName = resultSet.getString( 1 );
						System.out.println( tableName );
						cgTables.add( populateCGTable( tableName, statement ) );						
					}
	
				}

			}

		}
		catch ( ClassNotFoundException e )
		{
			// Handle an error loading the driver
			System.out.println( "Driver class not found!" );
		}
		catch ( SQLException e )
		{
			// Handle an error getting the connection
			System.out.println( "SQLException: " + e.getMessage() );
		}
		finally
		{
			// Close the Connection to release the database resources immediately.
			try
			{
				if ( connection != null ) connection.close();
				if ( resultSet != null ) resultSet.close();
				if ( innerResultSet != null ) innerResultSet.close();
			}
			catch ( SQLException ignored )
			{
			}
		}

		return cgTables;
	}
	
	
	private static CGTable populateCGTable(final String tableName, final Statement statement)
	{
		CGTable cgTable = new CGTable();
		
		try
		{
			String javaClassName = StringUtils.capitalize( Conventions.attributeNameToPropertyName( tableName ) );
			String descriptionName = tableName.replace('_', ' ').replace('-', ' ');
			//System.out.println( "Java class name for table '" + tableName + "'  is   '" + javaClassName+ "'" );

			cgTable.setJavaClassName( javaClassName );
			cgTable.setTableName( tableName );
			cgTable.setDescriptionName(descriptionName);
			
			// populate columns for this table
			ResultSet innerResultSet = statement.executeQuery( "SELECT * FROM " + tableName );
			ResultSetMetaData rsmd = innerResultSet.getMetaData();
			
			//Get number of columns returned
			int numOfCols = rsmd.getColumnCount();
			List<CGColumn> columns = new ArrayList<CGColumn>();
			
			for ( int i = 1; i <= numOfCols; ++i )
			{
				CGColumn column = new CGColumn();
				String sqlColumnName =  rsmd.getColumnName( i );
				String propertyName = convertColumnNameToPropertyName( sqlColumnName );
				column.setColumnName( sqlColumnName );
				column.setPropertyName( propertyName );
				column.setMaxLength(rsmd.getColumnDisplaySize(i));
				column.setPropertyType( rsmd.getColumnClassName( i ).replace( "java.lang.", "" ) ); // Column's Java type 
				column.setJavaClassNameForTable( javaClassName ); //c == '-' || c == '_'
				//column.setIsPrimaryKey( rsmd. )	
				
				columns.add( column );
			}
			
			cgTable.setColumns( columns );
		}
		catch ( SQLException e )
		{
			System.err.println( "Error occured while retriveing data for table: "+tableName );
		}
		
		return cgTable;
	}
	
	private static String convertColumnNameToPropertyName(final String columnName)
	{
		String propertyName = Conventions.attributeNameToPropertyName( columnName.toLowerCase() );
		
		return propertyName;		
	}

}
