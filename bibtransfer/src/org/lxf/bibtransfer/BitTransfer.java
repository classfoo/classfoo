package org.lxf.bibtransfer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.Key;
import org.jbibtex.ParseException;
import org.jbibtex.Value;

/**
 * 转换bib文件，提取其中作者关联信息
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-12-3
 */
public class BitTransfer implements Serializable {

	private static final long serialVersionUID = 2975563815488244613L;

	public static void main(String[] args) throws IOException, ParseException {

		InputStreamReader is = new InputStreamReader(BitTransfer.class.getResourceAsStream("2012.bib"), "UTF-8");
		try {
			Reader newis = BibTransFormater.formatBit(is);
			try {
				BibTeXParser parser = new BibTeXParser();
				BibTeXDatabase db = parser.parse(newis);
				Map<Key, BibTeXEntry> entries = db.getEntries();
				BufferedWriter bw = new BufferedWriter(new FileWriter("out.txt"));
				try {
					for (Key key : entries.keySet()) {
						BibTeXEntry entry = entries.get(key);
						Value field = entry.getField(BibTeXEntry.KEY_AUTHOR);
						String ustr = field.toUserString().replaceAll("\n", " ");
						System.out.println("作者：" + ustr);
						String[] arr = ustr.split(" and ");
						int length = arr.length;
						for (int i = 0; i < length; i++) {
							for (int j = i + 1; j < length; j++) {
								String p1 = arr[i].trim();
								String p2 = arr[j].trim();
								StringBuilder sb = new StringBuilder(p1.length() + p2.length() + 5);
								sb.append(p1).append("\t").append(p2).append("\t").append("1\n");
								bw.write(sb.toString());
								System.out.print(sb.toString());
							}
						}
						/*for(int i=0;i<arr.length;i++)
								System.out.println("作者："+arr[i]);*/

						/*Value title = entry.getField(BibTeXEntry.KEY_TITLE);
						System.out.println("文章：" + title.toUserString());*/
					}
				}
				finally {
					bw.close();
				}
			}
			finally {
				newis.close();
			}
		}
		finally {
			is.close();
		}
	}
}
