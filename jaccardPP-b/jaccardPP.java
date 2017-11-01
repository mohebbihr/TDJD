
/*MapReduce application 
*/
import java.io.File;
import java.io.IOException;

public class jaccardPP {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	
	static int readSize = 100;
	static int windowSize = 32;
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
				
		if (args.length != 4){
			System.err.println("Usage: <input directory> <output directory> <number of query points> <number of data points>");
		}
		
		int nWindowsRead = 2;//Changed here because we are getting only the first and last window of the read
		int nQueryPoints = Integer.parseInt(args[2]);
		int nDataPoints = Integer.parseInt(args[3]);
		
		System.out.println();
		System.out.println("**Starting MPPP**");
		System.out.println();
		System.out.println("Pre-Defined max number of query points in input file: "+ String.valueOf(nQueryPoints));
		System.out.println("Pre-Defined max number of data points in input file: "+ String.valueOf(nDataPoints));
		System.out.println();
		
		File folder = new File(args[0]);
		File parent = folder.getParentFile();
		File[] listOfFiles = folder.listFiles();
		System.out.println("Number of input files: " + listOfFiles.length); 
		for (int i = 0; i < listOfFiles.length; i++) 
	{
			if ((listOfFiles[i].isFile()) && !listOfFiles[i].getName().endsWith("~") && listOfFiles[i].getName().length() > 2)
			{
				String inFile = folder.getAbsolutePath()+ "/" + listOfFiles[i].getName();
				int posRoot = inFile.indexOf("./");
				int dataFileId = 1;
				if (posRoot != -1)
					inFile = inFile.substring(0,posRoot)+inFile.substring(posRoot+2,inFile.length());				
				String outFile = args[1]+ listOfFiles[i].getName();
				posRoot = outFile.indexOf("./");
				if (posRoot != -1)
					outFile = outFile.substring(0,posRoot)+outFile.substring(posRoot+2,outFile.length());
				int pos = listOfFiles[i].getName().indexOf('_');
				//System.out.println("list of file: " + listOfFiles[i].getName() + " ,inFile: " + inFile + ", outFile: "+ outFile);
				if(listOfFiles[i].getName().indexOf(".bam") != -1)
					dataFileId = Integer.parseInt(listOfFiles[i].getName().substring(pos+1, listOfFiles[i].getName().length() -4 ));
				else
					dataFileId = Integer.parseInt(listOfFiles[i].getName().substring(pos+1, listOfFiles[i].getName().length() ));
				
				int posChr = listOfFiles[i].getName().indexOf("chr");
				String referenceChr = listOfFiles[i].getName().substring(posChr+3,pos);
				//System.out.println("Pre-processing "+ referenceChr + "_" + String.valueOf(dataFileId));

				
				System.out.println("Input File: "+ inFile);
				System.out.println("Pre-processing " + inFile + " ..");
				jaccardPPAction mp_pp = new jaccardPPAction(inFile,outFile,/*queryFileId,*/dataFileId,nWindowsRead,referenceChr,nQueryPoints,nDataPoints);
				System.out.println("Output File: "+ outFile);
				System.out.println("Pre-processing completed.");
				System.out.println();
			 }
		}
	}
}
