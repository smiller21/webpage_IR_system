package webpage_IR_package;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Webpage_IR_System {

	private static int max_crawldepth = 0;
	private static IndexWriter w;
	
	public static org.apache.lucene.document.Document getLuceneDocument(Document jdoc) throws Exception {

		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

		Field title = new TextField("title", jdoc.getElementsByTag("title").text(), Store.YES);
		//Field path = new TextField("path", .getAbsolutePath(), Store.YES);
		Field body = new TextField("body", jdoc.getElementsByTag("body").text(), Store.YES);

		doc.add(title);
		doc.add(body);
		//doc.add(path);

		System.out.println("'" + jdoc.baseUri() + "'" + " indexed");

		return doc;
	}
	
	public static void crawlpage(String url, int crawldepth) throws Exception {
		final  ArrayList<String> result = new ArrayList<String>();
		System.out.println("crawl depth: " + crawldepth);
		Document doc;
		doc = Jsoup.connect(url).get();
		Elements links = doc.select("a[href]");
	    for (Element link : links) {
	        result.add(link.attr("abs:href"));
	    }
/*	    for(int i = 0; i<crawldepth; i++) {
	    	System.out.print("\t");
	    }
	    */
		System.out.println(result);
		if(crawldepth+1 > max_crawldepth) {
			System.out.println("reached maximum crawldepth - crawling stopped");
			return;
		}
		else {
			for (String res : result) {
				org.apache.lucene.document.Document ludoc = getLuceneDocument(doc);
				w.addDocument(ludoc);
				crawlpage(res,crawldepth+1);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.out.println("Please type all four required arguments");
			System.exit(-1);
		}
		String seedURL = args[0];
		max_crawldepth = Integer.parseInt(args[1]);
		File index_file = new File(args[2]);
		Directory index_dir = FSDirectory.open(index_file.toPath());
		String queryStr = "";
		for (int i = 3; i < args.length; i++)
			queryStr += args[i] + " ";
		
		EnglishAnalyzer analyzer = new EnglishAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		w = new IndexWriter(index_dir, config);
		
		crawlpage(seedURL,0);
		w.close();
		System.out.println("Indexing finished");
	}
	
	/*TODO:
	- URL normalization
	- safe URLs to text file (pages.txt)
	- check if page already visited 
	- use existing index or create one
	- add ranking
	*/
}
