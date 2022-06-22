package analysis.simple;

import agent.Agent;
import agent.Body;
import dataIO.CsvExport;
import dataIO.FileHandler;
import utility.ExtraMath;
import linearAlgebra.Matrix;
import referenceLibrary.AspectRef;
import shape.Shape;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Quick and dirty class to compare bacsim and idyno 2 output in terms of agent density per surface area
 */
public class Density {

    Double[] _lengths;
    Double[] _vox;

    CsvExport csv;
    double[][] _field;

    public Density(Double[] lengths, Double[] vox)
    {
        this._lengths = lengths;
        this._vox = vox;

        int x = (int) (_lengths[0]/vox[0]);
        int y = (int) (_lengths[1]/vox[1]);
        this._field = new double[x][y];
        csv = new CsvExport();
    }

    public void read(Collection<Agent> agents, Shape shape)
    {
        this._field = Matrix.zeros(this._field);
        for( Agent a : agents)
        {
            Body b = (Body) a.get(AspectRef.agentBody);
            double[] c = b.getCenter(shape);

            int[] coord = new int[3];
            int i = 0;
            for( double dim : c)
            {
                coord[i] = (int) Math.floor(dim/_vox[i]);
                i++;
            }
            _field[coord[0]][coord[1]] += a.getDouble("biomass@mass");
        }
    }

    public void toScreen()
    {
        System.out.println(Matrix.toString(this._field));
    }

    public void writeCSV(String prefix)
    {
        String out = Matrix.toString(this._field,"\n");

        csv.createFile(prefix);

        for( String line : out.split("\n"))
        {
            csv.writeLine(line);
        }

        csv.closeFile();

    }


    /**
     * Reading bacsim output to mass on grid
     * @param filename
     * @param cellDensity
     */
    public void read( String filename, Double cellDensity )
    {
        this._field = Matrix.zeros(this._field);

        FileHandler myFile = new FileHandler();
        ArrayList<String> lines = myFile.fopen( filename );

        for( String l : lines)
        {
            String[] words = l.split("\\t");

            double[] c = new double[] { Double.parseDouble(words[1]), Double.parseDouble(words[2]), Double.parseDouble(words[3]) };
            int[] coord = new int[3];
            int i = 0;
            for( double dim : c)
            {
                coord[i] = (int) Math.floor(dim/_vox[i]);
                i++;
            }

            _field[coord[0]][coord[1]] += ExtraMath.volumeOfASphere(Double.parseDouble(words[4]))*cellDensity;
        }
    }
}

