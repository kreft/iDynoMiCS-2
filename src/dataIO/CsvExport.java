package dataIO;

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
		for(int i = 0; i < 6-String.valueOf(filenr).length(); i++)
			apzero = "0" + apzero;
		return apzero;
	}
	
	/**
	 * @return
	 */
	private String toCsv(String... rowElements)
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
				+ prefix + "_" + DigitFilenr(_filewriterfilenr)  
				+ "_" + Idynomics.simulator.timer.getCurrentIteration() + ".csv";
		_csvFile.fnew(fileString);
	}
	

	public void createCustomFile(String fileName) 
	{
		String fileString = Idynomics.global.outputLocation + "/" 
				+ fileName + ".csv";
		_csvFile.fnew(fileString);
	}
	
	public void createCustomFile(String fileName, String outputfolder) 
	{
		String fileString = outputfolder + "/" 
				+ fileName + ".csv";
		_csvFile.fnew(fileString);
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
		this._csvFile.write(line + "\n");
		this._csvFile.flushAll();
	}
	
	public void writeLine(String[] rowElements) 
	{
		writeLine( toCsv(rowElements) );
	}

}


