### CS7IS3 - Information Retrieval and Web Search
### Individual Assignment - 1
### Lucene Based Search Engine 

**WHAT THIS REPO CONTAINS:**

1. The Java scripts are in the src/main/java/scherian folder, there are 3 files: 
   - Main.java - Starting point of execution.
   - CreateIndex.java - Indexes the 1400 documents in the cran.all.1400 collection to the index folder. 
   - QuerySearch.java - Reads the 225 queries from cran.qry, parses it and searches the index to return 1000 hits per query and write results into the result.txt file.

2. results

This folder contains the results from QuerySearch in the format for trec_eval : query_id, Q0, document_id, rank, score, STANDARD 
There are different files for different similarity measures + analyzers. Refer to the README.md inside the results folder for more info.

3. pom.xml 

Contains required dependencies to be installed before running the java code (includes Lucene version - 8.10.0). 

4. final_result.txt 

Result file with the highest MaP score (English Analyzer + BM25).

**TO EXECUTE:**

Run the following commands:

1. mvn clean && mvn install
2. mvn package
3. java -jar target/Project1-1.0.jar

**TO RUN TREC_EVAL:**

For MaP scores:

trec_eval/trec_eval -m map qrelcorrected final_result.txt

For recall scores: 

trec_eval/trec_eval -m recall qrelcorrected final_result.txt
