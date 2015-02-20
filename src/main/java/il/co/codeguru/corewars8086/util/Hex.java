package il.co.codeguru.corewars8086.util;

public class Hex {
	static public String toHexString(int num, int width) {
		String txt = Integer.toHexString(num);

		int len = txt.length();
		if(len<width){
			StringBuffer sb = new StringBuffer();
			for(;len<width;len++){
				sb.append('0');
			}
			txt = sb.append(txt).toString();
		} else if(len>width){
			txt = txt.substring(len-width, len);
		}

		return txt;
	}

	static public String toHexStringUnsigned(int num, int width) {
		String txt = Integer.toHexString(num);

		int len = txt.length();
		if(len>width){
			txt = txt.substring(len-width, len);
		}
		return txt;
	}


	static public String toHexStringSigned(int num, int width) {
		boolean negative = false;

		if(num<0) {
			num=-num;
			negative = true;
		}

		String txt = Integer.toHexString(num);

		int len = txt.length();
		if(len>width){
			txt = txt.substring(len-width, len);
		}

		if(negative==true){
			txt="-0x" + txt;}
		else{
			txt="+0x" + txt;}

		return txt;
	}


	static public int getDigit(String line,int pos) throws Exception
	{
		int c = line.charAt(pos);

		if (c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		if (c >= '0' && c <= '9')
			return c - '0';

		throw new Exception("Invalid digit '"+line.charAt(pos)+"'");
	}

	static public int getByte(String line,int pos) throws Exception
	{
		return (getDigit(line,pos+0) << 4) | getDigit(line,pos+1);
	}

	static public int getWord(String line,int pos) throws Exception
	{
		return (getByte(line,pos+0) << 8) | getByte(line,pos+2);
	}
}
