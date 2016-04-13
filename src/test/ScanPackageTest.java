package test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;


public class ScanPackageTest {

	public static void main(String[] args) 
	{
		for (String s : getClassNamesFromPackage("agent.state"))
		{
			System.out.println(s);
		}
	}
	
	/**
	 * based on: 
	 * http://stackoverflow.com/questions/1456930/how-do-i-read-all-classes-from-a-java-package-in-the-classpath
	 * @param packageName
	 * @return
	 */
	public static ArrayList<String>getClassNamesFromPackage(String packageName) {
	    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    URL packageURL;
	    ArrayList<String> names = new ArrayList<String>();

	    packageName = packageName.replace(".", "/");
	    packageURL = classLoader.getResource(packageName);
	    URI uri = null;
	    
		try {
			uri = new URI(packageURL.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	    File folder = new File(uri.getPath());
        File[] contenuti = folder.listFiles();
        String entryName;
        for(File actual: contenuti)
        {
            entryName = actual.getName();
            entryName = entryName.substring(0, entryName.lastIndexOf('.'));
            names.add(entryName);
        }
	    return names;
	}
}
