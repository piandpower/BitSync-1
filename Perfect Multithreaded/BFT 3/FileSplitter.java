import java.io.*;

public class FileSplitter
    
	{
	static String filePath;
	
	/** the maximum size of each file "chunk" generated, in bytes */
	public static long chunkSize = 100 * 1024;
	
		
	/**
	  * split the file specified by filename into pieces, each of size
	  * chunkSize except for the last one, which may be smaller
	  */
	
	public static void split(String filename) throws FileNotFoundException, IOException
		{
		// open the file
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
		
		// get the file length
		File f = new File(filename);
		long fileSize = f.length();
		
		// loop for each full chunk
		int subfile;
		for (subfile = 0; subfile < fileSize / chunkSize; subfile++)
			{
			// open the output file
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + ".chunk" + subfile));
			
			// write the right amount of bytes
			for (int currentByte = 0; currentByte < chunkSize; currentByte++)
				{
				// load one byte from the input file and write it to the output file
				out.write(in.read());
				}
				
			// close the file
			out.close();
			}
		
		// loop for the last chunk (which may be smaller than the chunk size)
		if (fileSize != chunkSize * (subfile - 1))
			{
			// open the output file
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + ".chunk" + subfile));
			
			// write the rest of the file
			int b;
			while ((b = in.read()) != -1)
				out.write(b);
				
			// close the file
			out.close();			
			}
		
		// close the file
		in.close();
		}
		
	/**
	  * list all files in the directory specified by the baseFilename
	  * , find out how many parts there are, and then concatenate them
	  * together to create a file with the filename <I>baseFilename</I>.
	  */
	public static void join(String baseFilename) throws IOException
		{
		int numberParts = getNumberParts(baseFilename);

		// now, assume that the files are correctly numbered in order (that some joker didn't delete any part)
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(baseFilename));
		for (int part = 0; part < numberParts; part++)
			{
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(baseFilename + ".chunk" + part));

			int b;
			while ( (b = in.read()) != -1 )
				out.write(b);

			in.close();
			}
		out.close();
		}
	
	/**
	  * find out how many chunks there are to the base filename
	  */
	private static int getNumberParts(String baseFilename) throws IOException
		{
		// list all files in the same directory
		File directory = new File(baseFilename).getAbsoluteFile().getParentFile();
		final String justFilename = new File(baseFilename).getName();
		String[] matchingFiles = directory.list(new FilenameFilter()
			{
			public boolean accept(File dir, String name)
				{
				return name.startsWith(justFilename) && name.substring(justFilename.length()).matches("^\\.chunk\\d+$");
				}
			});
		return matchingFiles.length;
		}
	}