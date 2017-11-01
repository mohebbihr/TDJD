/*
 * TDJD - Translocation Detector using computing jaccard distance.
 * This program implements part of an algorithm to detect translocation in genome using a window technique that splits
 * the reads in pre-determined size windows and uses jaccard distance to find the nearest neighboor.  
 * Version 1.0 by Hamidreza Mohebbi
 * October 2016
 */
public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		int i = 0, j;
		String input="", output="", outputDB="", discordantPairs="";
		
		//Default parameters:
		short windowSize=30; 
		short referenceChr=2;
		short permuteLength = 4;
		Boolean isReference=false;
		short min_mapq=32;		
		long wgChunkSize = 20000000;
		int jumpStep = 7; // we jump k step for creating reference finger print		

		while (i < args.length && args[i].startsWith("-")) {
		    	String arg = args[i++];
	    		for (j = 1; j < arg.length(); j++) {
				char flag = arg.charAt(j);
				switch (flag) {
				case 'i':
				input= args[i++];
				break;
				case 'o':
				output= args[i++];
				outputDB= output+"_DB";	
				break;
				case 'd':
				discordantPairs= args[i++];
				break;
				case 'w':
				windowSize= Short.parseShort(args[i++]);
				break;
				case 'g':
				permuteLength= Short.parseShort(args[i++]);
				break;
				case 'c':
				referenceChr= Short.parseShort(args[i++]);
				isReference = true;							
				break;
				case 'k':
				jumpStep = Integer.parseInt(args[i++]);
				break;
				case 's':
				wgChunkSize= Long.parseLong(args[i++]);				
				break;
				default:
				System.err.println("ParseCmdLine: illegal option " + flag + " Usage:< -i input_file, -o output_file -d discordant_reads_file -w windows_length -g gram_length -c ref_chromosome -k jumpStep -s wgChunkSize>");
				break;
				}
		        }
		}
		
		//Windows generation	
		System.out.println("Started at: "+ System.nanoTime() + "(nanotime)");
		System.out.println("Input: " + input + " , output: " + output + " , c: "+ referenceChr);
		Utils.permute("ACGNT".toCharArray(), permuteLength);
		//Utils.permute_wc("ACGT".toCharArray(), permuteLength);
		//Utils.builLookupTbl();
						
		if (isReference){
			DataFile df = new DataFile(input,output,outputDB,discordantPairs,windowSize,referenceChr,min_mapq,isReference,permuteLength, wgChunkSize, jumpStep);
			df.run();
		}else{
			QueryFile qf = new QueryFile(input,output,outputDB,discordantPairs,windowSize,referenceChr,min_mapq, isReference,permuteLength);
			qf.run();
		}
			
		System.out.println("Finished at: "+ System.nanoTime());
	}
}
