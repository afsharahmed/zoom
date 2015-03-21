/**
 * 
 */
package com.afshar.cg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.afshar.cg.model.CGColumn;
import com.afshar.cg.model.CGTable;
import com.afshar.cg.util.StringUtils;

/**
 * @author afshar.ahmed
 * @see http://docs.oracle.com/javase/tutorial/essential/io/
 * @see http://docs.oracle.com/javase/tutorial/essential/io/scanning.html
 */
public class CGFileScanner
{
	private static String			TEMPLATE_FILES_PATH = "/config/template/";
	private static String			TEMPLATES;	
	private static String			CODE_GENERATION_BASE_PCKG;
	
	private static String			PACKAGE_PATTERN;
	private static String			CLASS_NAME_PATTERN;
	private static String			CLASS_NAME_STARTINGWITHLOWERCASE_PATTERN;
	private static String			CLASS_NAME_WITH_SPACES_PATTERN;
	
	private static String			SQL_TABLE_PATTERN;
	private static String			SQL_COLUMN_PATTERN;
	private static String			PROPERTY_TYPE_PATTERN;
	private static String			PROPERTY_NAME_PATTERN;
	private static String 			PROPERTY_MAX_LENGTH_PATTERN;
	private static String			PROPERTY_NAME_STARTINGWITHUPPERCASE_PATTERN;
	private static String			IDENTIFIER_PROPERTY_NAME_PATTERN;
	
	private static String			LOOP_START_PATTERN;
	private static String			LOOP_END_PATTERN;
	private static String			CLASS_NAME_SUFFIX;
	private static String 			LINE_SEPARATOR = System.getProperty("line.separator");
	private static List<CGTable> 	CGTABLES = null;

	protected static void scanTemplateFiles(final Properties p, final List<CGTable> cgTables, final String codeGenerationPath)
	{
		TEMPLATES 									= p.getProperty( "codeGeneration.templates" );
		CODE_GENERATION_BASE_PCKG 					= p.getProperty( "codeGeneration.basePackageName" );
		PACKAGE_PATTERN 							= p.getProperty( "codeGeneration.packagePattern" );
		CLASS_NAME_PATTERN 							= p.getProperty( "codeGeneration.classNamePattern" );
		CLASS_NAME_STARTINGWITHLOWERCASE_PATTERN	= p.getProperty( "codeGeneration.classNameStartingWithLowercasePattern" );
		CLASS_NAME_WITH_SPACES_PATTERN				= p.getProperty( "codeGeneration.classNameWithSpacesPattern");
		
		SQL_TABLE_PATTERN 							= p.getProperty( "codeGeneration.sqlTablePattern" );
		SQL_COLUMN_PATTERN 							= p.getProperty( "codeGeneration.sqlColumnPattern" );
		PROPERTY_TYPE_PATTERN 						= p.getProperty( "codeGeneration.propertyTypePattern" );
		PROPERTY_NAME_PATTERN 						= p.getProperty( "codeGeneration.propertyNamePattern" );
		PROPERTY_MAX_LENGTH_PATTERN					= p.getProperty( "codeGeneration.propertyMaxLengthPattern" );
		PROPERTY_NAME_STARTINGWITHUPPERCASE_PATTERN = p.getProperty( "codeGeneration.propertyNameStartingWithUppercasePattern" );
		IDENTIFIER_PROPERTY_NAME_PATTERN			= p.getProperty( "codeGeneration.identifierPropertyNamePattern" );
		
		LOOP_START_PATTERN 							= p.getProperty( "codeGeneration.loopStartPattern" );
		LOOP_END_PATTERN 							= p.getProperty( "codeGeneration.loopEndPattern" );
		CLASS_NAME_SUFFIX 							= p.getProperty( "codeGeneration.classNameSuffix" );
				
		CGTABLES = cgTables;
		
		try 
		{
			// Read all comma separated template file names & split into an array
			String[] templates = TEMPLATES.split( "," );
			
			// Read all comma separated java class file name suffixes (eg: *Dao, *Manager) & split into an array
			String[] filesSuffixes = CLASS_NAME_SUFFIX.split( "," );
					
			// Create N files for each template file (where N = total number of DB tables)
			for ( int i = 0; i < templates.length; i++ )
				createFile( templates[i], filesSuffixes[i], codeGenerationPath);
			
			// Create a configuration file separately to list all lines for each table together 
			createConfigurationFile(codeGenerationPath);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.err.println("Number of template files and suffixes provided are not equal.");
			e.printStackTrace();
		}
		catch (Exception e) {
			System.err.println("Some error occured while splitting ");
			e.printStackTrace();
		}
		
	}
	
