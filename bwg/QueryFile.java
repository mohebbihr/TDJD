import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;


public class QueryFile extends WindowsFile{
	
	public QueryFile (String inputPath, String OutputFilePath, String OutputDbPath, String discordantPairs, short windowSize, short referenceChr, short min_mapq, boolean isReference, short permuteLength) throws Exception{
		
		super(inputPath,OutputFilePath,OutputDbPath,discordantPairs,windowSize,referenceChr,min_mapq,isReference,permuteLength);
	}
	
	@Override public void CreateWindows(String inputPath, String discordantPairs, short windowSize) throws IOException{
		
		long recordId = 0L;
		long count = 0;
				
		File inputFile = new File(inputPath);
		File inputIndex = new File(inputPath+".bai");
		final SAMFileReader inputBam = new SAMFileReader(inputFile,inputIndex);
		BufferedReader reader = new BufferedReader(new FileReader(discordantPairs));
		String line = null;
		String[] parts = null;
		try {
			if((line = reader.readLine()) != null) 
			{    	
				parts = line.split(" ");
				for (final SAMRecord bamRecord : inputBam)
				{
					String readBuffer = bamRecord.getReadString();
					
					/*********************Hard wire to chr2 and chr3*********************/
					if (bamRecord.getReferenceName().compareTo("chr2")<0)
						continue;
					if (bamRecord.getReferenceName().compareTo("chr3")>0)
						break;
					/********************************************************************/
									
					if (((bamRecord.getAlignmentStart() < Long.parseLong(parts[1])-550) && (bamRecord.getReferenceName().compareTo(parts[0])==0)) || (bamRecord.getReferenceName().compareTo(parts[0])<0))
						continue;
					while (((bamRecord.getAlignmentStart() >= (Long.parseLong(parts[1])+550)) && (bamRecord.getReferenceName().compareTo(parts[0])==0)) || (bamRecord.getReferenceName().compareTo(parts[0])>0)){
						if((line = reader.readLine()) != null) 
							parts = line.split(" ");
						else
							break;
					}
					if ((((bamRecord.getAlignmentStart()) > Long.parseLong(parts[1]) && (bamRecord.getAlignmentStart() < (Long.parseLong(parts[1])+500))) || ((bamRecord.getAlignmentStart()) < Long.parseLong(parts[1]) && (bamRecord.getAlignmentStart() > (Long.parseLong(parts[1])-550)))) && (bamRecord.getReferenceName().compareTo(parts[0])==0))
					{
						recordId++;
						for (int i = 0; i <= (bamRecord.getReadLength()-windowSize); i++) {
							short readStartPos = (short) (i+1);
							String window = "";
						    short posWindow = 0;
							boolean allNwindow = true;
							for (int j=i; j< i+windowSize;j++){
								char currentChar = Character.toUpperCase((char) readBuffer.charAt(j));
								// we replace ambiguous codes with its equevalent
								// ref: http://www.boekhoff.info/?pid=data&dat=fasta-codes
								switch(currentChar){
									case 'Y' :
										currentChar = 'T';
										break;
									case 'R':
										currentChar = 'G';
										break;
									case 'K':
										currentChar = 'T';
										break;
									case 'M':
										currentChar = 'A';
										break;
									case 'S':
										currentChar = 'C';
										break;
									case 'W':
										currentChar = 'A';
										break;
									case 'B':
										currentChar = 'C';
										break;
									case 'V':
										currentChar = 'G';
										break;
									case 'H': 
										currentChar = 'T';
										break;
								}
								if( currentChar != 'N' ){
									
								if (currentChar != 'N')
									allNwindow = false;
								window = window + currentChar;
								posWindow++;
								if (posWindow == windowSize){
									if (! allNwindow)
										if(i == 0 || i == bamRecord.getReadLength()-windowSize)//Catch only first and last window
											//SeqToWindow(window, windowSize, bamRecord.getAlignmentStart(), readStartPos, recordId, bamRecord.getReadName(), bamRecord.getReferenceName(),bamRecord.getReadString(),isReference);
											SeqToBinaryWindow(window, windowSize, bamRecord.getAlignmentStart(), readStartPos, recordId, bamRecord.getReadName(), bamRecord.getReferenceName(),bamRecord.getReadString(),isReference);											
											// convert sequence to the binary window									
								}
								}
							}
						}
						if ((recordId % 1000)==0){
							//printerFile.flush();
							fos.flush();
							printerDatabase.flush();
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//printerFile.flush();
		fos.flush();
		printerDatabase.flush();
		inputBam.close();
		reader.close();
	}
}
