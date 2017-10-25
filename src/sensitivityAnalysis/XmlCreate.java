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
import utility.Helper;
import dataIO.CsvExport;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;

/**
 * \brief Creates multiple protocol file from an XML file defining the 
 * parameters with ranges.
 * 
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk), University of Nottingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class XmlCreate
{
	public static Document _sensitivityDoc;
	
	public static String _filePath;
	
	private static List<Element> _sensParams = new ArrayList<Element>();
	
	/**
	 * \brief Main function for creating the protocol files from sensitivity 
	 * analysis 
	 * 
	 * @throws IOException 
	 * 
	 */
	public static void main(String args[]) throws IOException {
		System.out.println("Creating protocol files for Sensitivity Analysis");
		
		String xmlFilePath;
		if ( args == null || args.length == 0 || args[0] == null )
		{
			@SuppressWarnings("resource")
			Scanner user_input = new Scanner( System.in );
			System.out.print("Enter protocol file path: ");
			xmlFilePath = user_input.next();
//			user_input.close();
		}
		else
		{
			xmlFilePath = args[0];
		}
		
		_filePath = xmlFilePath;
		_sensitivityDoc = XmlHandler.xmlLoad(_filePath);
		xmlCopy();
	}
	
	/**
	 * \brief Copies the XML file to multiple output protocol files,
	 * changing the parameter values within provided ranges.
	 * Attributes are changed only for those XML elements which have
	 * <b>range</b> and <b>rangeFor</b> attributes defined.
	 */
	public static void xmlCopy() {
		NodeList allNodes = _sensitivityDoc.getElementsByTagName("*");
		for (int i = 0; i < allNodes.getLength(); i++) {
			if (allNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element currAspect = (Element) allNodes.item(i);
				if (currAspect.hasAttribute(XmlRef.rangeAttribute)) {
					_sensParams.add(currAspect);
				}
			}
		}
		
		/* Parameters for Morris method */
		int k = _sensParams.size();
		if (k == 0) {
			System.err.println("No range attribute defined for any parameter. "
					+ "Exiting.");
			return;
		}
		
		/* Number of levels. Ask in input file? */
		int p = Integer.valueOf( Helper.obtainInput( "", 
				"Number of sampling levels.", false));     
		/* Number of repetitions. From input? */
		int r = Integer.valueOf( Helper.obtainInput( "", 
				"Number of repetitions", false));         
		
		double[][] discreteUniformProbs = MorrisSampling.morris(k,p,r);
		
		double[] ones = Vector.onesDbl(r*(k+1));

		double[] inpMax = new double[k];
		double[] inpMin = new double[k];
		
		String[] rangeAspectNames = new String[k];
		String[][] ranges = new String[k][2];
		String csvHeader = "";
		for (Element currAspect : _sensParams) 
		{
			int idx = _sensParams.indexOf( currAspect );
			rangeAspectNames[idx] = currAspect.getAttribute( 
					XmlRef.nameAttribute );
			String[] inRange = currAspect.getAttribute(
					XmlRef.rangeAttribute ).split(",");
			inRange = checkRange(inRange);
			inpMax[idx] = Double.parseDouble(inRange[1]);
			inpMin[idx] = Double.parseDouble(inRange[0]);
			ranges[idx] = inRange.clone();
			csvHeader += currAspect.getAttribute( XmlRef.nameAttribute )
					+ ( (idx == (_sensParams.size() - 1)) ? "" : ", ");
		}
		
		/* Variable states: ( ones*inpMin + 
		 * ones*( inpMax-inpMin ) ).*discreteUniformProbs
		 */
		double[][] states = Matrix.add(Vector.outerProduct(ones, inpMin),
				Matrix.elemTimes(Vector.outerProduct(ones, 
						Vector.minus(inpMax, inpMin) ), discreteUniformProbs) );
		
		Element sim = (Element) _sensitivityDoc.getElementsByTagName(
				XmlRef.simulation ).item(0);
		String simName = sim.getAttribute( XmlRef.nameAttribute );
		
		CsvExport toCSV = new CsvExport();
		Idynomics.global.outputLocation = sim.getAttribute( XmlRef.outputFolder );
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
		toCSV.createCustomFile("xVal_"+dateFormat.format(new Date()));
		toCSV.writeLine(csvHeader);
		
		for (int row = 0; row < r*(k+1); row++) {
			String suffix = Integer.toString(row+1);
			for (Element currAspect : _sensParams) {
				int col = _sensParams.indexOf(currAspect);
				String attrToChange = currAspect.getAttribute(
						XmlRef.rangeForAttribute );
				Double curVal = states[row][col];
				currAspect.setAttribute(attrToChange, curVal.toString() );
				
			}
			String xValCSV = Vector.toString(states[row]);
			toCSV.writeLine(xValCSV);
			sim.setAttribute( XmlRef.nameAttribute, simName+"_"+suffix );
			newProtocolFile(suffix);
		}
		toCSV.closeFile();
	}
	
	/**
	 * \brief Checks if the provided range is in proper format
	 * @param valRange String array provided in XML
	 */
	public static String[] checkRange(String[] valRange) {
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
	public static void newProtocolFile(String suffix)
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
		
			_protocolFile.transform(new DOMSource(_sensitivityDoc), 
					new StreamResult(new FileOutputStream(fileString)));
		}
		catch (TransformerException te) {
            System.out.println(te.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
	}
}
