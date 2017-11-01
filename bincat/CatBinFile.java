import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class CatBinFile {
	
	String input1 = "", input2 = "", output = "";	
	FileOutputStream fos = null; // output stream for writing to third file
	long fileLen1 = 0L;
	long fileLen2 = 0L;
	FileInputStream fis1 = null; // reading from first binary file
	DataInputStream dis1 = null;
	FileInputStream fis2 = null; // reading from second binary file
	DataInputStream dis2 = null;	
	
	public CatBinFile(String inputPath1, String inputPath2, String OutputFilePath){
		input1 = inputPath1;
		input2 = inputPath2;
		output = OutputFilePath;
	}
	
	public void cat() throws Exception{
				
		try {			
			File outputFile = new File(output);
			fos = new FileOutputStream(outputFile);
			
			File inputFile1 = new File(input1);
			fileLen1 = inputFile1.length();
			fis1 = new FileInputStream(inputFile1);
			dis1 = new DataInputStream(fis1);
			
			File inputFile2 = new File(input2);
			fileLen2 = inputFile2.length();
			fis2 = new FileInputStream(inputFile2);
			dis2 = new DataInputStream(fis2);
			
			combineFiles();
			if(fos != null)
				fos.close();
			if(fis1 != null)
				fis1.close();
			if(dis1 != null)
				dis1.close();
			if(fis2 != null)
				fis2.close();
			if(dis2 != null)
				dis2.close();
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Files"+input1+", "+input2+" are successfully combined!!");
	}
	
	private void combineFiles() throws Exception{
		long readOffset = 0L;
		int count = 0;
		while( readOffset < fileLen1 ){ // reading from file1 and writing to output						
			count = fis1.available();
			byte[] byteArr = new byte[count];
			dis1.read(byteArr, readOffset, count);
			readOffset += count; // 16 bytes has been readen
			fos.write(byteArr); // writing to output file.
			fos.flush();
		}
		readOffset = 0L;
		while( readOffset < fileLen2 ){ // reading from file2 and writing to output						
			count = fis2.available();
			byte[] byteArr = new byte[count];
			dis2.read(byteArr, readOffset, count);
			readOffset += count; // 16 bytes has been readen
			fos.write(byteArr); // writing to output file.
			fos.flush();
		}
	}
}

