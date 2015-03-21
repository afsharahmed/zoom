/**
 * 
 */
package com.afshar.cg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.afshar.cg.model.CGTable;

/**
 * @author afshar.ahmed
 *
 */
public class CodeGenerator
{
	private static String CODE_GENERATION_PATH;
	private static String CODE_GENERATION_BASE_PKG;
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println( "<<Initialising CodeGenerator>>" );		
		
		// Loading configuration properties file from classpath
		InputStream input = Thread.currentThread().getClass().getResourceAsStream("/config/configurations.properties");
		Properties properties = new Properties();

		try 
		{
			// Load Properties object with all available properties, which can be retrieved at any place later 
			properties.load(input);
			
			CODE_GENERATION_PATH = properties.getProperty( "codeGeneration.path" );
			CODE_GENERATION_BASE_PKG = properties.getProperty( "codeGeneration.basePackageName" );
			
			// Retrieve all tables & their columns and create a corresponding list of java objects
			List<CGTable> cgTables = CGDatabaseManager.getTables( properties );
			
			// Using the 'java objects' for sql tables, create a 'CodeGenerator.xml', to enable user editing
			boolean fileCreated = CGFileManager.createCGXML( CODE_GENERATION_PATH, cgTables );
			
			if(fileCreated)
			{
				readUserInput(); // If any exception is occurred here.. program with close.
				
				System.out.println( "<<Parsing CodeGeneration.xml>>" );
				 
				// Parse CodeGeneration.xml file & create new List of CGTable objects from it 
				List<CGTable> verifiedCGTables = CGFileManager.readCGXMLAndGetTables( CODE_GENERATION_PATH );
				
				
				String s = CODE_GENERATION_PATH.replaceAll( Pattern.quote( "/" ),"//" )
						+ CODE_GENERATION_BASE_PKG.replaceAll( Pattern.quote( "." ),"//" ); //Windows only 

				// Create required directory structure 
				CGFileManager.createDirectories(s);
				
				// Scan & Parse all template files & generate code as per template file using data from CodeGeneration.xml
				CGFileScanner.scanTemplateFiles( properties, verifiedCGTables, s );
				
			}
			else 
			{
				System.out.println("Error occured while generating code. Please check logs.");
			}
			
			
		}
		catch (IOException e){
			System.err.println( "Error occured while accessing property file.");
		}
		catch (Exception e){
			System.err.println( "Some error occured while accessing property file.");
			e.printStackTrace();
		}
	}
	
	private static void readUserInput()
	{
		// Prompt the user to perform any required changes to the CodeGeneration.xml file
		System.out.println( "Perform any modifications needed for Java identifier names in 'CodeGeneration.xml'. \nPress Enter to generate the code.\n\n" );

		// open up standard input
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );

		// read the user-input from the command-line with readLine() method
		try
		{
			br.readLine(); 
		}
		catch ( IOException ioe )
		{
			System.out.println( "IO error trying to read your name!" );
			System.exit( 1 );
		}
	}
	
	
	

}
