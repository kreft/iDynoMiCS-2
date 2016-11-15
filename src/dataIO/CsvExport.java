package dataIO;

import dataIO.Log.Tier;
import idynomics.Idynomics;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class CsvExport
{
	/**
	 * TODO
	 */
	protected int _filewriterfilenr = 0;
	/**
	 * TODO
	 */
	protected FileHandler _csvFile = new FileHandler();

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
	 * @return
	 */
	private String toCsv(String[] rowElements)
	{
		String out = "";
		for ( int i = 0; i < rowElements.length; i++ )
			out += rowElements[i] + (i != rowElements.length ? "," : " ");
		return out;
	}
	
	/**
	 * create a new svg file with prefix in appropriate folder
	 * @param prefix
	 */
	public void createFile(String prefix)
	{
		String fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ prefix + "_" + DigitFilenr(_filewriterfilenr) + ".csv";
		_csvFile.fnew(fileString);
		Log.out(Tier.EXPRESSIVE, "Writing new file: " + fileString);
	}
	
	/**
	 * close the svg file and increment file number for next file
	 */
	public void closeFile()
	{
		_csvFile.fclose();
		_filewriterfilenr++;
	}

	/**
	 * 
	 */
	public void writeLine(String line) 
	{
		_csvFile.write(line + "\n");
	}
	
	public void writeLine(String[] rowElements) 
	{
		writeLine( toCsv(rowElements) );
	}
}


