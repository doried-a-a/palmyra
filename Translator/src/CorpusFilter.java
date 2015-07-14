import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.SortedSet;

import storage.NumericConverter;
import model2Alignment.Corpora;
import model2Alignment.PhrasePair;
import model2Alignment.Translation;
import model2Alignment.TranslationPair;


public class CorpusFilter {
	public static void correctPuncAndFilter( String source , String target , String outputSource , String outputTarget  , int maxSourceLen , int maxTargetLen ) throws IOException{
		
		Scanner sc1 = new Scanner(new File(source));
		Scanner sc2 = new Scanner(new File(target));
		
		int outCount=0;
		
		PrintWriter writertar = new PrintWriter( outputTarget , "UTF-8");
		PrintWriter writersrc = new PrintWriter( outputSource , "UTF-8");
		
		int count=0; 
		
		int [] statis = new int[200];
		
		while (sc1.hasNext() && sc2.hasNext()){
			
			String s1 = sc1.nextLine() , s2 = sc2.nextLine();
		
			String [] sw = s1.split(" ");
			String [] tw = s2.split(" ");
		
			
			//=====================================================
			HashSet<String> puncs = new HashSet<String>();
			
			for(int i=0; i<sw.length;i++){
				if( !sw[i].equals("..") && sw[i].length()!=1 ) continue;
				else if(sw[i].equals(".."))
					puncs.add("..");
				else if(isPunc(sw[i].charAt(0))) puncs.add( standardPunc(sw[i].charAt(0)) + "" );
			}
			
			String outputDirect = "";
			String lastWord = "#L#A#S#t$W4ord54ds";
			int removedCnt = 0;
			for(int i=0; i<tw.length;i++){
				if( !tw[i].equals("..") && tw[i].length()!=1 ) outputDirect += tw[i] + " ";
				else if (tw[i].equals("..") && puncs.contains("..") && !lastWord.equals("..") ) outputDirect += ".. ";
				else if ( !isPunc(tw[i].charAt(0)) ) outputDirect += tw[i] + " ";
				else if ( tw[i].length()==1 && puncs.contains( standardPunc( tw[i].charAt(0)) + "" )  && !tw[i].equals(lastWord) )
					outputDirect += tw[i] + " ";
				else removedCnt++;
				lastWord = tw[i];
			}
			outputDirect = outputDirect.trim();
			
			//=======================================================
			
			puncs = new HashSet<String>();
			for(int i=0; i<tw.length;i++){
				if( !tw[i].equals("..") && tw[i].length()!=1 ) continue;
				else if(tw[i].equals(".."))
					puncs.add("..");
				else if(isPunc(tw[i].charAt(0))) puncs.add( standardPunc(tw[i].charAt(0)) + "" );
			}
			
			String outputInverse = "";
			lastWord = "#L#A#S#t$W4ord54ds";
			
			for(int i=0; i<sw.length;i++){
				if( !sw[i].equals("..") && sw[i].length()!=1 ) outputInverse += sw[i] + " ";
				else if (sw[i].equals("..") && puncs.contains("..") && !lastWord.equals("..") ) outputInverse += ".. ";
				else if ( !isPunc(sw[i].charAt(0)) ) outputInverse += sw[i] + " ";
				else if ( sw[i].length()==1 && puncs.contains( standardPunc( sw[i].charAt(0)) + "" )  && !sw[i].equals(lastWord) )
					outputInverse += sw[i] + " ";
				else removedCnt++;
				lastWord = sw[i];
			}
			outputInverse = outputInverse.trim();
			
			//===============================================================
			
			
			statis[removedCnt] ++ ;
				
		
			if(outputDirect.length()>0 && outputInverse.length()>0 && outputDirect.split(" ").length <= maxSourceLen
					&& outputInverse.split(" ").length<= maxTargetLen ){
				writertar.println( outputDirect.trim());
				writersrc.println(outputInverse.trim());
				outCount ++;
			}
			
			//System.out.println( c+1 + " : " + removedCnt);
			if ((count+1)%5000==0)
				System.out.println(count+1 + " Sentence Punctioation Corrected.");
			count++;
		}
		
		writertar.close();
		writersrc.close();
		
		for(int i=0;i<statis.length; i++){
			if(statis[i]>0)
				System.out.println(i + " : " + statis[i]);
		}
		System.out.println("Total processed : " + count + " - accepted : "  + outCount);
		
	}
	
