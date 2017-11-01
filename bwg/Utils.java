/*
 * TILSH/Utils.java - Interchromosome Translocation/Insertion Detector 
 * This class provides useful functions for the TDLSH project, which implements an algorithm to detect translocations 
 * in genome using a technique that splits the reads into pre-determined length windows. This class generates 
 * fingerprints to represent those windows. The fingerprints are later submitted to an application that implements MinHash 
 * for the approximate NN search.  
 * Version 1.0 by Rosanne Vetro
 * June 2013
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.nio.ByteBuffer;
import java.lang.StringBuilder;
import java.util.stream.IntStream;

public class Utils {
	
	static List<Set> words=null;
	
	public static byte[] stringToByteArray(String str){
		//System.out.println("str: " + str + " ,len: " + str.length());
		byte[] wbytes = new byte[64]; // 512 / 8  = 64
		int byte_idx =0 ;

		for(int i=0; i<str.length(); i+=8){
			short a = Short.parseShort(str.substring(i,i+8), 2);
			ByteBuffer bytes = ByteBuffer.allocate(2).putShort(a);
			wbytes[byte_idx] = bytes.array()[1];
			byte_idx ++;
		}
		return wbytes;
	}
	
	private static String byteArrayToString(byte[] rdata, int offset){ 
		String readstr = "";
		for(int i=offset; i<rdata.length; i++){
			Byte cb = new Byte(rdata[i]);				
			readstr += ("0000000" + Integer.toBinaryString(0xFF & cb)).replaceAll(".*(.{8})$", "$1");
			
		}
		return readstr;
	}

	private static boolean pairExists(Set<String> p ){		
		if(words.size() == 0 ) return false;
							
		for(Set<String> q : words){	
			if(q.containsAll(p)){
				return true;				
			}									
		}			
		
		return false;
	}

	//Permute considering reverse complement
	public static List<Set> permute(char[] chars, int length) {
	    final double NUMBER_OF_PERMUTATIONS = Math.pow(chars.length, length);// list is longer than needed
	    words = new ArrayList<Set>();
	    char[] temp = new char[length];
	    Arrays.fill(temp, '0');

	    for (int i = 0; i < NUMBER_OF_PERMUTATIONS; i++) {
	        int n = i;
	        for (int k = 0; k < length; k++) {
	            temp[k] = chars[n % chars.length];
	             n /= chars.length;
	        }
	        Set<String> pair = new HashSet<String>(); 
	        pair.add(String.valueOf(temp));
	        pair.add(reverseComplement(String.valueOf(temp)));
		if (words.indexOf(pair) == -1)
	        	words.add(pair);
	    }
	    return words;
	}

	// This method convert character window into binary window, we assign each number associated
	// to a fream a bit in a 32 bit finger print. 
	public static byte[] binaryCodingJaccardWindow(String window, short permuteLength,boolean isReference){
		
		//int jw_size = words.size();
		int jw_size = 512; // more than its needed.
		FixedSizeBitSet jw = new FixedSizeBitSet(jw_size);
		jw.clear();

		if(isReference)
			jw.set(1);
		else
			jw.set(2);
		
		for (int i=0; i<= window.length()-permuteLength;i++){
			String current = window.substring(i, i+permuteLength);
			//COSIDER LOWER AND UPPER THE SAME
			/*****added to onsider reverse complement*****/
			Set<String> pair = new HashSet<String>();
			pair.add(current.toUpperCase());
			pair.add(reverseComplement(current.toUpperCase()));
			/********************************************/
			if (words.indexOf(pair) == -1)
				System.out.println("Did not find permutation and rc: "+ window);
			else				
				jw.set(words.indexOf(pair));
		}
		return stringToByteArray(jw.toString());
	}
	
	public static String jaccardWindow(String window, short permuteLength){
		
		//We have 4 possible letters, we skip N	
		//int jw_size = words.size();
		int jw_size = 256; // for chunksize = 4
		BitSet jw = new BitSet(jw_size);
				
		for (int i=0; i<= window.length()-permuteLength;i++){
			String current = window.substring(i, i+permuteLength);
			//COSIDER LOWER AND UPPER THE SAME
			/*****added to onsider reverse complement*****/
			Set<String> pair = new HashSet<String>();
			pair.add(current.toUpperCase());
			pair.add(reverseComplement(current.toUpperCase()));
			/********************************************/
			if (words.indexOf(pair) == -1)
				System.out.println("Did not find permutation and rc: "+ window);
			else				
				jw.set(words.indexOf(pair));
		}
		String jwString = "";
		int i;
		for(i=0; i<jw_size; i++){
			if (jw.get(i)){
				jwString += String.valueOf(i);
				if (i < jw_size-1)
					jwString += " ";
			}
		}
		return jwString;
	}

	public static String reverseComplement(String window){ 

		char[] rc = new char[window.length()];
	       	for (int i = window.length()-1; i >= 0; i--){
	       		int index = window.length()-1-i;
	       		rc[index] = window.charAt(i);
	       		char sym = rc[index];
			if(sym == 'A' || sym == 'a') {
				rc[index] = 'T';
			} else if(sym == 'G' || sym == 'g'){
				rc[index] = 'C';
			} else if(sym == 'C' || sym == 'c') {
				rc[index] = 'G';
			} else if(sym == 'T' || sym == 't') {
				rc[index] = 'A';
			}
		}
		return String.valueOf(rc);
	}
}

class FixedSizeBitSet extends BitSet {
    private final int nbits;

    public FixedSizeBitSet(final int nbits) {
        super(nbits);	
        this.nbits = nbits;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(nbits);
        IntStream.range(0, nbits).mapToObj(i -> get(i) ? '1' : '0').forEach(buffer::append);
        return buffer.toString();
    }
}
