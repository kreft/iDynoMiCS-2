package dataIO;

import java.text.SimpleDateFormat;
import java.util.Date;

import idynomics.Idynomics;

public class Report {
	
	/**
	 * Full date format.
	 */
	private static SimpleDateFormat _ft = 
						new SimpleDateFormat("[yyyy.MM.dd] ");

	protected FileHandler _reportFile = new FileHandler();

	public void createCustomFile(String fileName)
	{
		String fileString = Idynomics.global.outputLocation + "/" 
				+ fileName + ".rmd";
		_reportFile.fnew(fileString);
		_reportFile.write(
				"---\n" +
				"title: \"" + Idynomics.global.simulationName + " simulation report\"\n" +
				"date: \""  + _ft.format(new Date()) + "\"\n" +
				"output:\n" +
				"  html_document: default\n" +
				"---\n");
		
		
	}

	public void closeFile()
	{
		_reportFile.write("\n");
		_reportFile.fclose();
	}
	
	public void writeReport()
	{
		_reportFile.write("#Model structure \n\n");
		
		_reportFile.write("##Species library \n\n");
		String fileName = "speciesDiagram";
		Diagram diagSpec = new Diagram();
		diagSpec.createCustomFile(fileName);
		diagSpec.speciesDiagram();
		diagSpec.closeFile();
		
		_reportFile.write(
				"```{r, echo=FALSE}\n" +
					"packageList <- c(\"DiagrammeR\")\n" +
					"packagesToInstall <- packageList[!(packageList %in% " +
					"installed.packages()[,\"Package\"])]\n" +
					"if(length(packagesToInstall))\n" +
					"\t install.packages(packagesToInstall)\n" +
					"if (!any(search() == \"package:DiagrammeR\"))\n" +
					"\t library(DiagrammeR)\n" +
				"grViz(\"" + fileName + ".dot\")\n" +
				"```\n\n");
		

		_reportFile.write("##Chemical interactions \n\n");
		
		for( String comp : Idynomics.simulator.getCompartmentNames() )
		{
			fileName = "reactionDiagram_" + comp;
			_reportFile.write("###" + comp + "\n\n");
			Diagram diagReac = new Diagram();
			diagReac.createCustomFile(fileName);
			diagReac.reactionDiagram( Idynomics.simulator.getCompartment(comp));
			diagReac.closeFile();
			
			_reportFile.write(
					"```{r, echo=FALSE}\n" +
					"library(DiagrammeR)\n" +
					"grViz(\"" + fileName + ".dot\")\n" +
					"```\n\n");
		}
				
		_reportFile.write("##microbial and chemical species \n\n");
		
		FileHandler fileHandler = new FileHandler();
		if ( fileHandler.doesFileExist(Idynomics.global.outputLocation + "/" +
				"data.csv"))
		{
			_reportFile.write(
			"```{r, echo=FALSE}\n" +
			"dataFile <- read.csv(\"data.csv\", header = TRUE)\n" +
			"xcol <- ncol( dataFile )\n" +
			"xrow <- nrow( dataFile )\n" +
			"x <- seq(1, xrow, by = 1)\n" +
			"plot( x, dataFile[,1] , type = \"n\",\n" +
			"      ylim = c(0, max( dataFile )), xlim = c(0, xrow*2.0),\n" +
			"      xlab=\"iteration\", ylab=\"value\")\n" +
			"for( i in 1:xcol ) {\n" +
			"  lines(x, dataFile[,i], type=\"l\", col=palette()[i]) }\n" +
			"text( x=rep(max(x)+2, 2), y=dataFile[xrow,], pos=4, labels=colnames(dataFile))\n" +
			"```\n\n"
			);
			
			_reportFile.write(
			"```{r, echo=FALSE}\n" +
			"dataFile <- read.csv(\"data.csv\", header = TRUE)\n" +
			"xcol <- ncol( dataFile )\n" +
			"xrow <- nrow( dataFile )\n" +
			"x <- seq(1, xrow, by = 1)\n" +
			"for( i in 1:xcol ) {\n" +
			"	plot( x, dataFile[,i] , type = \"l\",\n" +
			"      xlab=\"iteration\", ylab=\"value\")\n" +
			"	title(main = colnames(dataFile)[i])" +
			"}\n" +
			"```\n\n"
			);
		}
		
	}
}