	public static void filter( String source , String target , String alignProb , String outputSource , String outputTarget , double maxPerp ) throws IOException{
		
		Scanner sc1 = new Scanner(new File(source));
		Scanner sc2 = new Scanner(new File(target));
		Scanner sc3 = new Scanner(new File(alignProb));
		
		PrintWriter writertar = new PrintWriter( outputTarget , "UTF-8");
		PrintWriter writersrc = new PrintWriter( outputSource , "UTF-8");
		
		int count=0; // count of all sentences 
		int acc=0;   // count of accepted sentences
		
		while (sc1.hasNext() && sc2.hasNext() && sc3.hasNext()){
			
			String s1 = sc1.nextLine() , s2 = sc2.nextLine();
			double prob = Double.parseDouble(sc3.nextLine());
			
			if(prob>maxPerp && prob<200){
				System.out.println(s2 + "\n" + s1 + "\n" + prob );
				System.out.println("===========================================");
			}
			
		
			if (prob < maxPerp ){
				writertar.println( s2 );
				writersrc.println( s1 );
				acc++;
			}
			
			count++;
		}
		
		writertar.close();
		writersrc.close();
		
		System.out.println(acc + "/" + count + " Sentences were accepted.");
		
	}
	
	
public static void sortByPerp( String source , String target , String alignProb , String output , NumericConverter con ) throws IOException{
		
		Scanner sc1 = new Scanner(new File(source));
		Scanner sc2 = new Scanner(new File(target));
		Scanner sc3 = new Scanner(new File(alignProb));
		
		PrintWriter writer = new PrintWriter( output , "UTF-8");
		
		PriorityQueue<PhrasePair> set = new PriorityQueue<PhrasePair>();
	
		while (sc1.hasNext() && sc2.hasNext() && sc3.hasNext()){
			
			String s1 = sc1.nextLine() , s2 = sc2.nextLine();
			double prob = Double.parseDouble(sc3.nextLine());
			set.add( new PhrasePair(s1 , s2 , prob) );
		}
		
		
		while(!set.isEmpty()){
			
			PhrasePair trans = set.poll();
			
			if(con!=null){
				String sourceSentence = "";
				String [] words = trans.source.split(" ");
				for(int i=0;i<words.length;i++){
					sourceSentence += con.getWordOfSourceHash(Integer.parseInt(words[i])) + " ";
				}
				sourceSentence = sourceSentence.trim();
				
				String targetSentence = "";
				words = trans.target.split(" ");
				for(int i=0;i<words.length;i++){
					targetSentence += con.getWordOfTargetHash(Integer.parseInt(words[i])) + " ";
				}
				targetSentence = targetSentence.trim();
				
				writer.write( sourceSentence + "\n" + targetSentence + "\n" +trans.probability +"\n");
				writer.println("===========================");
				
			}
			
			else {
				writer.write( trans.source + "\n" + trans.target + "\n" +trans.probability +"\n");
				writer.println("===========================");
			}
		}
		
		writer.close();
		
	}

public static void sortByPerpAndSentenceLengthDistribution( String source , String target , String alignProb , double reliablePerbLimit , double lengthDistWeight , String output , NumericConverter con ) throws IOException{
	
	if(lengthDistWeight > 1 || lengthDistWeight < 0) throw new RuntimeException("Weght must be between 0 and 1.");
	
	double [] d = getStatisticsAboutSentencesDifference(source, target, alignProb, reliablePerbLimit);
	
	Scanner sc1 = new Scanner(new File(source));
	Scanner sc2 = new Scanner(new File(target));
	Scanner sc3 = new Scanner(new File(alignProb));
	
	PrintWriter writer = new PrintWriter( output , "UTF-8");
	
	PriorityQueue<PhrasePair> set = new PriorityQueue<PhrasePair>();

	while (sc1.hasNext() && sc2.hasNext() && sc3.hasNext()){
		
		String s1 = sc1.nextLine() , s2 = sc2.nextLine();
		double prob = Double.parseDouble(sc3.nextLine());
		double lenDifProb = d[100 + s1.split(" ").length - s2.split(" ").length];
		lenDifProb = -Math.log(lenDifProb);

		set.add( new PhrasePair(s1 , s2 , (1-lengthDistWeight)*prob + lengthDistWeight*lenDifProb) );
	}
	
	
	while(!set.isEmpty()){
		
		PhrasePair trans = set.poll();
		
		if(con!=null){
			String sourceSentence = "";
			String [] words = trans.source.split(" ");
			for(int i=0;i<words.length;i++){
				sourceSentence += con.getWordOfSourceHash(Integer.parseInt(words[i])) + " ";
			}
			sourceSentence = sourceSentence.trim();
			
			String targetSentence = "";
			words = trans.target.split(" ");
			for(int i=0;i<words.length;i++){
				targetSentence += con.getWordOfTargetHash(Integer.parseInt(words[i])) + " ";
			}
			targetSentence = targetSentence.trim();
			
			writer.write( sourceSentence + "\n" + targetSentence + "\n" +trans.probability +"\n");
			writer.println("===========================");
			
		}
		
		else {
			writer.write( trans.source + "\n" + trans.target + "\n" +trans.probability +"\n");
			writer.println("===========================");
		}
	}
	
	writer.close();
	sc1.close(); sc2.close(); sc3.close();
}




public static double[] getStatisticsAboutSentencesDifference(String source , String target , String alignProb , double reliablePerbLimit) throws IOException{
	
	Scanner sc1 = new Scanner(new File(source));
	Scanner sc2 = new Scanner(new File(target));
	Scanner sc3 = new Scanner(new File(alignProb));
	
	double [] d = new double[200];
	int count=0;
	while (sc1.hasNext() && sc2.hasNext() && sc3.hasNext()){
		
		String s1 = sc1.nextLine() , s2 = sc2.nextLine();
		
		// normalized align probability
		double prob = Double.parseDouble(sc3.nextLine());
		if( prob>=reliablePerbLimit) continue;
		count++;
		d[100+s1.split(" ").length - s2.split(" ").length]+= 1;

	}
	
	for(int i=0;i<200;i++)
		d[i]/=count;
	
	sc1.close(); sc2.close(); sc3.close();
	
	return d;
	
}

