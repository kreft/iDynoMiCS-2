package dataIO;

import java.io.StringWriter;

import idynomics.Global;
import idynomics.Idynomics;

/**
 * Writes the model state to XML files, automatic number increment
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * 
 * NOTE for compression look into Fast Infoset
 *
 */
public class XmlExport
{
	/**
	 * Counter for documents created by this object: ensures unique file names.
	 */
	protected int _fileCounter = 0;
	
	/**
	 * TODO
	 */
	protected FileHandler _xmlFile = new FileHandler();
	
	/**
	 * 
	 */
	protected boolean _exiEncoding = false;
	
	/**
	 * The minimum number of digits allowed in a file name.
	 */
	private final static int NUMBER_OF_DIGITS = Global.file_number_of_digits;
	
	/**
	 * The first lines in any XML document.
	 */
	private final static String XML_HEADER =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<document>\n";
	
	/**
	 * The final line in any XML document.
	 */
	private final static String XML_FOOTER = "</document>\n";
	
	public XmlExport(boolean encoding)
	{
		this._exiEncoding = encoding;
	}
	
	public XmlExport()
	{
		this._exiEncoding = false;
	}
	
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
		if( this._exiEncoding )
			this._xmlFile.bufferOutput();
		String fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ prefix + "_" + this.fileNumberAsPaddedString() + 
				(this._exiEncoding ? ".exi" : ".xml");
		this._xmlFile.fnew(fileString);
		this._xmlFile.write(XML_HEADER);
	}
	
	
	
	/**
	 * Close the XML file and increment the file number counter for the next
	 * file.
	 */
	public void closeXml()
	{
		this._xmlFile.write(XML_FOOTER);
		this._xmlFile.flushAll();
		this._xmlFile.fclose();
		this._fileCounter++;
	}
	
	/**
	 * TODO
	 */
	public void writeState()
	{
		StringWriter outputWriter = new StringWriter();
		outputWriter = Idynomics.simulator.getModule().getXML(1, outputWriter);
		this._xmlFile.write(outputWriter.toString());
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
