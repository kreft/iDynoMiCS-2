package sensitivityAnalysis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.CsvExport;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import idynomics.launchable.SamplerLaunch;
import linearAlgebra.Vector;
import optimization.sampling.Sampler;
import referenceLibrary.XmlRef;
import utility.Helper;


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
	
	public double[][] _ranges;

	private int[] _scale;
	
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
	public ProtocolCreater( String doc, boolean custom ) 
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
					csvHeader += currAspect.getAttribute(
							XmlRef.nameAttribute) + ",";
				}
			}
		}
		if( custom )
			this._ranges = getCustomRanges(_sampleParams.size(), _sampleParams);
		else
			this._ranges = getRanges(_sampleParams.size(), _sampleParams);
		
		this._scale = getType(_sampleParams.size(),_sampleParams);
	}
	
	public void setSampler( Sampler.SampleMethod method, int... pars )
	{
		int[] temp = new int[pars.length+1];
		temp[0] = _sampleParams.size();
		for( int i = 0; i < pars.length; i++)
			temp[i+1] = pars[i];
		
		this._sampler = Sampler.getSampler(method, this._ranges, temp);
	}
	
	public void xmlWrite()
	{
		writeOutputs( _sampler.size(), _sampler.sample( this._ranges, this._scale ), 0);
	}
	
	public double[][] getBounds()
	{
		return this._ranges;
	}
	
	public double[][] getRanges( int k, List<Element> elementParameters )
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
	
	public double[][] getCustomRanges( int k, List<Element> elementParameters )
	{
		HashMap<Integer,double[]> range = new HashMap<Integer,double[]>();
		int l = 0;
		for (int i = 0; i < elementParameters.size(); i++) 
		{
			Element currAspect = elementParameters.get(i);
			String[] inRange = currAspect.getAttribute(
					XmlRef.rangeAttribute ).split(",");
			double[] temp = new double[inRange.length];
			
			l = Math.max(l, inRange.length);
			for( int j = 0; j < temp.length; j++)
				temp[j] = Double.parseDouble(inRange[j]);
			range.put(i, temp);
		}
		double[][] out = new double[l][k];
		for(int i = 0; i < range.size(); i++)
		{
			double[] tmp = range.get(i);
			for( int j = 0; j < l; j++)
				out[j][i] = ( j > tmp.length ? Double.MAX_VALUE : tmp[j]);
		}
		return out;
	}

	
	public int[] getType(int k, List<Element> elementParameters )
	{
		int[] out = new int[k];
		String t;
		int idx;
		for (Element currAspect : elementParameters) 
		{
			idx = elementParameters.indexOf( currAspect );
			t = XmlHandler.gatherAttribute(currAspect, XmlRef.rangeScaleAttribute);
			if( t != null && t.equals("exp") )
				out[idx] = 1;
			else
				out[idx] = 0;
		}
		return out;
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
	public void newProtocolFile(String suffix, String folder)
	{
		String sub = ( Helper.isNullOrEmpty( Idynomics.global.subFolderStruct) ?
				"output" : Idynomics.global.subFolderStruct );
		String protocol_loc = Idynomics.global.outputRoot + "/" + sub;
		
		String[] fileDirs;
		/* In case of windows */
		if( _filePath.contains("\\"))
		{
			fileDirs = _filePath.split("\\\\");
		}
		else
			fileDirs = _filePath.split("/");
		
		String fileName = fileDirs[fileDirs.length-1].split("\\.")[0];
		fileDirs = Arrays.copyOf(fileDirs, fileDirs.length-1);
		String dirPath = "";
		if (resultsFolder != "")
		{
			dirPath = String.join("/", fileDirs) + "/"
					+ resultsFolder + "/";
		}
		else
		{
			dirPath = String.join("/", fileDirs) + "/"
					+ "SensitivityAnalysisFiles/" + fileName + "/";
		}
		dirPath = protocol_loc + "/" + folder + "/";
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
	public void writeOutputs(int n, double[][] samples, int genCount)
	{
		Element sim = (Element) _masterDoc.getElementsByTagName(
				XmlRef.simulation ).item(0);
		String simName = sim.getAttribute( XmlRef.nameAttribute );
		
		CsvExport toCSV = new CsvExport();
		
		String sub = ( Helper.isNullOrEmpty( Idynomics.global.subFolderStruct) ?
				"output" : Idynomics.global.subFolderStruct );
		String csv_loc = Idynomics.global.outputRoot + "/" + sub + "/input/gen_" + genCount;
		
	
		toCSV.createCustomFile("xVal", csv_loc);
		toCSV.writeLine( csvHeader.substring(0, csvHeader.length() - 1) );
		
		for (int row = 0; row < n; row++)
		{
			String suffix = Integer.toString(row+1);
			for (Element currAspect : _sampleParams)
			{
				int col = _sampleParams.indexOf(currAspect);
				String attrToChange = currAspect.getAttribute(
						XmlRef.rangeForAttribute );
				Double curVal = samples[row][col];
				currAspect.setAttribute(attrToChange, curVal.toString() );
				
			}
			String xValCSV = Vector.toString(samples[row]);
			toCSV.writeLine(xValCSV);
			sim.setAttribute( XmlRef.nameAttribute, simName+"_"+suffix );
			sim.setAttribute( XmlRef.subFolder, Idynomics.global.subFolderStruct + "/result/gen_" + (genCount) );
			newProtocolFile(suffix, "protocol/gen_" + genCount);
		}
		toCSV.closeFile();
	}
}
