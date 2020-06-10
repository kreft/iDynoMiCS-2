package dataIO;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FolderOperations {
	
	public static final String[] extensions = new String[] { "exi", "xml" };
	
	/**
	 * \brief Check whether passed files contain at least 1 folder
	 * @param files
	 * @return
	 */
	public static boolean includesfolders( File ... files )
	{
		for( File f : files )
	    	if ( f.isDirectory() ) 
	            return true;
		return false;
	}
	
	/**
	 * \brief return all files from passed file and folder list but do not
	 * include sub-folders
	 * 
	 * @param subfolders
	 * @param files
	 * @return
	 */
	public static List<File> getFiles( File ... files  )
	{
		LinkedList<File> out = new LinkedList<File>();
		for( File f : files )
		{
	    	if ( f.isFile() ) 
	            out.add( f );
	    	else
	    		for (File fileEntry : f.listFiles())
	    			out.addAll( getFiles( false, fileEntry ) );
		}
	    return out;
	}
	
	/**
	 * \brief return all files and optionally all files in all sub-folders
	 * @param subfolders
	 * @param files
	 * @return
	 */
	public static List<File> getFiles( boolean subfolders, File ... files  )
	{
		LinkedList<File> out = new LinkedList<File>();
		for( File f : files )
		{
	    	if ( f.isFile() ) 
	            out.add( f );
	    	else if ( subfolders ) 
	    		for (File fileEntry : f.listFiles())
	    			out.addAll( getFiles( subfolders, fileEntry ) );
		}
	    return out;
	}
	
	/**
	 * \brief filter all files with common iDynoMiCS extensions
	 * @param files
	 * @return
	 */
	public static List<File> filterFiles( File ... files )
	{
		return filterFiles( extensions, files);
	}
	
	/**
	 * \brief filter all files with given extensions
	 * @param extension
	 * @param files
	 * @return
	 */
	public static List<File> filterFiles( String[] extension, File ... files )
	{
		LinkedList<File> out = new LinkedList<File>();
		for( File file : files )
			for( String s : extension )
				if( file.getName().toLowerCase().contains( s ) )
				{
					out.add( file );
					break;
				}
		return out;
	}
}
