package dataIO;

import dataIO.Log.Tier;
import idynomics.Idynomics;

public class XmlExport 
{
	/**
	 * TODO
	 */
	protected int _filewriterfilenr = 0;
	
	/**
	 * TODO
	 */
	protected FileHandler _xmlFile = new FileHandler();
	
	/**
	 * handles incrementing file numbering
	 * @param filenr
	 * @return
	 */
	private String DigitFilenr(int filenr) {
		String apzero = String.valueOf(filenr);
		for(int i = 0; i < 4-String.valueOf(filenr).length(); i++)
			apzero = "0" + apzero;
		return apzero;
	}
	
	/**
	 * create a new svg file with prefix in appropriate folder
	 * @param prefix
	 */
	public void newXml(String prefix)
	{
		String fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ prefix + "_" + DigitFilenr(_filewriterfilenr) + ".xml";
		_xmlFile.fnew(fileString);
		Log.out(Tier.EXPRESSIVE, "Writing new file: " + fileString);

		_xmlFile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<document>\n");
	}
	
	/**
	 * close the svg file and increment file number for next file
	 */
	public void closeXml()
	{
		_xmlFile.write("</document>\n");
		_xmlFile.flushAll();
		_xmlFile.fclose();
		_filewriterfilenr++;
	}

	

	
	/**
	 * draw a line
	 * @param File
	 * @param q
	 */
	public void writeState()
	{
		_xmlFile.write(Idynomics.simulator.getNode().getXML(1));
	}
	
	public void writeFile()
	{
		this.newXml(Idynomics.global.simulationName);
		this.writeState();
		this.closeXml();
	}
}
