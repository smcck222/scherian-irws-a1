package scherian;

public class Main {

    public static void main(String[] args) {

        // Starting point.
        try {
            CreateIndex indexer = new CreateIndex();
            indexer.create_index();
            System.out.println("Finished indexing process");

            QuerySearch searcher = new QuerySearch();
            searcher.query_search();
            System.out.println("Finished querying process");

        } catch (Exception e) {
            System.out.println(e.getClass());
        }
    }
}
