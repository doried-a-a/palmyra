package model2Alignment;


import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Scanner;

public abstract class Corpora{
	
	public static int	  trainingLineLengthLimit = 100*6;
	
	protected int count = 0;
	
	public Corpora (File f1 , File f2) throws Exception {

		LineNumberReader  lnr1 = new LineNumberReader(new FileReader(f1));
		LineNumberReader  lnr2 = new LineNumberReader(new FileReader(f2));
		
		lnr1.skip(Long.MAX_VALUE);
		lnr2.skip(Long.MAX_VALUE);
		
		
		if( lnr1.getLineNumber() != lnr2.getLineNumber()){
			lnr1.close();
			lnr2.close();
			throw new Exception ("Line number in the translation and source files is not the same.");
		}
		count = lnr1.getLineNumber();
		
		lnr1.close();
		lnr2.close();
			
	}
	
	public abstract String getSource(int idx);
	
	public abstract String getTranslate(int idx);
	
	public int getCount(){
		return count;
	}
}

