import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;


public class jaccardPPAction {

	//BufferedReader br;
	PrintWriter pr;
	File inputFile = null;
	FileInputStream fis = null; // reading from binary file
	DataInputStream dis = null;
	//FileOutputStream fos = null; // we used only binary files for input, if we use binary file for output, we need to use binary files for hadoop which is difficult
	//BufferedOutputStream outBuff = null;
	//DataOutputStream dos = null;
	
	long fileLen =0L;
	int queryFileId;
	int dataFileId;
	int nWindowsRead;
	String referenceChr;
	int nQPoints;
	int nDPoints;
		
	public jaccardPPAction(String inFile,String outFile/*,int queryFileId*/,int dataFileId,int nWindowsRead,String referenceChr, int nQPoints, int nDPoints) throws IOException{
			
			//br = new BufferedReader(new FileReader(inFile));
			pr = new PrintWriter(new File(outFile));
			inputFile = new File(inFile);
			fileLen = inputFile.length();
			fis = new FileInputStream(inputFile);
			dis = new DataInputStream(fis);
			
			//ouput file objects
			//fos = new FileOutputStream(outFile);
			//outBuff =new BufferedOutputStream(fos);
		    //dos =new DataOutputStream(outBuff);
			
			this.nWindowsRead = nWindowsRead;
			this.referenceChr = referenceChr;
			this.nQPoints = nQPoints;
			this.nDPoints = nDPoints;
			this.dataFileId = dataFileId;
						
			process();
			
			if(fis != null)
				fis.close();
			if(dis != null)
				dis.close();
			
			/*if(dos != null){
				dos.flush();
				dos.close();
			}
			if(outBuff != null){
				outBuff.flush();
				outBuff.close();
			}
			if(fos != null){
				fos.flush();
				fos.close();
			}	*/			
			
			/*if (br != null)
				br.close();*/
			if (pr != null){
				pr.flush();
				pr.close();
			}
		}
	
	public void process() throws IOException{
		long readOffset =0L;
		String currentLine;
		//while ((currentLine = br.readLine()) != null) {
		while( readOffset < fileLen ){		
			int qWindow = Integer.reverseBytes(dis.readInt());
			readOffset += (Integer.SIZE / 8);
			int dWindow = Integer.reverseBytes(dis.readInt());
			readOffset += (Integer.SIZE / 8);
			
			if (qWindow >= nQPoints){
				int temp = qWindow;
				qWindow = dWindow;
				dWindow = temp;
			}
			
			dWindow += (nDPoints*dataFileId);			
			int read = (int) Math.floor((double)qWindow/(double)nWindowsRead)+1;			
			float distance = dis.readFloat();
			readOffset += (Float.SIZE / 8);			
			pr.println(String.valueOf(qWindow) + " "+ String.valueOf(read) + " " + String.valueOf(dWindow) + " " + referenceChr + " " + String.valueOf(distance));

		}
	}
	
}
