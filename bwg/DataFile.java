import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class DataFile extends WindowsFile {
	
	static final int SIZE = 4 * 1024; 
	static byte[] buffer = new byte[SIZE];
	static byte[] printBuffer = new byte[SIZE];
	long wgChunkSize = 20000000; 
	int fileCount = 0;		
	int jumpStep = 3; // if the jumpStep is k, we need to put k-1 here

	public DataFile(String inputPath,String OutputFilePath,String OutputDbPath,String discordantPairs,short windowSize,short referenceChr,short min_mapq,boolean isReference,short permuteLength, long wgChunkSize, int jumpStep) throws Exception{
		
		super(inputPath,OutputFilePath,OutputDbPath,discordantPairs,windowSize,referenceChr,min_mapq,isReference,permuteLength);
		this.wgChunkSize = wgChunkSize;		
		this.jumpStep = jumpStep - 1;
	}
	
	@Override public void run(){
		File outputFile = new File(OutputFilePath + "_" + fileCount);
		File outputDatabase = new File(OutputDbPath);
		try {			
			fos = new FileOutputStream(outputFile);
			printerDatabase = new PrintWriter(outputDatabase);
			CreateWindows(inputPath,discordantPairs,windowSize);
			fos.close();
		printerDatabase.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Files successfully created!!");
	}
	
	@Override public void CreateWindows(String inputPath,String discordantPairs,short windowSize) throws Exception{
		
		long pos =0L;
		long referenceStartPos = 1L;
		short posWindow = 0;
		String window = "";
		char currentChar;
		boolean allNwindow = true;
		long fileLen = 0L;
		boolean posWindowChange = false;
				
		RandomAccessFile memoryMappedFile = new RandomAccessFile(inputPath, "r");
		FileChannel channel = memoryMappedFile.getChannel();
				
		do{
			long read = Math.min(Integer.MAX_VALUE, channel.size() - pos);
			MappedByteBuffer mbb = channel.map(FileChannel.MapMode.READ_ONLY, pos, read);
			int nGet;
			int leftlen = 0;
			while (mbb.hasRemaining()) {
				nGet = Math.min(mbb.remaining(), SIZE);
				mbb.get(buffer, 0, nGet); 
				int posWindowBuffer = 0;
				posWindowChange = false;
				fileLen += nGet;
				for (int i = 0; i < nGet; i++) {
					currentChar = Character.toUpperCase((char) buffer[i]);
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
                                        	

					if (currentChar == '>'){
						while (currentChar != '\n'){
							i++;
							currentChar = (char) buffer[i];
						}
						continue;
					}
					else	
					if (currentChar != -1 && currentChar != '\n'){
						
						if (currentChar != 'N')
							allNwindow = false;
						if (posWindowBuffer == 0 && posWindowChange == false){
							if(window != "")
	                                                        leftlen = window.length();
							else
								leftlen = 0;		
							posWindowBuffer = i;
							posWindowChange = true;
						}
							
						window = window + currentChar;
						posWindow++;
						if (posWindow == windowSize){
							if(nValuesPrinted == (fileCount + 1) * wgChunkSize){
								// create a new output file and write content to it
								fos.flush();
								fos.close();
								fileCount ++;
								File outputFile = new File(OutputFilePath + "_" + fileCount);
								try {									
									fos = new FileOutputStream(outputFile);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
							}
							if (! allNwindow)
								//SeqToWindow(window,windowSize,referenceStartPos,(short) 0,0,"R", String.valueOf(referenceChr),"",isReference);
								SeqToBinaryWindow(window,windowSize,referenceStartPos,(short) 0,0,"R", String.valueOf(referenceChr),"",isReference);
							window="";
							referenceStartPos += 1;
							posWindow=0;
							if(i < windowSize - 1 )
								i = (posWindowBuffer + windowSize - leftlen - 1);
							else							
								i= (posWindowBuffer + jumpStep);
							posWindowBuffer = 0;		
							posWindowChange = false;					
							allNwindow = true;
							nValuesPrinted ++;
						}
					}
					
		      		}
				}
				//printerFile.flush();
				fos.flush();
				printerDatabase.flush();
			}
			pos += read;
		}while (pos < channel.size());
		channel.close();
		memoryMappedFile.close();		
		System.out.println("Windows printed: " + nWindowsPrinted);
		System.out.println("Values printed: " + nValuesPrinted);
		System.out.println("fileLen: " + fileLen);
	}
}

