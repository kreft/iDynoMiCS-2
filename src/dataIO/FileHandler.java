package dataIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class FileHandler {
	BufferedWriter output;
	int filewriterfilenr;
	
	private boolean dirMake(String dir)
	{
		File base = new File(dir);
		boolean result = false;
		if (!base.exists()) {
			try{
				base.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		}
		return result;
	}
	
	public boolean dir(String dir)
	{
		return dir(dir, 0);
	}
	
	private boolean dir(String dir, int min)
	{
		String[] folders = dir.split("/");
		String path = "";
		boolean result = false;
		for (int i = 0; i < folders.length - min; i++)
		{
			path = path + folders[i] + "/";
			result = dirMake(path);
		}
		// next line for testing
		System.out.println("Writing output to: " + path);
		return result;
	}
	
	public void fopen(String file)
	{
		//TODO
	}
	
	public void fnew(String file)
	{
		if(file.split("/").length > 1)
			dir(file,1);
		try {
			File f = new File(file);
			f.delete();
			java.io.FileWriter fstream;
			fstream = new java.io.FileWriter(f, true);
			output = new BufferedWriter(fstream);
		} catch (IOException e) {
			// catch
			e.printStackTrace();
		}
	}
	
	public void write(String line)
	{
		try {
			output.write(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void fclose() {
		try {
			output.flush();
			output.close();
		} catch (IOException e) {
			// catch
			e.printStackTrace();
		}
	}

}
