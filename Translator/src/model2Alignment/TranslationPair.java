package model2Alignment;


public class TranslationPair extends Object{
	public int source;
	public int target;
	
	public TranslationPair(int source,int target){
		this.source = source;
		this.target = target;
	}
	
	public boolean equals(Object obj){
		TranslationPair p = (TranslationPair) obj;
		return source==p.source && target==p.target;
	}
	
	public int hashCode(){
		return ("" + source+target).hashCode();
	}
	
}