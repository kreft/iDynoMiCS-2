package debugTools;

import dataIO.CsvExport;
import linearAlgebra.Matrix;

import java.util.HashMap;
import java.util.Map;

public class QuickCSV {


    public static Map<String,QuickCSV> writers =
            new HashMap<String,QuickCSV>();

    static int numberOfWriters = 0;

    private CsvExport exporter = new CsvExport();

    private String _name = "QuickCSV writer";

    private boolean _verbose = false;

    /**
     * create numbered writer
     */
    public QuickCSV()
    {

        QuickCSV.numberOfWriters++;
    }

    /**
     * create named writer
     * @param name
     */
    public QuickCSV(String name)
    {
        this._name = name;
        QuickCSV.numberOfWriters++;
    }

    /**
     * add unnamed writer
     * @return
     */
    public static int add()
    {
        int temp = numberOfWriters;
        writers.put(String.valueOf( temp ), new QuickCSV() );
        return temp;
    }

    public static String add(String name)
    {
        writers.put(name , new QuickCSV(name));
        return name;
    }

    public static QuickCSV select(int i)
    {
        return writers.get(String.valueOf(i));
    }


    public static QuickCSV select(String name)
    {
        return writers.get(name);
    }

    public void write(double[][] matrix)
    {
        this.write( Matrix.toString( matrix ) );
    }

    public void write(String content)
    {
        this.exporter.createFile( this._name );
        this.exporter.writeLine( content );
        this.exporter.closeFile();
    }

    public static void write(String name, double[][] matrix)
    {
        //lot's of other programs don't work nice with , and ; delimiters mixed
        write( name, Matrix.toString( matrix , "\n") );
    }

    public static void write(String name, String content)
    {
        if ( !writers.containsKey(name) )
        {
            add(name);
        }
        writers.get(name).write( content );
    }

    public void verbose(boolean verbose)
    {
        this._verbose = verbose;
    }

}
