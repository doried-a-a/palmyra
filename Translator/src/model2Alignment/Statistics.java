package model2Alignment;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Scanner;

import javax.management.RuntimeErrorException;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;


public class Statistics {
	
	public static int maximumSourceSentenceLength = 50;
	public static int maximumTargetSentenceLength = 50;
	private static boolean isMaximumLengthProvided = false;
	
	// used for calculating initial prob. of translation 
	
	private 	int 						targetLandWordsCount ;
	private 	boolean 					isSourceWordCountProvidedWhenBuild = false;
	
	// used for noisy_channel_model translation
	
	private  	HashMap<String, Integer> 	unigrams 		= new HashMap<String, Integer>();
	public 		int							unigramsCount ;
	private 	boolean 					isUnigramsProvidedWhenBuild 	= false;
	
	
	// counts that are calculated during the execution of the EM algorithm
	
	public  HashMap<TranslationPair,Float> 		transCount  	= new HashMap<TranslationPair,Float> ();
	public  HashMap<Integer , Float > 			wordCount		= new HashMap<Integer,Float>();
	
	
	float [][][][] 								align4Count;
	float [][][]   								align3Count;
	
	// parameters used when building alignment and translation model during EM algorithm
	public 	 float [][][][]						q; 	
	public   HashMap<TranslationPair,Float>	t 				= new HashMap<TranslationPair,Float>();
	
	
	/** initializing counts */
	public  void resetCounts(){
		transCount.clear();
		wordCount.clear();
		transCount  	= new HashMap<TranslationPair,Float> ();
		wordCount		= new HashMap<Integer,Float> ();
		
		align4Count = new float[maximumSourceSentenceLength][][][];
		for(int i=0;i<maximumSourceSentenceLength; i++){
			align4Count[i]=new float[maximumTargetSentenceLength][][];
			for(int j=0;j<maximumTargetSentenceLength;j++)
			{
				align4Count[i][j]=new float[maximumTargetSentenceLength][];
				for(int k=0;k<maximumTargetSentenceLength;k++)
					align4Count[i][j][k] = new float[maximumSourceSentenceLength];	
			}
		}
		
		align3Count = new float[maximumTargetSentenceLength][][];
		for(int i=0;i<maximumTargetSentenceLength; i++){
			align3Count[i]=new float[maximumTargetSentenceLength][];
			for(int j=0;j<maximumTargetSentenceLength;j++)
				align3Count[i][j]=new float[maximumSourceSentenceLength];
		}
		
	}
	
	/**
	 * 
	 * @param targetUnigramsPath path to the unigrams of the target language , or null. necessary when translating using noisy-channel-model
	 * @param targetLangWordsCount the count of the words in the target language, or null. used as initial value for translation in EM algorithm.
	 * so necessary when building a model 
	 */
	
	public Statistics(String targetUnigramsPath , Integer targetLangWordsCount) {
		
		q = new float[maximumSourceSentenceLength][][][];
		for(int i=0;i<maximumSourceSentenceLength; i++){
			q[i]=new float[maximumTargetSentenceLength][][];
			for(int j=0;j<maximumTargetSentenceLength;j++)
			{
				q[i][j]=new float[maximumTargetSentenceLength][];
				for(int k=0;k<maximumTargetSentenceLength;k++){
					q[i][j][k] = new float[maximumSourceSentenceLength];
					for(int l=0; l < maximumSourceSentenceLength ; l++)
						q[i][j][k][l]=-1.0f;  // it means not initialized yet
				}
			}
		}
		
		if(targetLangWordsCount==null)
			this.isSourceWordCountProvidedWhenBuild=false;
		else
		{
			this.targetLandWordsCount = targetLangWordsCount.intValue();
			this.isSourceWordCountProvidedWhenBuild = true;
		}
		
		if(targetUnigramsPath == null) {
			isUnigramsProvidedWhenBuild = false; 
			return;
		}
		
		isUnigramsProvidedWhenBuild = true;
		
		Scanner sc;
		String val1="" ; int freq;
		try {

			sc = new Scanner(new File(targetUnigramsPath));
			unigramsCount = 0;
			while(sc.hasNext()){
				freq = sc.nextInt();
				val1  = sc.nextLine().substring(1).trim();
				unigrams.put(val1, new Integer(freq));
				unigramsCount ++ ;
			}
			System.out.println("Debug: unigrams loaded");
			sc.close();
			
		}catch (Exception ee){}
			
	}
	
	/** increments count(target,source) by (amount) */
	public void incrementTransCount(int target,int source,double amount){
		if(amount<0) throw new RuntimeException();
		else if(amount==0) return;
		else transCount.put(new TranslationPair(source,target), new Float(getTransCount(target, source)+amount));
	}
	/** gets count(target,source), 0 if not initialized yet. */
	public double getTransCount(int target,int source){
		TranslationPair pr = new TranslationPair(source, target);
		Float count = transCount.get(pr);
		if(count==null) return 0;
		else return count.doubleValue();
	}
	
	/** increments count(word) by (amount) */
	public void incrementWordCount(int word,double amount){
		if(amount<0) throw new RuntimeException();
		else if(amount==0) return;
		else wordCount.put(word , new Float(getWordCount(word)+amount));
	}
	/** gets count(word) , 0 if not initialized yet. */
	public double getWordCount(int word){
		Float count = (Float) wordCount.get(word);
		if(count==null) return 0;
		else return count.doubleValue();
	}
	
