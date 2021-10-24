CS7IS3 - Information Retrieval and Web Search
Individual Assignment - 1
Lucene Based Search Engine 

WHAT THIS REPO CONTAINS:

1. The Java scripts are in the src/java/ folder, there are 3 files: 
   - Main.java - Starting point of execution.
   - CreateIndex.java - Indexes the 1400 documents in the cran.all.1400 collection to the index folder. 
   - QuerySearch.java - Reads the 225 queries from cran.qry, parses it and searches the index to return 1000 hits per query and write results into the result.txt file.

2. results
This folder contains the results from QuerySearch in the format for trec_eval : query_id, Q0, document_id, rank, score, STANDARD 
There are different files for different similarity measures + analyzers. Refer to the README.md inside the results folder for more info.

4. trec_eval 
Already made. Also includes the final_result.txt file and the qrelcorrected file.

5. pom.xml 
Contains required dependencies to be installed before running the java code (includes Lucene version - 8.9.0). 

6. final_result.txt 
Result file with the highest MaP score (English Analyzer + BM25). A copy of this is also in the trec_eval folder.

TO EXECUTE: 

The following commands: 

mvn clean && mvn install

mvn exec:java -Dexec.mainClass="Main"

TO RUN TREC_EVAL: 

cd trec_eval
./trec_eval qrelcorrected final_result.txt

For recall scores: 
./trec_eval -m recall qrelcorrected final_result.txt
