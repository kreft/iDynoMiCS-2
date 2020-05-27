package dataIO;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.siemens.ct.exi.core.CodingMode;
import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.FidelityOptions;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.core.grammars.Grammars;
import com.siemens.ct.exi.core.helpers.DefaultEXIFactory;
import com.siemens.ct.exi.grammars.GrammarFactory;
import com.siemens.ct.exi.main.api.sax.EXIResult;

import idynomics.Idynomics;

/**
 * Writes the model state to XML files, automatic number increment
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * 
 * NOTE for compression look into Fast Infoset
 *
 */
public class ExiExport
{
	/**
	 * Counter for documents created by this object: ensures unique file names.
	 */
	protected int _fileCounter = 0;
	
	/**
	 * TODO
	 */
	protected FileHandler _fileHandler = new FileHandler();
	
	protected OutputStream exiOutput;
	
	private StringBuffer outputBuffer;
	
	/**
	 * The minimum number of digits allowed in a file name.
	 */
	private final static int NUMBER_OF_DIGITS = 4;
	
	/**
	 * The first lines in any XML document.
	 */
	private final static String XML_HEADER =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<document>\n";
	
	/**
	 * The final line in any XML document.
	 */
	private final static String XML_FOOTER = "</document>\n";
	
	/**
	 * \brief Formats the file number counter as a string, padding the start
	 * with zeros if necessary.
	 * 
	 * @return String representation of the file number counter.
	 */
	private String fileNumberAsPaddedString()
	{
		String out = String.valueOf(this._fileCounter);
		int numZeros = NUMBER_OF_DIGITS - out.length();
		/* If the number already has enough characters, return it as is. */
		if ( numZeros <= 0 )
			return out;
		/* Otherwise, pad the beginning with enough zeros for the length. */
		return new String(new char[numZeros]).replace("\0", "0") + out;
	}
	
	/**
	 * \brief Create a new XML file with prefix in appropriate folder.
	 * 
	 * @param prefix String for the first part of the file name.
	 */
	public void newXml(String prefix)
	{
		
		String fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ prefix + "_" + this.fileNumberAsPaddedString() + ".exi";
		this._fileHandler.fnew(fileString);
		this._fileHandler.fclose();
		try {
			exiOutput = new FileOutputStream(fileString);
			outputBuffer = new StringBuffer();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outputBuffer.append(XML_HEADER);
	}
	
	/**
	 * Close the XML file and increment the file number counter for the next
	 * file.
	 */
	public void closeXml()
	{
		outputBuffer.append(XML_FOOTER);
		
		EXIFactory factory = DefaultEXIFactory.newInstance();

		factory.setFidelityOptions(FidelityOptions.createDefault());
		factory.setCodingMode(CodingMode.COMPRESSION);
		GrammarFactory grammarFactory = GrammarFactory.newInstance();
		Grammars g = grammarFactory.createSchemaLessGrammars();
		factory.setGrammars( g );
		
		EXIResult exiResult = null;
		try {
			exiResult = new EXIResult(factory);
			exiResult.setOutputStream(exiOutput);
			encode(exiResult.getHandler());
			exiOutput.close();
		} catch (EXIException|IOException|SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this._fileCounter++;
	}
	
	protected void encode(ContentHandler ch) throws SAXException, IOException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(ch);

		// parse xml file
		xmlReader.parse(new InputSource(new ByteArrayInputStream( 
				outputBuffer.toString().getBytes() ) ) );
	}
	
	/**
	 * TODO
	 */
	public void writeState()
	{
		outputBuffer.append(Idynomics.simulator.getModule().getXML(1));
	}
	
	/**
	 * TODO
	 */
	public void writeFile()
	{
		this.newXml(Idynomics.global.simulationName);
		this.writeState();
		this.closeXml();
	}
}
