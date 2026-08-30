package com.example.domain.agent

import java.util.Locale
import kotlin.math.ln
import kotlin.math.sqrt

data class VectorDocument(
    val id: String,
    val text: String
)

class LocalVectorSearchEngine {
    private val stopWords = setOf(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", 
        "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", 
        "can", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", 
        "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", 
        "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", 
        "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", 
        "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", 
        "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", 
        "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", 
        "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", 
        "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", 
        "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", 
        "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", 
        "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", 
        "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "how", "many", 
        "calculate", "calculator", "estimate", "estimator", "need", "needed", "want"
    )

    private val documents = mutableListOf<VectorDocument>()
    private val docTermFreqs = mutableMapOf<String, Map<String, Double>>() // docId -> term -> termCount
    private val docLengths = mutableMapOf<String, Double>() // docId -> vectorMagnitude
    private val df = mutableMapOf<String, Int>() // term -> docCount

    fun indexDocuments(docs: List<VectorDocument>) {
        documents.clear()
        docTermFreqs.clear()
        docLengths.clear()
        df.clear()

        documents.addAll(docs)

        // Count DF & TF
        for (doc in docs) {
            val tokens = tokenize(doc.text)
            val termCounts = mutableMapOf<String, Double>()
            for (token in tokens) {
                termCounts[token] = termCounts.getOrDefault(token, 0.0) + 1.0
            }

            docTermFreqs[doc.id] = termCounts

            // Update Doc Frequency
            for (term in termCounts.keys) {
                df[term] = df.getOrDefault(term, 0) + 1
            }
        }

        // Calculate TF-IDF vectors & normalizations
        val numDocs = docs.size.toDouble()
        for (doc in docs) {
            val termCounts = docTermFreqs[doc.id] ?: emptyMap()
            var lengthSq = 0.0
            for ((term, count) in termCounts) {
                val idf = idf(term, numDocs)
                val tfIdf = count * idf
                lengthSq += tfIdf * tfIdf
            }
            docLengths[doc.id] = sqrt(lengthSq)
        }
    }

    private fun idf(term: String, numDocs: Double): Double {
        val count = df.getOrDefault(term, 0)
        if (count == 0) return 0.0
        return ln(numDocs / count.toDouble()) + 1.0
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase(Locale.ROOT)
            .replace("[^a-zA-Z0-9\\s]".toRegex(), " ")
            .split("\\s+".toRegex())
            .filter { it.length > 1 && !stopWords.contains(it) }
    }

    fun search(query: String, limit: Int = 4): List<String> {
        if (query.isBlank() || documents.isEmpty()) return emptyList()

        val queryTokens = tokenize(query)
        if (queryTokens.isEmpty()) {
            // fallback simple string containment search if query contains only stopwords
            val lower = query.lowercase(Locale.ROOT)
            return documents.map { doc ->
                val score = if (doc.text.lowercase(Locale.ROOT).contains(lower)) 1.0 else 0.0
                doc.id to score
            }.sortedByDescending { it.second }
                .filter { it.second > 0.0 }
                .take(limit)
                .map { it.first }
        }

        val queryTermCounts = mutableMapOf<String, Double>()
        for (token in queryTokens) {
            queryTermCounts[token] = queryTermCounts.getOrDefault(token, 0.0) + 1.0
        }

        val numDocs = documents.size.toDouble()
        val queryVector = mutableMapOf<String, Double>()
        var queryLengthSq = 0.0
        for ((term, count) in queryTermCounts) {
            val idf = idf(term, numDocs)
            val tfIdf = count * idf
            queryVector[term] = tfIdf
            queryLengthSq += tfIdf * tfIdf
        }
        val queryLength = sqrt(queryLengthSq)
        if (queryLength == 0.0) return emptyList()

        val results = mutableListOf<Pair<String, Double>>() // docId -> score

        for (doc in documents) {
            val docVector = docTermFreqs[doc.id] ?: emptyMap()
            val docLen = docLengths[doc.id] ?: 0.0
            if (docLen == 0.0) continue

            // Dot product
            var dotProduct = 0.0
            for ((term, qWeight) in queryVector) {
                val docTermWeight = docVector[term] ?: 0.0
                if (docTermWeight > 0.0) {
                    val idf = idf(term, numDocs)
                    val docTfIdf = docTermWeight * idf
                    dotProduct += qWeight * docTfIdf
                }
            }

            // Cosine Similarity
            val score = dotProduct / (queryLength * docLen)
            
            // Add a small boost for title match
            var finalScore = score
            if (doc.text.split(".")[0].lowercase(Locale.ROOT).contains(queryTokens.first())) {
                finalScore += 0.15
            }

            if (finalScore > 0.0) {
                results.add(doc.id to finalScore)
            }
        }

        return results.sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
}
