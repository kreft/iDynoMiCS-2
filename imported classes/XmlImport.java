package superpack;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlImport {
	XmlImport(String inputFile, ArrayList<Cell> cells) {
		try {
			File fXmlFile = new File(inputFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Cell");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					// obtain properties
					Element eElement = (Element) nNode;
					int type	= Integer.parseInt(eElement.getAttribute("type"));
					double radius = Double.parseDouble(eElement.getAttribute("radius"));
					double mass = Double.parseDouble(eElement.getAttribute("mass"));
					double length = Double.parseDouble(eElement.getAttribute("length"));
					
					// obtain attachments
					String string = eElement.getAttribute("att");
					int[] att = null;
					if (eElement.getAttribute("att") != "") {
						String[] splits = string.split(" ");
						att = new int[splits.length];
						for (int i = 0; i < splits.length; i++) {
							att[i] = Integer.parseInt(splits[i]); 
						}
					}
					
					// obtain array's of DoubleVectors
					int l = eElement.getChildNodes().getLength();
					DoubleVector[] pos = new DoubleVector[l];
					DoubleVector[] vel = new DoubleVector[l];
					DoubleVector[] frc = new DoubleVector[l];
					
					for(int i = 0; i < l; i++) {
						pos[i] = new DoubleVector(eElement.getChildNodes().item(i).getAttributes().getNamedItem("pos").getNodeValue());
						vel[i] = new DoubleVector(eElement.getChildNodes().item(i).getAttributes().getNamedItem("vel").getNodeValue());
						frc[i] = new DoubleVector(eElement.getChildNodes().item(i).getAttributes().getNamedItem("frc").getNodeValue());
					}
					// type import is untested feature
					cells.add(new Cell(radius, mass, length, pos, vel, frc, att, type));

						
				}
			}
			System.out.println("Finished loading: " + doc.getBaseURI());
			System.out.println("-------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
