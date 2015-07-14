package model2Alignment;

public class Alignment3 extends Object{
	 public byte tarIndex;
	 public byte sourceLength;
	 public byte targetLength;
	 
	 public Alignment3(int tar,int targetLen,int sourceLen){
		 if(sourceLen>127 || targetLen>127)
			 throw new RuntimeException("alignment variable > 127");
		 this.tarIndex  = (byte)tar;
		 this.targetLength = (byte)targetLen;
		 this.sourceLength = (byte)sourceLen;
	 }
	 
	 public boolean equals(Object obj) {
		 Alignment3 a = (Alignment3) obj;
		 return tarIndex == a.tarIndex && sourceLength == a.sourceLength && targetLength == a.targetLength;
	 }	
	 
	 public int hashCode() {
		return (tarIndex+""+sourceLength+""+targetLength).hashCode();
	}
}
