package model2Alignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import storage.NumericConverter;

public class TranslationAndAlignmentTableCreater {

	
	private static int iterationsNum = 10;
	private static int trainingLineLengthLimit = 40 * 6;

	
	private Corpora co;
	private Statistics st;

	public TranslationAndAlignmentTableCreater(Corpora corpora, Statistics sta) {
		this.co = corpora;
		this.st = sta;
	}

	public  void buildTranslationAndAlignmentTables(String outputPath) throws FileNotFoundException,
			UnsupportedEncodingException {
		
		PrintWriter writer = new PrintWriter("debug.txt" , "UTF-8");
		for (int s = 0; s < iterationsNum; s++) {

			st.resetCounts();

			for (int k = 0; k < co.count; k++) {

				if (k % 25000 == 0)
					System.out.println("Iteration " + s
							+ " sentence " + k);

				String sou =  "-1 " + co.getSource(k);
				String tar =  co.getTranslate(k);

				String[] wSou = divideString(sou);
				int Ls = wSou.length;
				String[] wTar = divideString(tar);
				int Lt = wTar.length;

				
				// j scans the target sentence words
				for(int j=0;j<Lt;j++){
					
					int w_tar_j_int = Integer.parseInt(wTar[j]); 
					
					double s_total_ej=0;
					
					for(int i=0;i<Ls;i++){
						int w_sou_i_int = Integer.parseInt(wSou[i]);
						s_total_ej += st.getT(w_tar_j_int,w_sou_i_int)*st.getQ(i, j, Lt, Ls);
					}
				
					for(int i=0;i<Ls;i++){
						
						int w_sou_i_int = Integer.parseInt(wSou[i]);
						
						double c = st.getT(w_tar_j_int,w_sou_i_int )*st.getQ(i,j,Lt,Ls)/s_total_ej ;
						st.incrementTransCount(w_tar_j_int, w_sou_i_int, c);
						st.incrementWordCount(w_sou_i_int, c);
						st.incrementAlign4Count(i,j,Lt,Ls,c);
						st.incrementAlign3Count(j, Lt, Ls, c);
					}
				}
			}

			Iterator ite = st.transCount.entrySet().iterator();
			while(ite.hasNext()){
				Map.Entry<TranslationPair, Float> itpair = (Map.Entry<TranslationPair, Float>) ite.next();
				TranslationPair pair = itpair.getKey();
				Float value = itpair.getValue();
				
				st.setT(pair.target,pair.source, 
						value==null? 0 : value.floatValue()/st.getWordCount(pair.source));
				
			}
			
			float [][][][] al4 = st.align4Count;
			for(int i=0;i<al4.length; i++)
				for(int j=0;j<al4[i].length; j++)
					for(int Lt=0;Lt<al4[i][j].length; Lt++)
						for(int Ls=0;Ls<al4[i][j][Lt].length; Ls++){
							st.setQ(i, j, Lt, Ls, (al4[i][j][Lt][Ls]==0 )? 
									( 0 ) : (al4[i][j][Lt][Ls] /st.getAlign3Count(j, Lt, Ls)) );
						}
			
			while(ite.hasNext()){
				Map.Entry<Alignment4, Float> itpair = (Map.Entry<Alignment4, Float>) ite.next();
				Alignment4 align = itpair.getKey();
				Float value = itpair.getValue();
				
				st.setQ(align.sourceIndex, align.targetIndex, align.targetLength, align.sourceLength, value==null?
						0 : value.floatValue()/st.getAlign3Count(align.targetIndex, align.targetLength, align.sourceLength));
		    }
			
			if(s==iterationsNum-1){
				storeModels(st,outputPath);
			}
		}

	}
	
	
	public void storeModels(Statistics st,String outputPath) throws FileNotFoundException, UnsupportedEncodingException{
		
		PrintWriter writer = new PrintWriter(outputPath + "Trans.txt", "UTF-8");
		
		Iterator ite = st.t.entrySet().iterator();
		
		while(ite.hasNext()){
			Map.Entry<TranslationPair, Float> itpair = (Map.Entry<TranslationPair, Float>) ite.next();
			TranslationPair pair = itpair.getKey();
			Float value = itpair.getValue();
			writer.println(pair.source + " " + pair.target + " " + (value==null?0:value.doubleValue()));
		}
		
		writer.close();
		writer = new PrintWriter(outputPath + "Align.txt", "UTF-8");
		

		float [][][][] q = st.q;
		for(int i=0;i<q.length; i++)
			for(int j=0;j<q[i].length; j++)
				for(int Lt=0;Lt<q[i][j].length; Lt++)
					for(int Ls=0;Ls<q[i][j][Lt].length; Ls++){
						if(q[i][j][Lt][Ls]!=0)
							writer.println("" + i + " " + j + " " + Lt + " " + Ls + " " + q[i][j][Lt][Ls] );
					}
		writer.close();
	}


	public void storeReadableTranslationModel(Statistics st,String outputPath , NumericConverter con) throws FileNotFoundException, UnsupportedEncodingException{
		
		PrintWriter writer = new PrintWriter(outputPath + "Trans.txt", "UTF-8");
		
		Iterator ite = st.t.entrySet().iterator();
		
		while(ite.hasNext()){
			Map.Entry<TranslationPair, Float> itpair = (Map.Entry<TranslationPair, Float>) ite.next();
			TranslationPair pair = itpair.getKey();
			Float value = itpair.getValue();
			writer.println(
					con.getWordOfSourceHash(pair.source) + " " + con.getWordOfTargetHash(pair.target)
					+ " " + (value==null?0:value.doubleValue()));
		}
		
		writer.close();
		
	}


	public static String[] divideString(String str) {
		str = str.trim();
		return str.split(" ");
	}

}
