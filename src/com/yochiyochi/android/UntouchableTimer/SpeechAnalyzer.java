package com.yochiyochi.android.UntouchableTimer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeechAnalyzer
{
	private static final String regexNum = "[0-9]+";	// 数字列を表現する正規表現、ほんとは [1-9][0-9]* かな。でも「03分」も救ってあげる
	private static final String regexExactHMS[] = { "時間", "分", "秒" };	// 正しい音声認識の場合の時間、分、秒
	private static final String regexHMS[] = { "("+regexExactHMS[0]+"|じかん|ジカン)", "("+regexExactHMS[1]+"|ふん|ぷん|フン|プン)", "("+regexExactHMS[2]+"|びょう|ビョウ)" };	// 時間、分、秒の音声誤認識となりそうなものをどんどん追加

	// 音声認識結果の文字列リストから最適な秒数値を取得する
	public static int speechToSecond(ArrayList<String> strList)
	{
		return getSecond(getCandidate(strList));
	}
	
	// 文字列から秒数値を取得する
	// 本当は private でよいが、TestTextToSecond サンプルアプリではキー入力の文字列を秒数値に変換しているので、呼び出し可能にしている
	public static int getSecond(String str)
	{
    	Matcher mch, mchN;

	    int result = 0;
    	if(valueOf(str) >= 0)	// 文字列が数字のみで構成されている場合、その数値は「分」を意味しているとみなして、秒数値を返す
    	{
			mchN = Pattern.compile(regexNum).matcher(str);
    		if(mchN.find())
    			result = Integer.valueOf(mchN.group())*60;
    	}
    	else					// 文字列が数字のみで構成されて「いない」場合、時間、分、秒を解析して、秒数値を返す
    	{
        	for(int i = 0; i < 3; i++)	// 時間、分、秒の順に処理する
        	{
        		result *= 60;
        		mch = Pattern.compile(regexNum + regexHMS[i]).matcher(str);
        		if(mch.find())
        		{
        			mchN = Pattern.compile(regexNum).matcher(mch.group());
        			if(mchN.find())
        				result += Integer.valueOf(mchN.group());
        		}
        	}
    	}
    	return result;
	}

	// 文字列リストの中から、時間情報として適切なものを取得する
	// 本当は private でよいが、TestTextToSecond サンプルアプリでは時間情報を取得した文字列を表示しているので、呼び出し可能にしている
	public static String getCandidate(ArrayList<String> strList)
	{
		String result;
		
		// 第1優先 : 文字列がもろに時間情報にマッチしている場合
		for(int i = 0; i < strList.size(); i++)
		{
			result = strList.get(i);
			if(isExactHMS(result))
				return result;
		}
		// 第2優先 : 多少の誤認識を許容して、時間情報らしきものにマッチした場合
		for(int i = 0; i < strList.size(); i++)
		{
			result = strList.get(i);
			if(isHMS(result))
				return result;
		}
		// 第3優先 : 文字列が数値のみで構成されている場合
		for(int i = 0; i < strList.size(); i++)
		{
			result = strList.get(i);
			if(valueOf(result) > 0)
				return result;
		}
		// 第4優先 : しょうがないから、音声認識で最もスコアの高いものを返す。でもこの文字列からは時間情報を取れないなぁ
		return strList.get(0);
	}

/* このメソッドは、デバッグ用です。どの優先順の候補が選択されたのかを確認するために。最終的には削除します。 */
	public static String getCandidate(ArrayList<String> strList, boolean debug)
	{
		String result;
		
		// 第1優先 : 文字列がもろに時間情報にマッチしている場合
		for(int i = 0; i < strList.size(); i++)
		{
			result = strList.get(i);
			if(isExactHMS(result))
				return result + " (第1優先)";
		}
		// 第2優先 : 多少の誤認識を許容して、時間情報らしきものにマッチした場合
		for(int i = 0; i < strList.size(); i++)
		{
			result = strList.get(i);
			if(isHMS(result))
				return result + " (第2優先)";
		}
		// 第3優先 : 文字列が数値のみで構成されている場合
		for(int i = 0; i < strList.size(); i++)
		{
			result = strList.get(i);
			if(valueOf(result) > 0)
				return result + " (第3優先)";
		}
		// 第4優先 : しょうがないから、音声認識で最もスコアの高いものを返す
		return strList.get(0) + " (第4優先)";
	}

	// 文字列がもろに時間情報にマッチしたら true を返す
	private static boolean isExactHMS(String str)
	{
		if(str != null && !str.equals(""))
			if(Pattern.compile("^("+regexNum+regexExactHMS[0]+")?\\s*("+regexNum+regexExactHMS[1]+")?\\s*("+regexNum+regexExactHMS[2]+")?$").matcher(str).find())
				return true;
		return false;
	}

	// 多少誤認識があるが、時間情報らしきものがあれば true を返す
	private static boolean isHMS(String str)
	{
		if(str != null && !str.equals(""))
			if(Pattern.compile(regexNum+regexHMS[0]).matcher(str).find())
				return true;
			else if(Pattern.compile(regexNum+regexHMS[1]).matcher(str).find())
				return true;
			else if(Pattern.compile(regexNum+regexHMS[2]).matcher(str).find())
				return true;
		return false;
	}
	
	// 文字列が数値のみで構成されていたら、その値 を返す、数値のみでなければ -1 を返す
	private static int valueOf(String str)
	{
		if(str != null && !str.equals(""))
		{
			Matcher mch = Pattern.compile("^"+regexNum+"$").matcher(str);
			if(mch.find())
				return Integer.valueOf(mch.group());
		}
		return (-1);
	}
}
