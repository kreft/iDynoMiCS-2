package superpack;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import superpack.Cell;

public class XmlExport {
	static Writer twriter = null;
	XmlExport(String prefix, int threadNr, ArrayList<Cell> cells) {
		twriter = new Writer(prefix, threadNr, cells);
	}

static class Writer implements Runnable {
	static ArrayList<Cell> c = new ArrayList<Cell>();
	static int filewriterfilenr;
	static String filewriterfileprefix;
	
		Thread runner;
		public Writer() {
		}
		
		public Writer(String prefix, int threadNr, ArrayList<Cell> cells) {
			runner = new Thread(this, Integer.toString(threadNr)); // (1) Create a new thread.
			filewriterfilenr = threadNr;
			filewriterfileprefix = prefix;
			c = cells;
			if(par.hdd_thread_writes)
				runner.start();
			else
				writexml();
		}
		
		public void run() {
			writexml();
		}

		static void writexml() {
			XMLOutputFactory factory      = XMLOutputFactory.newInstance();
			 try {
			     XMLStreamWriter writer = factory.createXMLStreamWriter(
			     new FileWriter(par.output_location + filewriterfileprefix + helperFunctions.DigitFilenr(filewriterfilenr) + ".xml"));
			     
			     // header
			     writer.writeStartDocument();
			     
			     // state
			     writer.writeStartElement("state");
			     
			     // cells
			     for (int i = 0; i < c.size(); i++) {
				     writer.writeStartElement("Cell");
				     writer.writeAttribute("type", 		"" + c.get(i).type );
				     writer.writeAttribute("radius", 	"" + c.get(i).radius );
				     writer.writeAttribute("mass", 		"" + c.get(i).mass );
				     writer.writeAttribute("length", 	"" + c.get(i).length );
				     if (c.get(i).att != null) {
					     String attstr = "";
					     for (int j = 0; j < c.get(i).att.length; j++) {
					    	 attstr += c.get(i).att[j] + " ";
					     }
				    	 writer.writeAttribute("att", 	"" + attstr );
				     }
			    	 for (int j = 0; j < c.get(i).pos.length; j++) {
					     writer.writeStartElement("Point");
					     writer.writeAttribute("pos", c.get(i).pos[j].toString());
					     writer.writeAttribute("vel", c.get(i).vel[j].toString());
					     writer.writeAttribute("frc", c.get(i).frc[j].toString());
					     writer.writeEndElement();
			    	 }
				     writer.writeEndElement();
				     writer.writeDTD("\n");
			     }
			     
			     // close scene
			     writer.writeEndElement();
			     writer.writeEndDocument();

			     writer.flush();
			     writer.close();

			 } catch (XMLStreamException e) {
			     e.printStackTrace();
			 } catch (IOException e) {
			     e.printStackTrace();
			 }
		}
	}
}