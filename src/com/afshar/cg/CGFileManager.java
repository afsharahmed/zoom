/**
 * 
 */
package com.afshar.cg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.afshar.cg.model.CGColumn;
import com.afshar.cg.model.CGTable;

/**
 * @author afshar.ahmed
 * @see http://docs.oracle.com/javase/tutorial/essential/io/fileio.html
 */
public class CGFileManager
{
	private final static Logger	logger	= Logger.getLogger( CGFileManager.class.getName() );

	protected static boolean createCGXML(final String atPath, final List<CGTable> cgTables)
	{
		boolean fileCreated = false;
		try
		{
			System.out.println( "<<Creating CodeGenerator.XML file>>" );
			
			// Create XML for code-generation
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement( "TABLES" );
			doc.appendChild( rootElement );

			for ( CGTable cgTable : cgTables )
			{
				// TABLE element
				Element tableElem = doc.createElement( "TABLE" );
				tableElem.setAttribute( "name", cgTable.getJavaClassName() );
				tableElem.setAttribute( "sqlName", cgTable.getTableName() );
				tableElem.setAttribute( "descName", cgTable.getDescriptionName() );
				rootElement.appendChild( tableElem );

				// Columns element
				Element columnElems = doc.createElement( "COLUMNS" );
				tableElem.appendChild( columnElems );

				for ( CGColumn cgColumn : cgTable.getColumns() )
				{
					// firstname elements
					Element columnElem = doc.createElement( "COLUMN" );
					columnElem.setAttribute( "sqlName", cgColumn.getColumnName() );
					columnElem.setAttribute( "type", cgColumn.getPropertyType() );
					columnElem.setAttribute( "maxLength", cgColumn.getMaxLength().toString() );
					columnElem.appendChild( doc.createTextNode( cgColumn.getPropertyName() ) );
					columnElems.appendChild( columnElem );
				}

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			Properties props = new Properties();
			props.put( "indent", "yes" ); // To get each <ELEMENT> tag on new line
			transformer.setOutputProperties( props );

			DOMSource source = new DOMSource( doc );
			StreamResult result = new StreamResult( new File( atPath + "\\CodeGenerator.xml" ) );
			transformer.transform( source, result );

			System.out.println( "<<Created CodeGenerator.XML file>>" );
			fileCreated = true;
		}
		catch ( NullPointerException e )
		{
			logger.log( Level.SEVERE, "Error occured writing the file" );
			e.printStackTrace();
		}
		catch ( ParserConfigurationException e )
		{
			logger.log( Level.SEVERE, "Error occured while creating XML the file" );
			e.printStackTrace();
		}
		catch ( TransformerConfigurationException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( TransformerException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return fileCreated;
	}

	/**
	 * 
	 * @param atPath
	 * @param cgTables
	 * @see http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
	 */
	protected static List<CGTable> readCGXMLAndGetTables(final String atPath)
	{
		List<CGTable> cgTables = new ArrayList<CGTable>();
		
		try
		{
			File xmlFile = new File( atPath + "\\CodeGenerator.xml" );
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse( xmlFile );
			
			NodeList tableElems = doc.getElementsByTagName("TABLE");

			System.out.println("Total tables found in DB:"+tableElems.getLength());
			
			// Looping over all <TABLE> tags
			for (int i = 0; i < tableElems.getLength(); i++) 
			{
		 		CGTable cgTable = new CGTable();
				Node node = tableElems.item(i);
		 
				if (node.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element elem = (Element) node;
					String javaClassName = elem.getAttribute("name");
					cgTable.setJavaClassName( javaClassName );
					cgTable.setDescriptionName(elem.getAttribute("descName"));
					cgTable.setTableName( elem.getAttribute("sqlName") );
			
					List<CGColumn> cgColumns = new ArrayList<CGColumn>();
					NodeList columnElems = elem.getElementsByTagName("COLUMN");
					
					for (int j = 0; j < columnElems.getLength(); j++) 
					{
				 		CGColumn cgColumn = new CGColumn();
						Node colNode = columnElems.item(j);
						
						if (colNode.getNodeType() == Node.ELEMENT_NODE) 
						{
							Element colElem = (Element) colNode;
							cgColumn.setColumnName( colElem.getAttribute( "sqlName" ) );
							cgColumn.setPropertyType( colElem.getAttribute( "type" ) );
							cgColumn.setPropertyName( colElem.getTextContent() );
							cgColumn.setMaxLength( Integer.parseInt( colElem.getAttribute( "maxLength" ) ) );
							cgColumn.setJavaClassNameForTable( javaClassName );
							cgColumns.add( cgColumn );
							//System.out.println(cgColumn.toString());
						}
							
					}		
														
					cgTable.setColumns( cgColumns );		
							
					//System.out.println(cgTable.toString());	
				}
			
				cgTables.add( cgTable );
			}
			
		}
		catch ( ParserConfigurationException e )
		{
			// TODO: handle exception
		}
		catch ( SAXException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cgTables;
	}
	
	protected static void createFile(final StringBuilder data, final String filePath, final String fileName)
	{
		Writer writer = null;

		try
		{
			writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( filePath + fileName ), "utf-8" ) );
			writer.write( data.toString() );
			System.out.println( "<<File " + filePath + fileName + " written successfully>>" );
		}
		catch ( IOException e )
		{
			System.err.println("Error occured writing the file '" + fileName + "'" );
			e.printStackTrace();
		}
		finally
		{
			try
			{
				writer.close();
			}
			catch ( Exception ex )
			{
			}
		}
	}

	protected static void createDirectories(String dirs)
	{
		
		System.out.println("<<Creating packages>>");
		
		File files = new File(dirs);
		if (!files.exists()) {
			if (files.mkdirs()) {
				System.out.println("Multiple directories are created!");
			} else {
				System.out.println("Failed to create multiple directories!");
			}
		}
		
	}
}
