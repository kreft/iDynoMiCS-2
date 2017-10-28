package sensitivityAnalysis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import referenceLibrary.XmlRef;
import dataIO.CsvExport;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import idynomics.launchable.SamplerLaunch;
import linearAlgebra.Vector;
import optimization.sampling.Sampler;


/**
 * \brief Creates multiple protocol file from an XML file defining the 
 * parameters with ranges.
 * 
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk), University of Nottingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class ProtocolCreater
{
	public Document _masterDoc;
	
	public String _filePath;
	
	private List<Element> _sampleParams = new ArrayList<Element>();
	
	public String csvHeader = "";
	
	public String resultsFolder = "";
	
	public Sampler _sampler;
	
	public double[][] _bounds;
	
	/**
	 * \brief Main function for creating the protocol files from sensitivity 
	 * analysis 
	 * 
	 * @throws IOException 
	 * 
	 */
	public static void main(String args[]) throws IOException {

		SamplerLaunch sl = new SamplerLaunch();
		sl.initialize( args );
	}
	
	public ProtocolCreater()
	{
		
	}

	/**
	 * \brief Copies the master XML file to multiple output protocol
	 * files, changing the parameter values within provided ranges.
	 * Attributes are changed only for those XML elements which have
	 * <b>range</b> and <b>rangeFor</b> attributes defined.
	 */
	public ProtocolCreater( String doc ) 
	{
		
		_filePath = doc;
		
		try {
			_masterDoc = XmlHandler.xmlLoad(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NodeList allNodes = _masterDoc.getElementsByTagName("*");
		for (int i = 0; i < allNodes.getLength(); i++) {
			if (allNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element currAspect = (Element) allNodes.item(i);
				if (currAspect.hasAttribute(XmlRef.rangeAttribute)) {
					_sampleParams.add(currAspect);
				}
			}
		}
		this._bounds = getRanges(_sampleParams.size(), _sampleParams);
	}
	
	public void setSampler( Sampler.SampleMethod method, int... pars )
	{
		int[] temp = new int[pars.length+1];
		temp[0] = _sampleParams.size();
		for( int i = 0; i < pars.length; i++)
			temp[i+1] = pars[i];
		
		this._sampler = Sampler.getSampler(method, temp);
	}
	
	public void xmlWrite()
	{
		writeOutputs( _sampler.size(), _sampler.sample( this._bounds ));
	}
	
	public double[][] getBounds()
	{
		return this._bounds;
	}
	
	public double[][] getRanges(int k, List<Element> elementParameters )
	{
		double[][] bounds = new double[2][k];
		for (Element currAspect : elementParameters) 
		{
			int idx = elementParameters.indexOf( currAspect );
			String[] inRange = currAspect.getAttribute(
					XmlRef.rangeAttribute ).split(",");
			inRange = checkRange(inRange);
			bounds[0][idx] = Double.parseDouble(inRange[0]);
			bounds[1][idx] = Double.parseDouble(inRange[1]);

		}
		return bounds;
	}
	/**
	 * \brief Checks if the provided range is in proper format
	 * @param valRange String array provided in XML
	 */
	public String[] checkRange(String[] valRange) {
		if (valRange.length != 2 || Double.parseDouble(valRange[0]) >= 
				Double.parseDouble(valRange[1])) {
			System.out.println("Invalid range provided. Please enter range "
					+ "as comma separated value of min and max. (min,max)");
			Scanner user_input = new Scanner(System.in);
			String[] inputRange = user_input.next().split(",");
			user_input.close();
			return inputRange;
		}
		else {
			return valRange;
		}
	}
	
	/**
	 * \brief Creates the protocol file.
	 * @param suffix A string value to be appended to the name of the protocol 
	 * files, which provides the information about the changed attributes.
	 */
	public void newProtocolFile(String suffix)
	{
		String[] fileDirs = _filePath.split("/");
		String fileName = fileDirs[fileDirs.length-1].split("\\.")[0];
		fileDirs = Arrays.copyOf(fileDirs, fileDirs.length-1);
		String dirPath = String.join("/", fileDirs) + "/" 
				+ "SensitivityAnalysisFiles/" + fileName + "/";
		String fileString = dirPath + fileName + "_" + suffix + ".xml";
		try {
			Files.createDirectories(Paths.get(dirPath));
			Transformer _protocolFile = 
					TransformerFactory.newInstance().newTransformer();
			_protocolFile.setOutputProperty( OutputKeys.INDENT, "yes" );
			_protocolFile.setOutputProperty( OutputKeys.METHOD, "xml" );
			_protocolFile.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			_protocolFile.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4" );
		
			_protocolFile.transform(new DOMSource(_masterDoc), 
					new StreamResult(new FileOutputStream(fileString)));
		}
		catch (TransformerException te) {
            System.out.println(te.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
	}
	
	/**
	 * \brief Creates the csv file for the sample space and calls the protocol
	 * file creator function.
	 * @param n Integer specifying the number of protocol files to be created
	 * @param samples A double matrix which holds the sample space
	 */
	public void writeOutputs(int n, double[][] samples)
	{
		Element sim = (Element) _masterDoc.getElementsByTagName(
				XmlRef.simulation ).item(0);
		String simName = sim.getAttribute( XmlRef.nameAttribute );
		
		CsvExport toCSV = new CsvExport();
		Idynomics.global.outputLocation = sim.getAttribute( XmlRef.outputFolder );
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
		toCSV.createCustomFile("xVal_" + dateFormat.format(new Date()));
		toCSV.writeLine(csvHeader);
		
		for (int row = 0; row < n; row++) {
			String suffix = Integer.toString(row+1);
			for (Element currAspect : _sampleParams) {
				int col = _sampleParams.indexOf(currAspect);
				String attrToChange = currAspect.getAttribute(
						XmlRef.rangeForAttribute );
				Double curVal = samples[row][col];
				currAspect.setAttribute(attrToChange, curVal.toString() );
				
			}
			String xValCSV = Vector.toString(samples[row]);
			toCSV.writeLine(xValCSV);
			sim.setAttribute( XmlRef.nameAttribute, simName+"_"+suffix );
			sim.setAttribute( XmlRef.subFolder, resultsFolder + "/" );
			newProtocolFile(suffix);
		}
		toCSV.closeFile();
	}
}
