package model2Alignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Model {

	public  int 	maximumSourceSentenceLength = 50;
	public  int 	maximumTargetSentenceLength = 50;

	
	Statistics st;
	HashMap<TranslationPair, Float> map;
	float [][][][] align4;
	
	//public static final int maxSentenceLength = 40;
	
	public Model(Statistics st , String path , int maxSourLength , int maxTarLength) throws FileNotFoundException,
			UnsupportedEncodingException {
		
		this.st = st;
		this.maximumSourceSentenceLength = maxSourLength;
		this.maximumTargetSentenceLength = maxTarLength;
		
		String fileName = path + "Trans.txt";
		
		Scanner sc1 = new Scanner(new File(fileName));
		
		map = new HashMap<TranslationPair, Float>();

		while (sc1.hasNext()) {
			
			int t1 = sc1.nextInt();
			int t2 = sc1.nextInt();			
			float pro = sc1.nextFloat();

			TranslationPair pp = new TranslationPair(t1, t2);
			Float val = map.get(pp);
			
			if (val == null || (val != null && val.doubleValue() < pro))
				map.put(pp, pro);
		}	
		
		sc1.close();
		
		fileName = path + "Align.txt";
		
		sc1 = new Scanner(new File(fileName));
		
		align4 = new float[maximumSourceSentenceLength][][][];
		for(int i=0;i<maximumSourceSentenceLength; i++){
			align4[i]=new float[maximumTargetSentenceLength][][];
			for(int j=0;j<maximumTargetSentenceLength;j++)
			{
				align4[i][j]=new float[maximumTargetSentenceLength][];
				for(int k=0;k<maximumTargetSentenceLength;k++)
					align4[i][j][k] = new float[maximumSourceSentenceLength];	
			}
		}
		
		while (sc1.hasNext()) {
			
			int sou = sc1.nextInt() , tar=sc1.nextInt(), tarLen = sc1.nextInt() , souLen = sc1.nextInt();
						
			float pro = sc1.nextFloat();
			
		
			float val = align4[sou][tar][tarLen][souLen];
			
			if (val<pro)
				align4[sou][tar][tarLen][souLen]=pro;
		}	
		
		sc1.close();

	}
	
	
	/***
		word: a word from the target language of the model.
	*/
	public PriorityQueue<Translation> getNoisyChannelTranslations(int word){

		if(st==null) throw new RuntimeException("No statistics object provided when building the model object.");
		
		PriorityQueue<Translation> pr = new PriorityQueue<Translation>();
		
		Iterator<Map.Entry<TranslationPair, Float>> iit = map.entrySet()
				.iterator();
		
		while (iit.hasNext()) {
			
			Map.Entry<TranslationPair, Float> pair = (Map.Entry<TranslationPair, Float>) iit
					.next();
			
			if ( pair.getKey().target == word && pair.getValue().doubleValue() > 0.1){
				
				Integer sCnt =  st.getUnigramCount(pair.getKey().source);
				double sProb = sCnt==null ? 0 : sCnt.intValue()/(double) st.unigramsCount;
				
				pr.add(new Translation(pair.getKey().source,
						pair.getKey().target, pair.getValue().doubleValue() * sProb));
			}
		}
		
		return pr;
		
	}

	/***
	word: a word from the source language of the model.
	*/
	public PriorityQueue<Translation> getDirectTranslations(int word){

	PriorityQueue<Translation> pr = new PriorityQueue<Translation>();
	
	Iterator<Map.Entry<TranslationPair, Float>> iit = map.entrySet()
			.iterator();
	
	while (iit.hasNext()) {
		
		Map.Entry<TranslationPair, Float> pair = (Map.Entry<TranslationPair, Float>) iit
				.next();
		
		if ( pair.getKey().source == word && pair.getValue().doubleValue() > 0.1){
			
			pr.add(new Translation(pair.getKey().source, pair.getKey().target, pair.getValue().doubleValue()));
		}
	}
	return pr;
}

	public PriorityQueue<Translation> getInverseTranslations(int word){

		PriorityQueue<Translation> pr = new PriorityQueue<Translation>();
		
		Iterator<Map.Entry<TranslationPair, Float>> iit = map.entrySet()
				.iterator();
		
		while (iit.hasNext()) {
			
			Map.Entry<TranslationPair, Float> pair = (Map.Entry<TranslationPair, Float>) iit
					.next();
			
			if ( pair.getKey().target==word && pair.getValue().doubleValue() > 0.1){
				
				pr.add(new Translation(pair.getKey().source, pair.getKey().target, pair.getValue().doubleValue()));
			}
		}
		return pr;
	}
	
	public double getAlignmentProb(int sou,int tar,int tarLen,int souLen){
		return align4[sou][tar][tarLen][souLen];
	}
	
	public double getTranslationProb(int source , int target){
		TranslationPair pair = new TranslationPair(source, target);
		Float val = map.get(pair);
		return val==null? 0 : val.doubleValue();
	}
	

	//  p(source|target) 	=	 p(target|source)*p(source)/...
	public double getNoisyChannelTranslationProb(int originalSource , int originalTarget){
		if(st==null) throw new RuntimeException("No statistics object provided when building the model object.");
		TranslationPair pair = new TranslationPair(originalSource, originalTarget);
		Float val = map.get(pair);
		
		Integer sCnt =  st.getUnigramCount(originalSource);
		double sProb = sCnt==null ? 0 : sCnt.intValue()/(double) st.unigramsCount;
		
		return val==null? 0 : val.doubleValue()*sProb;
	}
	
	
	public float[][][][] getAlignmentModel(){
		return align4;
		
	}

}
