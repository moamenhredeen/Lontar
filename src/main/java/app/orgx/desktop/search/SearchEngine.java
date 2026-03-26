package app.orgx.desktop.search;

import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SearchEngine implements Closeable {

    private final ByteBuffersDirectory directory;
    private final StandardAnalyzer analyzer;
    private final IndexWriter writer;

    public SearchEngine() throws IOException {
        this.directory = new ByteBuffersDirectory();
        this.analyzer = new StandardAnalyzer();
        this.writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));
    }

    public void rebuildIndex(Vault vault) throws IOException {
        writer.deleteAll();
        for (var note : vault.notes().values()) {
            writer.addDocument(toDocument(note));
        }
        writer.commit();
    }

    public void updateNote(Note note) throws IOException {
        writer.updateDocument(new Term("path", note.path().toString()), toDocument(note));
        writer.commit();
    }

    public void deleteNote(Path path) throws IOException {
        writer.deleteDocuments(new Term("path", path.toString()));
        writer.commit();
    }

    public List<SearchResult> searchByTitle(String query, int maxResults) throws Exception {
        try (var reader = DirectoryReader.open(directory)) {
            var searcher = new IndexSearcher(reader);

            // Fuzzy query on title for typo tolerance
            var fuzzyQuery = new FuzzyQuery(new Term("title", query.toLowerCase()), 2);

            // Also try prefix query for partial matches
            var prefixQuery = new PrefixQuery(new Term("title", query.toLowerCase()));

            // Combine with OR
            var combined = new BooleanQuery.Builder()
                    .add(fuzzyQuery, BooleanClause.Occur.SHOULD)
                    .add(prefixQuery, BooleanClause.Occur.SHOULD)
                    .build();

            var topDocs = searcher.search(combined, maxResults);
            return toResults(searcher, topDocs, null);
        }
    }

    public List<SearchResult> searchContent(String queryStr, int maxResults) throws Exception {
        try (var reader = DirectoryReader.open(directory)) {
            var searcher = new IndexSearcher(reader);
            var queryParser = new QueryParser("content", analyzer);
            var query = queryParser.parse(queryStr);

            var topDocs = searcher.search(query, maxResults);

            // Highlight matching snippets
            var highlighter = UnifiedHighlighter.builder(searcher, analyzer).build();
            var snippets = highlighter.highlight("content", query, topDocs);

            return toResults(searcher, topDocs, snippets);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
        directory.close();
    }

    private Document toDocument(Note note) {
        var doc = new Document();
        doc.add(new StringField("path", note.path().toString(), Field.Store.YES));
        doc.add(new TextField("title", note.title().toLowerCase(), Field.Store.YES));
        doc.add(new StoredField("displayTitle", note.title()));
        doc.add(new TextField("content", note.content(), Field.Store.YES));
        doc.add(new LongField("modified", note.lastModified().toEpochMilli(), Field.Store.NO));
        return doc;
    }

    private List<SearchResult> toResults(IndexSearcher searcher, TopDocs topDocs, String[] snippets) throws IOException {
        var results = new ArrayList<SearchResult>();
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            var doc = searcher.storedFields().document(topDocs.scoreDocs[i].doc);
            var path = Path.of(doc.get("path"));
            var title = doc.get("displayTitle");
            var snippet = snippets != null && i < snippets.length ? snippets[i] : "";
            var score = topDocs.scoreDocs[i].score;
            results.add(new SearchResult(path, title, snippet, score));
        }
        return results;
    }
}
