package model2Alignment;


public class Alignment4 extends Object{
	 public byte sourceIndex;
	 public byte targetIndex;
	 public byte sourceLength;
	 public byte targetLength;
	 
	 public Alignment4(int sou,int tar,int targetLen,int sourceLen){
		 if(sourceLen>127 || targetLen>127)
			 throw new RuntimeException("alignment variable > 127");
		 this.sourceIndex = (byte)sou;
		 this.targetIndex = (byte)tar;
		 this.targetLength = (byte)targetLen;
		 this.sourceLength = (byte)sourceLen;
	 }
	 
	 public boolean equals(Object obj) {
		 Alignment4 a = (Alignment4) obj;
		 return sourceIndex == a.sourceIndex && targetIndex == a.targetIndex && sourceLength == a.sourceLength && targetLength == a.targetLength;
	 }	
	 
	 public int hashCode() {
		return (sourceIndex+""+targetIndex+""+sourceLength+""+targetLength).hashCode();
	}
}
