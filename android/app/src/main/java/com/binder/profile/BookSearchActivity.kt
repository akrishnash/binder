package com.binder.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.binder.R
import com.binder.models.Book
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder

class BookSearchActivity : AppCompatActivity() {
    
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var adapter: BookSearchAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_search)
        
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)
        
        adapter = BookSearchAdapter { book ->
            val resultIntent = Intent().apply {
                putExtra("book", book)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = adapter
        
        searchButton.setOnClickListener {
            searchBooks()
        }
    }
    
    private fun searchBooks() {
        val query = searchInput.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a book title or author", Toast.LENGTH_SHORT).show()
            return
        }
        
        searchButton.isEnabled = false
        searchButton.text = "Searching..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = URL("https://openlibrary.org/search.json?q=$encodedQuery&limit=20")
                val response = url.readText()
                
                withContext(Dispatchers.Main) {
                    parseSearchResults(response)
                    searchButton.isEnabled = true
                    searchButton.text = getString(R.string.search)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookSearchActivity, "Failed to search: ${e.message}", Toast.LENGTH_SHORT).show()
                    searchButton.isEnabled = true
                    searchButton.text = getString(R.string.search)
                }
            }
        }
    }
    
    private fun parseSearchResults(json: String) {
        try {
            val gson = Gson()
            val jsonObject = gson.fromJson(json, JsonObject::class.java)
            val docs = jsonObject.getAsJsonArray("docs")
            
            val books = mutableListOf<Book>()
            for (i in 0 until docs.size()) {
                val doc = docs.get(i).asJsonObject
                val title = doc.get("title")?.asString ?: "Unknown Title"
                val author = doc.getAsJsonArray("author_name")?.get(0)?.asString ?: "Unknown Author"
                val coverId = doc.get("cover_i")?.asInt
                val bookId = doc.get("key")?.asString ?: "unknown"
                
                books.add(Book(
                    id = bookId,
                    title = title,
                    author = author,
                    coverId = coverId,
                    coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
                ))
            }
            
            adapter.updateBooks(books)
        } catch (e: Exception) {
            Toast.makeText(this, "Error parsing results: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private class BookSearchAdapter(
        private val onBookSelected: (Book) -> Unit
    ) : RecyclerView.Adapter<BookSearchAdapter.ViewHolder>() {
        
        private val books = mutableListOf<Book>()
        
        fun updateBooks(newBooks: List<Book>) {
            books.clear()
            books.addAll(newBooks)
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_book_search_result, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(books[position])
        }
        
        override fun getItemCount() = books.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleText = itemView.findViewById<TextView>(R.id.bookTitle)
            private val authorText = itemView.findViewById<TextView>(R.id.bookAuthor)
            
            fun bind(book: Book) {
                titleText.text = book.title
                authorText.text = "by ${book.author}"
                itemView.setOnClickListener {
                    onBookSelected(book)
                }
            }
        }
    }
}
