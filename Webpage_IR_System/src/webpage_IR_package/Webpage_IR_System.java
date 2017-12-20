package webpage_IR_package;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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

	private static ArrayList<String> visitedURLs = new ArrayList<String>();
	private static ArrayList<Integer> depthList = new ArrayList<Integer>();
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
	
	public static void normalizeAndAddURL(String url) {
		if(url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
        if(!visitedURLs.contains(url)) {
        	visitedURLs.add(url.toLowerCase());
        }
	}
	
	public static void crawlpage(String url, int crawldepth) throws Exception {
		final  ArrayList<String> result = new ArrayList<String>();
		System.out.println("crawl depth: " + crawldepth);
		Document doc;
		doc = Jsoup.connect(url).get();
        normalizeAndAddURL(url);
        depthList.add(crawldepth);
		Elements links = doc.select("a[href]");
		if(crawldepth + 1 > max_crawldepth) {
			System.out.println("reached maximum crawldepth - crawling stopped");
			return;
		}
		else {
			for (Element link : links) {
		    	String linkURL = link.attr("abs:href");
		        result.add(linkURL);
		    }

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
		
		//TEMPORARY
		final File[] files = index_file.listFiles();
		for (File f: files) f.delete();
		
		String queryStr = "";
		for (int i = 3; i < args.length; i++)
			queryStr += args[i] + " ";
		
		EnglishAnalyzer analyzer = new EnglishAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		w = new IndexWriter(index_dir, config);
		while(!visitedURLs.isEmpty()) {
			visitedURLs.remove(0);
		}
		while(!depthList.isEmpty()) {
			depthList.remove(0);
		}
		
		crawlpage(seedURL,0);
		w.close();
		String pages = "";
		for(int i=0;i<visitedURLs.size();i++) {
			pages += visitedURLs.get(i) + "\t" + depthList.get(i) + "\n";
		}
		
		try {
			File file = new File(index_file + "/pages.txt");
			if(!file.exists()) {
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(pages);
			fileWriter.flush();
			fileWriter.close();
			System.out.println("saved url list to: " + index_file + "/pages.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		System.out.println("Indexing finished");
	}
	
	/*TODO:
	- Dont index that are already indexed
	- check if page already visited 
	- use existing index or create one
	- add ranking
	*/
}
