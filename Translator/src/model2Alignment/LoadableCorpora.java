package model2Alignment;


import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Scanner;

public class LoadableCorpora extends Corpora{

	String source[],trans[];
	
	public LoadableCorpora (File f1 , File f2) throws Exception {
		
		super(f1,f2);

		Scanner sc1 = new Scanner(f1,"UTF-8");
		Scanner sc2 = new Scanner(f2,"UTF-8");
		
		source = new String[super.count];
		trans  = new String[super.count];
		
		int i=0;
		
		while(sc1.hasNext() && sc2.hasNext()){
			source[i] = sc1.nextLine();
			trans [i] = sc2.nextLine();
			if(source[i].length()> trainingLineLengthLimit || trans[i].length()>trainingLineLengthLimit) {
				count--; 
				continue;
			}
			i++;
		}
		
		sc1.close();
		sc2.close();
		
		if(i!=count) throw new Exception("Files reading was not completed successfully. Operation aborted."); 
		
	}
	
	public String getSource(int idx){
		if(idx<0 || idx>=count)
			throw new ArrayIndexOutOfBoundsException();
		return source[idx];
	}
	
	public String getTranslate(int idx){
		if(idx<0 || idx>=count)
			throw new ArrayIndexOutOfBoundsException();
		return trans[idx];
	}
}

