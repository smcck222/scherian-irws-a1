package scherian;

import java.io.IOException;
import java.util.ArrayList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import sun.jvm.hotspot.types.CIntegerField;

public class QuerySearch
{
	// Directory where the index from CreateIndex are saved.
	private static String INDEX_DIRECTORY = "index";

	public static void query_search() throws IOException, ParseException
	{
		// Directory with the CRAN query data.
		final Path CRAN_QRY_DIR = Paths.get("cran/cran.qry");

		// Opening reader at the index path.
		IndexReader ireader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
		IndexSearcher isearcher = new IndexSearcher(ireader);

		// Analyzer that is used to process TextField
		//Analyzer analyzer = new StandardAnalyzer();
		// Use more of these later //
		//Analyzer analyzer = new SimpleAnalyzer();
		//Analyzer analyzer = new WhitespaceAnalyzer();
		//Analyzer analyzer = new StopAnalyzer();
		Analyzer analyzer = new EnglishAnalyzer();

		// Similarity measure.
		isearcher.setSimilarity(new BM25Similarity());    // Vector space + BM25.
		// Use more of these later.
		//isearcher.setSimilarity(new ClassicSimilarity());   // Vector space TFID.
		//isearcher.setSimilarity(new LMDirichletSimilarity());
		//isearcher.setSimilarity(new BooleanSimilarity());
		//isearcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(), new ClassicSimilarity()}));
		//isearcher.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(), new LMDirichletSimilarity()}));
		//isearcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(), new LMDirichletSimilarity()}));


		String result_file_path = "final_result.txt";
		PrintWriter iwriter = new PrintWriter(result_file_path, "UTF-8");

		//Reading from the QRY file.
		BufferedReader br = Files.newBufferedReader(CRAN_QRY_DIR);

		MultiFieldQueryParser mfqparser = new MultiFieldQueryParser(new String[]{"title", "author", "content"}, analyzer);
		//QueryParser qparser = new QueryParser("content",analyzer);

		String line = br.readLine();
		System.out.printf("Reading queries from \"%s\"\n", CRAN_QRY_DIR);

		String id = ""; // Creating our own ID because the IDs in cran.qry are not in order.
		int n = 0;
		String query_data = "";

		while(line!=null) {
			n = n + 1;
			if (line.startsWith(".I")) { //ID no.
				id = Integer.toString(n);
				line = br.readLine();
			}
			if (line.startsWith(".W")) { // Content of query.
				line = br.readLine();
				while (line!=null && !line.startsWith(".I")) { //Read till the next ID or till end of file (for last query).
					query_data += line + " ";
					line = br.readLine();
				}
			}
			query_data = query_data.trim(); // Remove spaces in beg and end.
			query_data = query_data.replace("?", "");  // Remove '?' marks as Lucene threw an error because it's a WildcardQuery character.

			Query query = mfqparser.parse(QueryParser.escape(query_data));
			//Query query = qparser.parse(query_data);

			// Searching.

			// Supplying the query to the searcher.
			TopDocs qry_results = isearcher.search(query,1000);      // Returning 1000 hits.
			ScoreDoc[] hits = qry_results.scoreDocs; // All relevant documents.

			// Writing into the results.txt file.
			// This needs to be in the format for trec_eval
			// query_id, Q0, document_id, rank, score, STANDARD
			// System.out.println(hits.length);
			for (int i = 0; i < hits.length; ++i) { // 225 queries => 1000 hits. Results file - 225*1000
				Document doc = isearcher.doc(hits[i].doc);
				iwriter.println(Integer.parseInt(id) + " 0 " + doc.get("id") + " " + i + " " + hits[i].score + " STANDARD");
			}
			query_data = "";
		}

		System.out.printf("Completed search, results in: \"%s\"\n", result_file_path);
		// Close everything and quit.
		iwriter.close();
		ireader.close();

	}
}