	public static double getAverageSentencesDifference(String source , String target , String alignProb , double maxAllowedPerb ) throws IOException{
		
		Scanner sc1 = new Scanner(new File(source));
		Scanner sc2 = new Scanner(new File(target));
		Scanner sc3 = new Scanner(new File(alignProb));
		
		double sum = 0 ;
		int count  = 0 ;
		while (sc1.hasNext() && sc2.hasNext() && sc3.hasNext()){
			
			String s1 = sc1.nextLine() , s2 = sc2.nextLine();
			double prob = Double.parseDouble(sc3.nextLine());
			if(prob<maxAllowedPerb){
				count ++;
				sum += s1.split(" ").length - s2.split(" ").length;
			}
		}	
		
		sc1.close();sc2.close();sc3.close();
		return sum/count;
		
	}


	
	
	public static void filterCorpusAccordingToPerpAndSentenceLengthDistribution(
			String source , String target , String alignProb , double cutOffPerb , 
			double reliablePerbLimit , double lengthDistWeight , String outputDir , NumericConverter con ) throws IOException{
		
		if(lengthDistWeight > 1 || lengthDistWeight < 0) throw new RuntimeException("Weght must be between 0 and 1.");
		
		double [] d = getStatisticsAboutSentencesDifference(source, target, alignProb, reliablePerbLimit);
		
		Scanner sc1 = new Scanner(new File(source));
		Scanner sc2 = new Scanner(new File(target));
		Scanner sc3 = new Scanner(new File(alignProb));
		
		PrintWriter snwr = new PrintWriter( outputDir + "source.fil.num" , "UTF-8");
		PrintWriter tnwr = new PrintWriter( outputDir + "target.fil.num" , "UTF-8");
		
		PrintWriter slwr=null,tlwr=null;
		if(con!=null){
			slwr = new PrintWriter( outputDir + "source.fil.lex" , "UTF-8");
			tlwr = new PrintWriter( outputDir + "target.fil.lex" , "UTF-8");
		}
		
		
		PriorityQueue<PhrasePair> set = new PriorityQueue<PhrasePair>();

		while (sc1.hasNext() && sc2.hasNext() && sc3.hasNext()){
			
			String s1 = sc1.nextLine() , s2 = sc2.nextLine();
			double prob = Double.parseDouble(sc3.nextLine());
			double lenDifProb = d[100 + s1.split(" ").length - s2.split(" ").length];
			lenDifProb = -Math.log(lenDifProb);

			set.add( new PhrasePair(s1 , s2 , (1-lengthDistWeight)*prob + lengthDistWeight*lenDifProb) );
		}
		
		while(!set.isEmpty()){
			
			PhrasePair trans = set.poll();
			if (trans.probability > cutOffPerb)
				continue;
			
			if(con!=null){
				String sourceSentence = con.getWordsOfSouceHashes(trans.source);
				String targetSentence = con.getWordsOfTargetHashes(trans.target);
				slwr.println( sourceSentence );
				tlwr.println( targetSentence );
			}

			snwr.println(trans.source);
			tnwr.println(trans.target);
		}
		
		snwr.close(); tnwr.close();
		if(con!=null){
			slwr.close(); tlwr.close();
		}
		
	}
	
	
	
	public static char standardPunc(char ch){
		switch(ch){
		case ',': case '،': case ';':
			return '،';
		case '?': case '؟':
			return '?';
		case '-': case '_':
			return '_';
		default:
			return ch;
		}
	}
	
	public static boolean isPunc(char ch) throws IOException{
		String arabic = "ابتثجحخدذرزسشصضطظعغفقكلمنهويأآئءؤةﻻﻵﻷ";
		boolean res =  !( (ch>='0' && ch<='9') || (ch>='A' && ch <='Z' ) || (ch>='a' && ch<='z') ||  arabic.indexOf(ch)>-1  );
		return res;
	}
}
