package dataIO;

import dataIO.Log.Tier;
import idynomics.Idynomics;
import linearAlgebra.Vector;

public class XmlExport 
{
	/**
	 * TODO
	 */
	int filewriterfilenr = 0;
	/**
	 * TODO
	 */
	FileHandler xmlFile = new FileHandler();
	
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
				+ prefix + "_" + DigitFilenr(filewriterfilenr) + ".xml";
		xmlFile.fnew(fileString);
		Log.out(Tier.EXPRESSIVE, "Writing new file: " + fileString);

		xmlFile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<document>\n");
	}
	
	/**
	 * close the svg file and increment file number for next file
	 */
	public void closeXml()
	{
		xmlFile.write("</document>\n");
		xmlFile.fclose();
		filewriterfilenr++;
	}

	
	/**
	 * draw a line
	 * @param File
	 * @param q
	 */
	public void writeState()
	{
		xmlFile.write(Idynomics.simulator.getNode().getXML(1));
	}
}