	/**increments count(i,j,Lt,Ls) by (amount).   */
	public void incrementAlign4Count(int i,int j,int Lt , int Ls ,double amount){
		if(Lt>maximumTargetSentenceLength || Ls>maximumSourceSentenceLength)
			throw new RuntimeException("index greater than sentence length " );
		if(amount<0) throw new RuntimeException();
		else if(amount==0) return;
		else align4Count[i][j][Lt][Ls]+=amount;
		//else align4Count.put( new Alignment4(i, j , Lt, Ls), new Float(getAlign4Count(i,j,Lt,Ls)+amount));
	}
	
	/**gets count(i,j,Lt,Ls) , 0 if not initialized yet. */
	public double getAlign4Count(int i,int j, int Lt,int Ls){
		if(Lt>maximumTargetSentenceLength || Ls>maximumSourceSentenceLength)
			throw new RuntimeException("index greater than sentence length " );
		//Float count = (Float) align4Count.get(new Alignment4(i,j, Lt, Ls));
		//if(count==null) return 0;
		//else return count.doubleValue();
		return align4Count[i][j][Lt][Ls];
	}
	
	
	/** increments count(j,Lt,Ls) by amount. */
	public void incrementAlign3Count(int j,int Lt , int Ls ,double amount){
		if(Lt>maximumTargetSentenceLength || Ls>maximumSourceSentenceLength)
			throw new RuntimeException("index greater than sentence length " );
		if(amount<0) throw new RuntimeException();
		else if(amount==0) return;
	//	else align3Count.put( new Alignment3( j , Lt, Ls), new Float(getAlign3Count(j,Lt,Ls)+amount));
		align3Count[j][Lt][Ls] += amount;
	}
	
	/**gets count(j,Lt,Ls) , 0 if not initialized yet. */
	public double getAlign3Count(int j, int Lt,int Ls){
		if(Lt>maximumTargetSentenceLength || Ls>maximumSourceSentenceLength)
			throw new RuntimeException("index greater than sentence length " );
//		Float count = (Float) align3Count.get(new Alignment3(j, Lt, Ls));
//		if(count==null) return 0;
//		else return count.doubleValue();
		return align3Count[j][Lt][Ls];
	}
	
	
	
 	/**sets parameter t(targetWord|sourceWord) to (value) */
	public void setT(int targetWord , int sourceWord , double value){
		if(value<0 || value>1) throw new RuntimeException();
		else t.put( new TranslationPair (sourceWord , targetWord) , new Float(value));
	}
	

	/**gets parameter t(targetWord|sourceWord) , 1/(targetLangWordsCount+1) if not initialized yet. */
	public double getT(int targetWord,int sourceWord){
		Float count = (Float) t.get(new TranslationPair(sourceWord,targetWord));
		if(count==null) {
			setT(targetWord,sourceWord,1.0/(this.getTargetLangWordsCount()+1));
			return 1.0/ (this.getTargetLangWordsCount()+1);
		}
		else return count.doubleValue();
		
	
	}
	
		
	/**
	 gets q(i|j,Lt,Ls) prob. of aligning target word j to source word i given target sentence length Lt and source sentence length Ls
	 */
	public void setQ(int i,int j,int Lt,int Ls , double value){
		if(Lt>maximumTargetSentenceLength || Ls>maximumSourceSentenceLength)
			throw new RuntimeException("index greater than sentence length " );
		if(value<0 || value>1) throw new RuntimeException();
		//else q.put( new Alignment4(i, j, Lt, Ls)  , new Float(value));
		q[i][j][Lt][Ls] = (float) value;
	}
	
	/**
	 gets q(i|j,Lt,Ls) , the prob. of aligning target word j to source word i given target sentence length Lt and source sentence length Ls , 
	 1/(Ls) if not initialized yet. [supposing that the source phrase contains #NULL# token , and Ls considered this fact]
	 */
	public double getQ(int i,int j,int Lt,int Ls ){
		//Float count = (Float) q.get(new Alignment4(i, j, Lt, Ls));
		// Here , I've set a default value of q : Suppose we have uniform distribution => the probability that the 
		// word j from source is aligned to any word of the destination (or to no thing) is 1/(destination words count+1)
//		if(count==null) {
//			setQ(i,j,Lt,Ls,1.0/(Ls));
//			return 1.0/(Ls);
//		}
//		else return count.doubleValue();
		
		// if not initialized yet , then consider a uniform distribution
		if(Lt>maximumTargetSentenceLength || Ls>maximumSourceSentenceLength)
			throw new RuntimeException("index greater than sentence length " );
		if(q[i][j][Lt][Ls]==-1.0f)
			return 1.0f/Ls;
		else return q[i][j][Lt][Ls];
	}
	
	/**gets count(s) in the unigrams language model*/
	public int getUnigramCount(int s){
		if(isUnigramsProvidedWhenBuild==false) 
			throw new RuntimeException("No unigrams provided when creating the statistics object.");
		
		Integer res = unigrams.get(s);
		return (res==null?0:res.intValue());
	}
		
	/**returns the number of words in the target language (if exists) , that was given while creating the Statistics object */
	public int getTargetLangWordsCount(){
		if(!isSourceWordCountProvidedWhenBuild) 
			throw new RuntimeException("Not provided when creating the statistics object");
		else return targetLandWordsCount;
	}
	
}








