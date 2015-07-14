package model2Alignment;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Scanner;

public class NonLoadableCorpora extends Corpora{
	public static int	  trainingLineLengthLimit = 100*6;
	
	
	private int lastLoaded = -1;
	private String lastLoadedSrc = "";
	private String lastLoadedTar = "";
	
	private Scanner sc1 = null, sc2 = null ;
	private File f1,f2;
	
	public NonLoadableCorpora (File f1 , File f2) throws Exception {
		
		super(f1,f2);
		this.f1 = f1;
		this.f2 = f2;
		
	    sc1 = new Scanner(f1,"UTF-8");
		sc2 = new Scanner(f2,"UTF-8");
	
	}
	
	public String getSource(int idx){
		if(idx<0 || idx>=count)
			throw new ArrayIndexOutOfBoundsException();
		
		if(idx<lastLoaded){
			try{
				sc1 = new Scanner(f1 , "UTF-8");
				sc2 = new Scanner(f2 , "UTF-8");
			//	System.out.println("NonLoadableCorpora restarted from begin.");
			}
			catch(Exception ee){
				throw new RuntimeException("NonLoadableCorpora : File(s) not found");
			}
			lastLoaded = -1;
		}
		
		while(lastLoaded<idx){
			lastLoadedSrc = sc1.nextLine();
			lastLoadedTar = sc2.nextLine();
			lastLoaded ++;
		}
		
		return lastLoadedSrc;
		
	}	
	
	
	public String getTranslate(int idx){
		if(idx<0 || idx>=count)
			throw new ArrayIndexOutOfBoundsException();
		
		if(idx<lastLoaded){
			try{
				sc1 = new Scanner(f1 , "UTF-8");
				sc2 = new Scanner(f2 , "UTF-8");
			//	System.out.println("NonLoadableCorpora restarted from begin.");
			}
			catch(Exception ee){
				throw new RuntimeException("NonLoadableCorpora : File(s) not found");
			}
			lastLoaded = -1;
		}
		
		while(lastLoaded<idx){
			lastLoadedSrc = sc1.nextLine();
			lastLoadedTar = sc2.nextLine();
			lastLoaded ++;
		}
		
		return lastLoadedTar;
	}
	
}

