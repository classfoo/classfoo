package org.lxf.bibtransfer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public class BibTransFormater {

	/**
	 * 读取一个bib文件，清理非法内容
	 * @param r
	 * @return
	 * @throws IOException 
	 */
	public static Reader formatBit(Reader r) throws IOException{
		StringWriter sw = new StringWriter();
		try{
			char[] buffer = new char[512];
			int len = r.read(buffer);
			if(len == -1){
				throw new RuntimeException("文件为空");
			}
			boolean meetat = false;
			for(int i = 0;i < len;i++){
				char c = buffer[i];
				if(!meetat){
					if(c=='@'){//magic:@articles是文件开头，前面可能存在非法字符
						meetat = true;
					}else{
					continue;
				}
				}
				sw.write(c);
			}
			while((len =r.read(buffer))!=-1){
				for(int i = 0;i < len;i++){
					sw.write(buffer[i]);
				}
			}
			String str = sw.getBuffer().toString();
			str = str.replaceAll("Meeting Abstract", "Metting-Abstract");
			return new StringReader(str);
		}finally{
			sw.close();
		}
	}
}
