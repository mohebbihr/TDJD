import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Results {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        String readsFile = args[0];
        String dbFile = args[1];
		String output = args[2];
		String hitsFile = args[3];

        buildBatch(readsFile, dbFile);
        runBlast();
        extractResults(hitsFile,readsFile);
        buildResults(hitsFile, dbFile, output);
    }

    public static void runBlast() throws InterruptedException, IOException {
        System.out.println("Blasting reads...");
	Process p2 = Runtime.getRuntime().exec("blastn -task megablast -db HumanGenome/blastdb/chrdb_all -window_masker_db HumanGenome/blastdb/chrdb_all_mask.counts -query ./readBatchNFP.txt -out ./blastOutNFP.txt");
	p2.waitFor();
        System.out.println("Blast query done.");
    }

    public static void extractResults(String hitsFile, String readsFile) throws FileNotFoundException, IOException {
        System.out.println("Extracting hits...");
        try {
            FileReader blastResFile = new FileReader("./blastOutNFP.txt");
            BufferedReader resBr = new BufferedReader(blastResFile);
            FileReader readFile = new FileReader(readsFile);
            BufferedReader readBr = new BufferedReader(readFile);
            String readResult = null;
            String readNr = null;
            String blastLine = null;
			int count =0;
            Map<Integer, String> blastMap = new HashMap<Integer, String>();

            while ((readNr = readBr.readLine()) != null) {
				blastLine = null;
                do {
                    blastLine = resBr.readLine();
                } while (blastLine !=null && blastLine.indexOf("Query= MP Read sequence " + readNr) == -1);

                readResult = null;
                do {
                    blastLine = resBr.readLine();
                    readResult += blastLine;
                } while (blastLine !=null && blastLine.indexOf("Effective search space used") == -1);
		
		if(readResult !=null && readResult.length() > 8){ // to avoid nullnull in resul
                	blastMap.put(new Integer(readNr), readResult);
		}
            }
	    System.out.println(" blastMap Size: " + blastMap.size());	    

            blastResFile.close();
            resBr.close();
            readFile.close();
            readBr.close();

            PrintWriter writer = new PrintWriter(hitsFile);
            String result;
            String chromosomes[] = {
                "chr1",
                "chr4",
                "chr5",
                "chr6",
                "chr7",
                "chr8",
                "chr9",
                "chr10",
                "chr11",
                "chr12",
                "chr13",
                "chr14",
                "chr15",
                "chr16",
                "chr17",
                "chr18",
                "chr19",
                "chr20",
                "chr21",
                "chr22",
                "chr23"};
            boolean foundOtherChrs = false;
            for (Map.Entry blastRes : blastMap.entrySet()) {
                result = blastRes.getValue().toString();
				if (result.indexOf("chr3") != -1 || result.indexOf("chr2") != -1) {
					// checking for chr20,21,22 ...
                    final char c = result.charAt(result.indexOf("chr2") + "chr2".length());
                    if (!Character.isDigit(c)) {
                        foundOtherChrs = false;
                        for (int i = 0; i < chromosomes.length; i++) {
                            if (result.indexOf(chromosomes[i]) != -1) {
                                foundOtherChrs = true;
                                break;
                            }
                        }
                        if (!foundOtherChrs) {
							count++;
							String chr_target = "";
							if(result.indexOf("chr3") != -1)
								chr_target = "chr3";
							else if(result.indexOf("chr2") != -1)
								chr_target = "chr2";
                            writer.println("chromosome: " + chr_target +"\t,blast Result: " + blastRes.getValue().toString() + "\t,blast Result key: " + blastRes.getKey().toString());
                        }
                    }
                }
            }
            writer.close();
	    System.out.println("Number of hits: " + count);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void buildBatch(String readsFile, String dbFile) throws FileNotFoundException, IOException, InterruptedException {
        String scan;
        try {
            FileReader file = new FileReader(readsFile);
            BufferedReader br = new BufferedReader(file);
            FileReader dbfile = new FileReader(dbFile);
            BufferedReader dbr = new BufferedReader(dbfile);
            String readsBatchFile = "./readBatchNFP.txt";
            String dbline = null, echo;
            String[] tabsplit;
            int seqLineNum, i=0, count = 0;
            PrintWriter writer = new PrintWriter(readsBatchFile);
            System.out.println("Building reads batch file...");
            while ((scan = br.readLine()) != null) {
				count ++;
                echo = scan;
				seqLineNum = Integer.parseInt(scan);
                while((dbline = dbr.readLine()) != null && i < ((seqLineNum - 1)*2) ) i++;
                if(dbline !=null){
					tabsplit = dbline.split("\\t");
                	writer.println(">MP Read sequence " + scan);
					writer.println(tabsplit[6]);
				}
            }

            file.close();
            br.close();
            writer.close();
            dbfile.close();
            dbr.close();
            System.out.println("Batch of reads built.");
			System.out.println("Number of read records: " + count);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void buildResults(String hitsFilename, String dbFilename, String output) throws FileNotFoundException, IOException, InterruptedException {
        String scan;
        try {
            FileReader file = new FileReader(hitsFilename);
            BufferedReader br = new BufferedReader(file);
            String dbFileName = dbFilename;
            String resultsFile = output;
	    FileReader dbfile = new FileReader(dbFilename);
            BufferedReader dbr = new BufferedReader(dbfile);
	    String dbline = null;
            String[] tabsplit;
            int seqLineNum, i=0;
            PrintWriter writer = new PrintWriter(resultsFile);
	    String echo;
            while ((scan = br.readLine()) != null) {
                echo = scan;
                seqLineNum = Integer.parseInt(scan);
                while((dbline = dbr.readLine()) != null && i < ((seqLineNum - 1)*2) ) i++;
                if (dbline !=null){
			tabsplit = dbline.split("\\t");
                	echo = echo + "  " + tabsplit[6];
		}
		writer.println(echo);
		
		
 		/*echo = "printf '" + scan + "  ' >> " + resultsFile;
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", echo);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                p.waitFor();

                ProcessBuilder builder = new ProcessBuilder("bash", "-c", "awk \'{if ($2==" + scan + ") print $7}\' " + dbFileName + " | head -1 >> " + resultsFile);
                builder.redirectErrorStream(true);
                Process p1 = builder.start();
                p1.waitFor();*/
            }

            file.close();
            br.close();
            writer.close();
            System.out.println("Output is written to" + output);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