	private static void createConfigurationFile(String codeGenerationPath)
	{
		System.out.println( "<<Creating 'Configurations.xml' for all tables>>" );
		
		// Appending Configurations.xml path
		codeGenerationPath = codeGenerationPath + "//";
		
		CGFileManager.createFile( createConfigurationFileData(), codeGenerationPath, "Configurations.xml");

	}
	
	private static void createFile(final String templateFilename, final String fileSuffix, String codeGenerationPath)
	{
		// Create code for a template (eg: Model) for each of the tables 
		List<StringBuilder> fileList = createFileData( templateFilename );
		
		// Appending '(t)emplateFilename' directory to existing directory structure, for '(T)emplateFilename'
		codeGenerationPath = codeGenerationPath + "//" + StringUtils.uncapitalize( templateFilename ) + "//";
		
		// Create directory for this templateFilename
		CGFileManager.createDirectories( codeGenerationPath );
		
		// Loop over all tables
		for ( int i = 0; i < CGTABLES.size(); i++ )
		{
			String tablesJavaName = CGTABLES.get( i ).getJavaClassName();
			
			System.out.println( "<<Creating '"+templateFilename+"' for '"+tablesJavaName+"'>>" );

			// Create actual .java code files
			CGFileManager.createFile( fileList.get( i ), codeGenerationPath,  tablesJavaName + fileSuffix);
		} 
	}

	/**
	 * Create code for a given template, for all tables and put in corresponding StringBuilder
	 * @param templateFilename
	 * @return
	 */
	private static List<StringBuilder> createFileData(final String templateFilename)
	{
		Class runtimeClass = Thread.currentThread().getClass();
		InputStream input = runtimeClass.getResourceAsStream( TEMPLATE_FILES_PATH + templateFilename + ".txt" );
		Scanner scanner = new Scanner( input );
		
		int totalTables = CGTABLES.size();
		
		List<StringBuilder> fileList = new ArrayList<StringBuilder>();
		for ( int i = 0; i < CGTABLES.size(); i++ )
		{
			StringBuilder sb = new StringBuilder(); // One StringBuilder per Model file 
			fileList.add( sb );
		} 
		
		List<String> codeBlockLines = new ArrayList<String>();
		boolean createCodeBlock = false;

		while ( scanner.hasNextLine() )
		{
			String line = scanner.nextLine();
			
			if(line.trim().isEmpty() )	// If line empty
			{
				if ( !createCodeBlock )// Not a loop line
				{ 				
					for ( int i = 0; i < totalTables; i++ )
						fileList.get( i ).append( LINE_SEPARATOR );	
				}
				else if ( createCodeBlock )
				{
					codeBlockLines.add( LINE_SEPARATOR );
				}
							
			}
			else if(!line.contains( "@@" ))  // If line not empty but has no REPLACEABLE string
			{
				if ( !createCodeBlock )// Not a loop line
				{ 				
					for ( int i = 0; i < totalTables; i++ )
						fileList.get( i ).append( line ).append( LINE_SEPARATOR );

				}
				else if ( createCodeBlock )
				{
					codeBlockLines.add( line.concat( LINE_SEPARATOR ) );
				}
				
			}
			else // If line not empty & has a REPLACEABLE string
			{
				if ( line.contains( LOOP_START_PATTERN ) && !createCodeBlock )
				{
					createCodeBlock = true;
					//break;// Exit out of current loop, to skip parsing line with '#LOOP_BEGIN#'
				}
				else if ( line.contains( LOOP_END_PATTERN ) && createCodeBlock )
				{
					
					
					for ( int i = 0; i < totalTables; i++ )
						createCodeBlock( fileList.get( i ), CGTABLES.get( i ), codeBlockLines );
					
					//Reset CodeBlockLines Array 
					createCodeBlock = false;
					codeBlockLines = new ArrayList<String>();
										
				}
				else if ( !createCodeBlock )// Not a loop line
				{ 				
					for ( int i = 0; i < totalTables; i++ )
						fileList.get( i ).append( replacePattern( line, CGTABLES.get( i ) ) ).append( LINE_SEPARATOR );

				}
				else if ( createCodeBlock )
				{
					codeBlockLines.add( line.concat( LINE_SEPARATOR ) );
				}
			}
			

		}// Finished with scanning & replacements

		
		
		return fileList;
	}

