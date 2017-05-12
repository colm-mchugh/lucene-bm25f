package com.o19s.bm25f;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.BlendedTermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.Set;

/**
 * Created by doug on 10/11/16.
 */
public class BM25FIndexBuilder {

    private final static String ID = "id";
    private final static String HL = "headline";
    private final static String DS = "description";
    
    private static void addDoc(IndexWriter w, BM25FIndexData.RawDoc d) throws IOException {
        Document doc = new Document();
        doc.add(new TextField(ID, d.id, Field.Store.YES));
        doc.add(new TextField(HL, (d.headline == null? "" : d.headline), Field.Store.YES));
        doc.add(new TextField(DS, (d.description == null? "" : d.description), Field.Store.YES));
        w.addDocument(doc);
    }

    static Similarity perFieldSimilarities = new PerFieldSimilarityWrapper() {
        @Override
        public Similarity get(String name) {
            if (name.equals(HL)) {
                return new BM25FSimilarity(/*k1*/1.2f, /*b*/ 0.8f);
            } else if (name.equals(DS)) {
                return new BM25FSimilarity(/*k1*/1.4f, /*b*/ 0.9f);
            }
            return new BM25FSimilarity();
        }
    };

    public static void main(String[] argv)  throws IOException {
        // lots of boilerplate from http://www.lucenetutorial.com/lucene-in-5-minutes.html
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(perFieldSimilarities);
        IndexWriter w = new IndexWriter(index, config);

        Set<BM25FIndexData.RawDoc> docs = BM25FIndexData.getDocs();
        for (BM25FIndexData.RawDoc doc : docs) {
            addDoc(w, doc);
        }
        w.close();
        
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        Set<BM25FIndexData.SRQuery> qries = BM25FIndexData.getQrys();
        for (BM25FIndexData.SRQuery qry : qries) {
            BlendedTermQuery bm25fQuery = new BlendedTermQuery.Builder()
                    .add(new Term(HL, qry.problemDetails), 2.0f)
                    .add(new Term(DS, qry.problemDetails))
                    .setRewriteMethod(BlendedTermQuery.BOOLEAN_REWRITE)
                    .build();

            TopDocs srchDocs = searcher.search(bm25fQuery, 10);
            ScoreDoc[] hits = srchDocs.scoreDocs;

            for (int i = 0; i < Integer.min(3, hits.length); ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                StringBuilder sb = new StringBuilder();
                sb.append(qry.srId).append(", \"").append(qry.problemDetails).append("\", ");
                sb.append(d.get(ID)).append(", \"").append(d.get(HL)).append("\", \"");
                sb.append(d.get(DS)).append("\"");
                System.out.println(sb.toString());
            }
        }
    }

}
