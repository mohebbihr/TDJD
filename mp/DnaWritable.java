package mnt.miczfs.tide.mp.src;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

public class DnaWritable implements Writable, WritableComparable<DnaWritable> {
 	
	//LongWritable bamWindow;
	//LongWritable read;
	//LongWritable refWindow;
	private Long bamWindow;
	private Long read;
	private Long refWindow;
	private Integer chr;
	private Double dist;

	//IntWritable chr;
	//DoubleWritable dist;
	
	public DnaWritable(){
		super();
	}
	  
	/*public DnaWritable(){
		super();
		this.bamWindow = new Long();
		this.read = new Long();
		this.refWindow = new Long();
		
		this.bamWindow = new LongWritable();
    	this.read = new LongWritable();
    	this.refWindow = new LongWritable();
    	this.chr = new IntWritable();
    	this.dist = new DoubleWritable();
    }*/
	
	//public DnaWritable(LongWritable bamWindow, LongWritable read, LongWritable refWindow, IntWritable chr, DoubleWritable dist){
    public DnaWritable(Long bamWindow, Long read, Long refWindow, Integer chr, Double dist){
		super();	
		
		this.bamWindow = bamWindow;
		this.read = read;
    	this.refWindow = refWindow;
    	this.chr = chr;
    	this.dist = dist;
    }
	
	public DnaWritable(long bamWindow, long read, long refWindow, int chr, double dist){
		super();
		this.bamWindow = new Long(bamWindow);
        this.read = new Long(read);
        this.refWindow = new Long(refWindow);
		this.chr = chr;
		this.dist = dist;		
		/*this.bamWindow = new LongWritable(bamWindow);
    	this.read = new LongWritable(read);
    	this.refWindow = new LongWritable(refWindow);
    	this.chr = new IntWritable(chr);
    	this.dist = new DoubleWritable(dist);*/
    }
    
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(bamWindow);
        out.writeLong(read);
        out.writeLong(refWindow);
        out.writeInt(chr);
        out.writeDouble(dist);
		/*bamWindow.write(dataOutput);
		read.write(dataOutput);
		refWindow.write(dataOutput);
		chr.write(dataOutput);
		dist.write(dataOutput);*/

	}
	
    @Override
    public void readFields(DataInput in) throws IOException {
    	this.bamWindow = in.readLong();
        this.read = in.readLong();
        this.refWindow = in.readLong();
        this.chr = in.readInt();
        this.dist = in.readDouble();
	/*bamWindow.readFields(dataInput);
    	read.readFields(dataInput);
    	refWindow.readFields(dataInput);
    	chr.readFields(dataInput);
    	dist.readFields(dataInput);*/
    }
	
	@Override
	public String toString(){
		return bamWindow + "\t" + read + "\t" + refWindow + "\t" + chr + "\t" + dist;
	}
    
    //public LongWritable getBamWindow() {
    public Long getBamWindow() {
    	return bamWindow;
    }
    
    //public LongWritable getRead() {
    public Long getRead() {
    	return read;
    }
    
    //public LongWritable getRefWindow() {
    public Long getRefWindow() {    
	return refWindow;
    }
    
    //public IntWritable getChr() {
    public Integer getChr(){    
	return chr;
    }
    
    //public DoubleWritable getDist() {
    public Double getDist(){    
	return dist;
    }
    
    //public void setBamWindow(LongWritable bamWindow) {
    public void setBamWindow(Long bamWindow) {    
	this.bamWindow = bamWindow;
    }
    
    //public void setRead(LongWritable read) {
    public void setRead(Long read){
	this.read = read;
    }
    
    //public void setRefWindow(LongWritable refWindow) {
    public void setRefWindow(Long refWindow){
	this.refWindow = refWindow;
    }
    
    //public void setChr(IntWritable chr) {
    public void setChr(Integer chr) {
    	this.chr = chr;
    }
    
    //public void setDist(DoubleWritable dist) {
    public void setDist(Double dist) {   
	this.dist = dist;
    }

      //Add this static method as well
      public static DnaWritable read(DataInput in) throws IOException {
      		DnaWritable dnaWritable = new DnaWritable();
      		dnaWritable.readFields(in);
      		return dnaWritable;
      }

    @Override
    public int compareTo(DnaWritable dnaWritable) {
	return 1;
    }    
  
   
    @Override
    public int hashCode() {
    // This is used by HashPartitioner, so it will hash based on read number
    	return read.hashCode();
    }
}
