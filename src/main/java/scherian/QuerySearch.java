package scherian;

import java.io.IOException;
import java.util.ArrayList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.search.similarities.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class CreateIndex
{
	// Directory where the search index will be saved. 
	private static String INDEX_DIRECTORY = "index";


	public static void create_index() throws IOException
	{
		// Directory with the CRAN data.
		final Path CRAN_ALL_DIR = Paths.get("cran/cran.all.1400");
		
		if(!Files.isReadable(CRAN_ALL_DIR)){
			System.out.println("This directory (" + CRAN_ALL_DIR.toAbsolutePath() + ") does not exist");
			System.exit(1);
		}

		System.out.printf("Indexing \"%s\"\n", CRAN_ALL_DIR);

		// Open the indexing directory.
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

		//Analyzer that is used to process TextField
		//Analyzer analyzer = new StandardAnalyzer();
		// Use more of these later //
		//Analyzer analyzer = new SimpleAnalyzer();
		//Analyzer analyzer = new WhitespaceAnalyzer();
		//Analyzer analyzer = new StopAnalyzer();
		Analyzer analyzer = new EnglishAnalyzer();
		// Set up an index writer to add, process and save documents to the index
		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		// Similarity measure. 
		config.setSimilarity(new BM25Similarity());  // Vector space + BM25
		//config.setSimilarity(new ClassicSimilarity());  // Vector space TFID.
		//config.setSimilarity(new LMDirichletSimilarity());
		//config.setSimilarity(new BooleanSimilarity());
		//Combinations of similarity measures.
		//config.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(), new ClassicSimilarity()}));
		//config.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(), new LMDirichletSimilarity()}));
		//config.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(), new LMDirichletSimilarity()}));

		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		IndexWriter iwriter = new IndexWriter(directory, config);
		
		try(InputStream stream = Files.newInputStream(CRAN_ALL_DIR)){

			BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			System.out.println("Starting indexing..."); 

			String line = br.readLine(); 
			String field_data = ""; 

			while(line != null){

				Document doc = new Document();
				if(line.startsWith(".I")){ // ID of document.
					System.out.println("Indexing document " + line.substring(3));
					doc.add(new StringField("id", line.substring(3), Field.Store.YES));
					line = br.readLine();
				}
				if(line.startsWith(".T")){ // Title of the document.
					line = br.readLine(); 
					while(!line.startsWith(".A")){ // Reading till Author.
						field_data += line + " ";
						line = br.readLine();
					}
					doc.add(new TextField("title", field_data, Field.Store.YES));
					field_data = "";  // Empty this for the next field.
				}
				if(line.startsWith(".A")){ // Author of document.
					line = br.readLine(); 
					while(!line.startsWith(".B")){ // Reading till bibliography.
						field_data += line + " ";
						line = br.readLine();
					}
					doc.add(new TextField("author", field_data, Field.Store.YES));
					field_data = ""; // Empty this for the next field.
				}
				while(!line.startsWith(".W")) // Reading till content. Ignore bibliography.
					line = br.readLine();

				if(line.startsWith(".W")) {
					// Content of the document.
					line = br.readLine();
					while (line != null && !line.startsWith(".I")) { // Reading till end of document/ next document.
						field_data += line + " ";
						//System.out.println(field_data);
						line = br.readLine();
					}
					doc.add(new TextField("content", field_data, Field.Store.YES));
					field_data = ""; // Empty this for the next doc cycle.
				}
				// Check document before writing into index.
				//System.out.println("ID: " + doc.get("id"));
				//System.out.println("TITLE: " + doc.get("title"));
				//System.out.println("AUTHOR: " + doc.get("author"));
				//System.out.println("CONTENT: " + doc.get("content"));

				// Write document into the search index.
				iwriter.addDocument(doc);
			}// Repeat.


		}catch(IOException e) {
			System.out.println(("Exception"));
		}

		// Close everything and quit.
		//iwriter.forceMerge(1);
		iwriter.close();
	}
}