	private static void createCodeBlock(StringBuilder currentStringBuilder, CGTable cgTable,
			List<String> templateLines)
	{
		List<CGColumn> cgColumns = cgTable.getColumns();

		// Loop over all tables and create a parsed line for each template file
		for ( CGColumn cgColumn : cgColumns )
		{
			for ( String line : templateLines )
				currentStringBuilder.append( replacePattern( line, cgColumn ) );
		}
					
		
	}

	private static String replacePattern(final String targetLine, Object sqlObject)
	{
		String replacedString = targetLine;
		// String output = matcher.replaceFirst(replacement); // first match only

		// Replace by 'package' name
		Matcher packageMatcher = Pattern.compile( PACKAGE_PATTERN ).matcher( replacedString );

		if ( packageMatcher.find() )
			replacedString = packageMatcher.replaceAll( CODE_GENERATION_BASE_PCKG ); // com.onmobile.zero

		
		// Replace by 'class' name
		Matcher classMatcher = Pattern.compile( CLASS_NAME_PATTERN ).matcher( replacedString );

		if ( classMatcher.find() )
			replacedString = classMatcher.replaceAll( ( (CGTable) sqlObject ).getJavaClassName() ); 
		
		
		// Replace by 'class' name (reference object naming style) with First-Char-In-Lowercase
		Matcher classNameLoMatcher = Pattern.compile( CLASS_NAME_STARTINGWITHLOWERCASE_PATTERN ).matcher( replacedString );

		if ( classNameLoMatcher.find() )
			replacedString = classNameLoMatcher.replaceAll( StringUtils.uncapitalize( ( (CGTable) sqlObject ).getJavaClassName() ) ); 

		// Replace by 'class-name-with-spaces' 
		Matcher classDescNameMatcher = Pattern.compile( CLASS_NAME_WITH_SPACES_PATTERN ).matcher( replacedString );

		if ( classDescNameMatcher.find() )
			replacedString = classDescNameMatcher.replaceAll( ( (CGTable) sqlObject ).getDescriptionName() ); 
		
		// Replace by 'sql_table' name for @Table(name=??) annotation
		Matcher sqlTableMatcher = Pattern.compile( SQL_TABLE_PATTERN ).matcher( replacedString );

		if ( sqlTableMatcher.find() )
			replacedString = sqlTableMatcher.replaceAll( ( (CGTable) sqlObject ).getTableName() ); 
		

		// Replace by 'Primary Key property' name (Note: Its not column name)
		Matcher identifierPropertyMatcher = Pattern.compile( IDENTIFIER_PROPERTY_NAME_PATTERN ).matcher( replacedString );

		if ( identifierPropertyMatcher.find() )
			replacedString = identifierPropertyMatcher.replaceAll( ( (CGTable) sqlObject ).getColumns().get( 0 ).getPropertyName() ); 

		
		// Replace by 'sql_column' name for @Column(name=??) annotation
		Matcher sqlColumnMatcher = Pattern.compile( SQL_COLUMN_PATTERN ).matcher( replacedString );

		if ( sqlColumnMatcher.find() )
			replacedString = sqlColumnMatcher.replaceAll( ( (CGColumn) sqlObject ).getColumnName() ); 


		// Replace by 'property_java_type' name
		Matcher propertyTypeMatcher = Pattern.compile( PROPERTY_TYPE_PATTERN ).matcher( replacedString );

		if ( propertyTypeMatcher.find() )
			replacedString = propertyTypeMatcher.replaceAll( ( (CGColumn) sqlObject ).getPropertyType() ); 


		// Replace by 'property' name
		Matcher propertyNameMatcher = Pattern.compile( PROPERTY_NAME_PATTERN ).matcher( replacedString );

		if ( propertyNameMatcher.find() )
			replacedString = propertyNameMatcher.replaceAll( ( (CGColumn) sqlObject ).getPropertyName() ); 


		// Replace by 'property' name with First-Char-In-Uppercase
		Matcher propertyNameUpMatcher = Pattern.compile( PROPERTY_NAME_STARTINGWITHUPPERCASE_PATTERN ).matcher( replacedString );

		if ( propertyNameUpMatcher.find() )
			replacedString = propertyNameUpMatcher.replaceAll( StringUtils.capitalize( ( (CGColumn) sqlObject ).getPropertyName() ) ); 

		
		// Replace by 'property' max-length 
		Matcher propertyMaxLengthMatcher = Pattern.compile( PROPERTY_MAX_LENGTH_PATTERN ).matcher( replacedString );

		if ( propertyMaxLengthMatcher.find() )
			replacedString = propertyMaxLengthMatcher.replaceAll( ( (CGColumn) sqlObject ).getMaxLength().toString()  ); 
		
		
		return replacedString;
	}

	
	
