package sensitivityAnalysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import referenceLibrary.XmlRef;
import utility.Helper;
import dataIO.Log;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;

/**
 * \brief Creates multiple protocol file from an XML file defining the parameters with ranges.
 * 
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk), University of Nottingham, U.K.
 */
public class XmlCreate
{
	public static Document _sensitivityDoc;
	
	public static String _filePath;
	
	private static List<Element> _sensParams = new ArrayList<Element>();
	
	/**
	 * \brief Main function for creating the protocol files from sensitivity analysis 
	 * xml files
	 * 
	 */
	public static void main(String args[]) {
		System.out.println("Creating protocol files for Sensitivity Analysis");
		
		String xmlFilePath;
		if ( args == null || args.length == 0 || args[0] == null )
		{
			Scanner user_input = new Scanner( System.in );
			System.out.print("Enter protocol file path: ");
			xmlFilePath = user_input.next();
			user_input.close();
		}
		else
		{
			xmlFilePath = args[0];
		}
		
		_filePath = xmlFilePath;
		xmlLoad();
		xmlCopy();
	}
	
	/**
	 * \brief Checks if there is an argument provided and loads the input XML file 
	 * provided in the argument. If no argument provided, asks for file name.
	 */
	public static void xmlLoad() {
		if ( _filePath == null )
		{
			Log.printToScreen("No XML File given!", true);
			return;
		}
		Log.printToScreen("Reading XML file: " + _filePath + 
			"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
			+ "~~~~~~~~~~~~~~~~~~~~~~~~\n", false);
		
		_sensitivityDoc = XmlCreate.getDocument();
	}
	
	/**
	 * \brief Load the input XML file provided in the argument
	 */
	public static Document getDocument() {
		try {
			File fXmlFile = new File(_filePath);
			DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbF.newDocumentBuilder();
			Document doc;
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			return doc;
		} catch ( ParserConfigurationException | IOException e) {
			Log.printToScreen("Error while loading: " + _filePath + "\n"
					+ "error message: " + e.getMessage(), true);
			_filePath = Helper.obtainInput("", "Atempt to re-obtain document",
					false);
			return getDocument();
		} catch ( SAXException e ) {
			Log.printToScreen("Error while loading: " + _filePath + "\n"
				+ "error message: " + e.getMessage(), true);
			return null;
		}			
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
		// Parameters for Morris method
		int k = _sensParams.size();
		int p = 10;         // Number of levels. Ask in input file?
		int r = 15;         // Number of repetitions. From input?
		
		double[][] discreteUniformProbs = morris(k,p,r);
		
		double[] ones = Vector.onesDbl(r*(k+1));

		double[] inpMax = new double[k];
		double[] inpMin = new double[k];
		
		String[] rangeAspectNames = new String[k];
		String[][] ranges = new String[k][2];
		for (Element currAspect : _sensParams) {
			int idx = _sensParams.indexOf(currAspect);
			rangeAspectNames[idx] = currAspect.getAttribute(XmlRef.nameAttribute);
			String[] inRange = currAspect.getAttribute(XmlRef.rangeAttribute).split(",");
			inRange = checkRange(inRange);
			inpMax[idx] = Double.parseDouble(inRange[1]);
			inpMin[idx] = Double.parseDouble(inRange[0]);
			ranges[idx] = inRange.clone();
		}
		
		// Variable states: ones*inpMin + ones*(inpMax-inpMin)).*discreteUniformProbs
		double[][] states = Matrix.add(Vector.outerProduct(ones, inpMin),
				Matrix.elemTimes(Vector.outerProduct(ones, 
						Vector.minus(inpMax, inpMin)), discreteUniformProbs));
		
		for (int row = 0; row < r*(k+1); row++) {
			String suffix = "";
			for (Element currAspect : _sensParams) {
				int col = _sensParams.indexOf(currAspect);
				String attrToChange = currAspect.getAttribute(XmlRef.rangeForAttribute);
				Double curVal = states[row][col];
				currAspect.setAttribute(attrToChange, curVal.toString());
				suffix += currAspect.getAttribute(XmlRef.nameAttribute)+"_"+curVal;
			}
			newProtocolFile(suffix);
		}
	}
	
