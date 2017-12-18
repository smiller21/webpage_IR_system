package webpage_IR_package;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Webpage_IR_System {

	public static void main(String[] args) throws IOException {
		if (args.length < 4) {
			System.out.println("Please type all four required arguments");
			System.exit(-1);
		}
		
		String seedURL = args[0];
		int crawldepth = Integer.parseInt(args[1]);
		File index_file = new File(args[2]);
		Directory index_dir = FSDirectory.open(index_file.toPath());
		String queryStr = "";
		for (int i = 3; i < args.length; i++)
			queryStr += args[i] + " ";
		
	}

}
