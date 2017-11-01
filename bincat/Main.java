/*
 * This program combines two binary files and create another binary file. 
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
		String input1="", input2 = "", output="";
			
		while (i < args.length && args[i].startsWith("-")) {
		    	String arg = args[i++];
	    		for (j = 1; j < arg.length(); j++) {
				char flag = arg.charAt(j);
				switch (flag) {
				case 'i':
				input1= args[i++];
				break;
				case 'j':
				input2= args[i++];				
				break;
				case 'o':
				output= args[i++];
				break;				
				default:
				System.err.println("ParseCmdLine: illegal option " + flag + " Usage:< -i input_file1, -j input_file2, -o output_file >");
				break;
				}
		        }
		}
									
		CatBinFile df = new CatBinFile(input1, input2,output);
		df.cat();
			
		System.out.println("Finished at: "+ System.nanoTime());
	}
}
