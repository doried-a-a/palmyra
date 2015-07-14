package model2Alignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class PhraseTableCreator {
	Corpora corpora;
	HashMap<PhrasePair, Float > phi  = new HashMap<PhrasePair, Float>();
	HashMap<PhrasePair, Float > countPair = new HashMap<PhrasePair, Float>();
	HashMap<String, Float> countA=new HashMap<String , Float>();
	boolean [][] map;
	String [] eng;
	String [] ara;
	boolean [] isAligned;
	public static String[] divideString(String str) {
		str = str.trim();
		return str.split(" ");
	}
	
	protected void creatPhraseTable(String alignmentFilePath, String outputFilePath) throws FileNotFoundException, UnsupportedEncodingException{
		Scanner sc = new Scanner(new File(alignmentFilePath));
		
		PrintWriter wr = new PrintWriter(outputFilePath, "UTF-8");
		
		for(int i=0;i<corpora.getCount();++i){
			String ln=sc.nextLine();
			String [] align=divideString(ln);
			 eng=divideString(corpora.getSource(i));
			 ara=divideString(corpora.getTranslate(i));
			map =new boolean [ara.length][eng.length];
			isAligned=new boolean[eng.length];
			
			for(int j=0;j<align.length;++j){
				try{
					int col=Integer.parseInt(align[j].split("-")[0]);
					int row=Integer.parseInt(align[j].split("-")[1]);
					map[row][col]=true;
					isAligned[col]=true;
				} catch (Exception ee){
					System.out.println(align[j]);
				}
			}
           //for(int k=0;k<isAligned.length;++k)System.out.print(isAligned[k]?"1 ":"0 ");             
			for(int eStart=0;eStart<ara.length;eStart++){
				for (int eEnd = eStart; eEnd < ara.length; eEnd++) {
					
					int fStart=eng.length-1;
					int fEnd=-1;//because we started indexing from 0 and the algorithm in the book started from 1
					for (int row = 0; row < ara.length; row++) {
						for (int col = 0; col < eng.length; col++) {
							if(map[row][col]==true){
								if(row<=eEnd && row>=eStart){
									fStart=Math.min(fStart, col);
									fEnd=Math.max(fEnd, col);					
									
								}								
							}
						}
					}
					//System.out.println("DEBUG  ADD");
					addExtract(fStart,fEnd,eStart,eEnd);
					
					
				}	
				
			}
				
		}
		
		normalize();
		
		//writing results
		for (Iterator iterator = phi.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<PhrasePair, Float> ent= (Entry<PhrasePair, Float>) iterator.next();
			PhrasePair tp=ent.getKey();
			String araPhrase=tp.target;
			float ans=ent.getValue();
			//wr.println(tp.target+"#"+tp.source+"#"+String.valueOf(ans));
                        wr.println("("+tp.target+") , ("+tp.source+") #"+String.valueOf(ans));
			phi.put(tp, ans);
		}
		wr.close();
		
	}
	
	

	private void normalize() {
		// TODO Auto-generated method stub
		for (Iterator iterator = countPair.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<PhrasePair, Float> ent= (Entry<PhrasePair, Float>) iterator.next();
			PhrasePair tp=ent.getKey();
			String araPhrase=tp.target;
			float ans=ent.getValue()/countA.get(araPhrase);
			phi.put(tp, ans);
		}
	}

	private void addExtract(int fStart, int fEnd, int eStart, int eEnd) {
		// TODO Auto-generated method stub
		if(fEnd==-1)return;
		
        //System.out.println(ara.length);
		for (int row = 0; row < ara.length; row++) {
			for (int col = 0; col < eng.length; col++) {
				if(map[row][col]==true){
					if(col<=fEnd && col>=fStart && (row<eStart || row >eEnd))return;
					if(row<=eEnd && row>=eStart && (col<fStart || col >fEnd))return;
					
				}
				
				
			}
			
			
		}
              //  System.out.println("PAssed");
		String arPhrase="";
		String enPhrase="";
		for(int i=eStart;i<=eEnd;++i){arPhrase+=ara[i]+((i==eEnd)?"":" ");}
		for (int i = fStart; i <= fEnd; i++){enPhrase+=eng[i]+((i==fEnd)?"":" ");}
		incArPhrase(arPhrase);
		//half for the minimal matched and the remaining mass  for the null aligned
		int nulPre=0,nulPost=0;
                
               // System.out.println("PAssed");
		for(int i=fStart-1;i>=0 && !isAligned[i];--i)nulPre++;
		//for(int i=fEnd+1;i<eng.length && !isAligned[i];++i)nulPost++;
          //      if(arPhrase.equals("he"))System.out.println(nulPre+" "+nulPost+" "+fStart+" "+fEnd);
		if(nulPre+nulPost==0){
			//System.out.println(enPhrase+" "+arPhrase);
			incTransPairCount(enPhrase, arPhrase, 1);
			return;
			}//there are no null aligned  befor or after so we continue
		
		float dinomerator=(nulPost==0)?nulPre:((1+nulPre)*nulPost)-1;
                dinomerator=(dinomerator==0)?1:dinomerator;
		int fs=fStart;
                
              //  System.out.println("PAssed");
		do {
			int fe=fEnd;
			do {
				String newEngPhrase="";
				for (int i = fs; i <= fe; i++){newEngPhrase+=eng[i]+((i==fe)?"":" ");}
				if(fs==fStart && fe==fEnd)incTransPairCount(enPhrase, arPhrase, 0.5f);
				else incTransPairCount(newEngPhrase, arPhrase,(float) (0.5/dinomerator) );
				
			fe++;	
              //  System.out.println("PAssed");
			}while(fe<eng.length && !isAligned[fe]);	
			
		fs--;	
                
               // System.out.println("PAssed");
		}while(fs>=0 && !isAligned[fs]);
		
		
             //   System.out.println("PAssed");
		
	}
	
	
	
	public void incArPhrase(String p){
		if(countA.get(p)==null)countA.put(p, (float) 1);
		else countA.put(p, countA.get(p)+1);
	}
	public void incTransPairCount(String p1, String p2, float amount){
		PhrasePair tp=new PhrasePair(p1, p2);
		if(countPair.get(tp)==null)countPair.put(tp, amount);
		else countPair.put(tp, countPair.get(tp)+amount);
		
	}
	public static void main(String[] args) {
		
		Corpora cp = null;
		try{
			cp= new LoadableCorpora(new File("./files/en50000.pun.fil"), new File("./files/ar50000.pun.fil"));	
			PhraseTableCreator c = new PhraseTableCreator();
			c.corpora = cp;
			c.creatPhraseTable("Merged", "PhraseTable");
		}
		catch(Exception e){
            System.out.println(e.toString());    
        }


	}

}