	/**
	 * Create code for a given template, for all tables and put in corresponding StringBuilder
	 * @param templateFilename
	 * @return
	 */
	private static StringBuilder createConfigurationFileData()
	{
		Class runtimeClass = Thread.currentThread().getClass();
		InputStream input = runtimeClass.getResourceAsStream( TEMPLATE_FILES_PATH + "XMLConfigurations.txt" );
		Scanner scanner = new Scanner( input );
		StringBuilder sb = new StringBuilder();
		
		List<String> codeBlockLines = new ArrayList<String>();
		boolean createCodeBlock = false;
		int totalTables = CGTABLES.size();
		
		while ( scanner.hasNextLine() )
		{
			String line = scanner.nextLine();
			
			if(line.trim().isEmpty() )	// If line empty
			{
				if (!createCodeBlock)// Not a loop line
					sb.append(LINE_SEPARATOR);
				else if (createCodeBlock) 
					codeBlockLines.add(LINE_SEPARATOR);
				
			}
			else if(!line.contains( "@@" ))  // If line not empty but has no REPLACEABLE string
			{
				if (!createCodeBlock)// Not a loop line
					sb.append( line ).append( LINE_SEPARATOR );
				else if (createCodeBlock) 
					codeBlockLines.add(line);

			}
			else // If line not empty & has a REPLACEABLE string
			{
				if ( line.contains( LOOP_START_PATTERN ) && !createCodeBlock )
				{
					createCodeBlock = true;
				}
				else if ( line.contains( LOOP_END_PATTERN ) && createCodeBlock )
				{
					for ( int i = 0; i < totalTables; i++ )
					{
						for( String s : codeBlockLines)
						{
							sb.append(replacePattern(s, CGTABLES.get( i )));//.append( LINE_SEPARATOR );
						}
					}
					
					//Reset CodeBlockLines Array 
					createCodeBlock = false;
					codeBlockLines = new ArrayList<String>();
				}
				else if ( !createCodeBlock )// Not a loop line
				{
					// Since a configuration file will only have LOOPS, there is nothing to code here
				}
				else if ( createCodeBlock )
				{
					codeBlockLines.add( line.concat( LINE_SEPARATOR ) );
				}
			}
			

		}// Finished with scanning & replacements

		
		
		return sb;
	}

}