	/**
	 * \brief Checks if the provided range is in proper format
	 * @param valRange String array provided in XML
	 */
	public static String[] checkRange(String[] valRange) {
		if (valRange.length != 2 || Double.parseDouble(valRange[0]) >= Double.parseDouble(valRange[1])) {
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
	 * @param suffix A string value to be appended to the name of the protocol files,
	 * which provides the information about the changed attributes.
	 */
	public static void newProtocolFile(String suffix)
	{
		String[] fileDirs = _filePath.split("/");
		String fileName = fileDirs[fileDirs.length-1].split("\\.")[0];
		fileDirs = Arrays.copyOf(fileDirs, fileDirs.length-1);
		String fileString = String.join("/", fileDirs) + "/" 
				+ "SensitivityAnalysisFiles/"+ fileName + "_" + suffix + ".xml";
		try {
			Transformer _protocolFile = TransformerFactory.newInstance().newTransformer();
			_protocolFile.setOutputProperty(OutputKeys.INDENT, "yes");
			_protocolFile.setOutputProperty(OutputKeys.METHOD, "xml");
			_protocolFile.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			_protocolFile.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
			_protocolFile.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
			_protocolFile.transform(new DOMSource(_sensitivityDoc), 
					new StreamResult(new FileOutputStream(fileString)));
		}
		catch (TransformerException te) {
            System.out.println(te.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
	}
	
	/**
	 * \brief Morris method for creating sample distribution within provided range.
	 * @param k Integer value for number of input parameters to be changed
	 * @param p Integer value for the number of levels.
	 * @param r Integer value for the number of repetitions.
	 */
	public static double[][] morris(int k, int p, int r) {
		double delta = p/(2.0*(p-1));
		double[][] B = new double[k+1][k];
		double[][] J = Matrix.matrix(k+1, k, 1.0);
		for (int row = 0; row < k+1; row++) {
			for (int col = 0; col < k; col++) {
				if (row <= col)
					B[row][col] = 0;
				else
					B[row][col] = 1;
			}
		}		
		
		List<Double> xRange = new ArrayList<Double>();
		for (Double val = 0.0; val <= (1-delta); val=val+(1.0/(p-1.0))) {
			xRange.add(val);
		}
		int m = xRange.size();
		
		double X[][] = new double[r*(k+1)][k];
		
		for (int idx = 0; idx < r; idx++) {
			int[][] D = Matrix.identityInt(k);
			double[] probs = ThreadLocalRandom.current().doubles(k, 0, 1).toArray();
					//Vector.randomZeroOne(k); 
			
			int[] randInts = ThreadLocalRandom.current().ints(m, 0, k).toArray();
					//Vector.randomInts(m, 0, k);
			double[] xVals = new double[k];
			
			int[][] idMatrix = Matrix.identityInt(k);
			List<Integer> rowNums = new ArrayList<Integer>();
			int[][] permIdMatrix = new int[k][k];
			
			for (int row = 0; row < k; row++) {
				rowNums.add(row);
			}
			Collections.shuffle(rowNums);
			
			for (int row = 0; row < k; row++) {
				for (int col = 0; col < k; col++) {
				// step 1: make a diagonal matrix with integer values of 1 or -1 selected with equal probability.
					if (row == col && probs[row] < 0.5) {
						D[row][col] = -1;
					}
				}
				
				// step 2: select randomly from discrete uniform distribution with p levels for each input factors k 
				xVals[row] = xRange.get(randInts[row]);
				
				// step 3: randomly permuted identity matrix
				permIdMatrix[row] = idMatrix[rowNums.get(row)];
			}
			//first Expression: J[:,1]*xVals
			double[] Jcol = Matrix.getColumn(J, 0);
			double[][] firstExpr = Vector.outerProduct(Jcol, xVals);
			
			//second Expression: (delta/2)*( (2*B-J)*D + J ) 
			double[][] secondExpr = Matrix.times(Matrix.add(
					Matrix.times(Matrix.minus(Matrix.times(B, 2), J), Matrix.toDbl(D)), J), delta/2);
			
			// Bp = (first Expression + second Expression)*permIdMatrix
			double[][] Bp = Matrix.times(Matrix.add(firstExpr, secondExpr), Matrix.toDbl(permIdMatrix));
			for (int row = 0; row < k+1; row++) {
				for (int col = 0; col < k; col++) {
					X[row+(idx*(k+1))][col] = Bp[row][col];
				}
			}
		}
		return X;
	}
}
