package com.binder.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.fragment.app.Fragment
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

class Step5BooksFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var selectedBooksContainer: LinearLayout
    private lateinit var selectedBooksLabel: TextView
    private lateinit var nextButton: Button
    private lateinit var questionText: TextView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var searchResultsContainer: LinearLayout
    
    private val selectedBooks = mutableListOf<Book>()
    private val searchResults = mutableListOf<Book>()
    private lateinit var searchAdapter: BookSearchAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step5_books, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        questionText = view.findViewById(R.id.questionText)
        selectedBooksContainer = view.findViewById(R.id.selectedBooksContainer)
        selectedBooksLabel = view.findViewById(R.id.selectedBooksLabel)
        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchButton)
        resultsRecyclerView = view.findViewById(R.id.resultsRecyclerView)
        searchResultsContainer = view.findViewById(R.id.searchResultsContainer)
        nextButton = view.findViewById(R.id.nextButton)
        
        // Animate question
        val questionAnim = AlphaAnimation(0f, 1f).apply {
            duration = 800
            fillAfter = true
        }
        questionText.startAnimation(questionAnim)
        
        // Next button starts visible but disabled
        nextButton.isEnabled = false
        
        // Set up search results RecyclerView with checkboxes
        searchAdapter = BookSearchAdapter(selectedBooks) { book ->
            if (selectedBooks.size < 3) {
                if (!selectedBooks.any { it.id == book.id }) {
                    selectedBooks.add(book)
                    updateSelectedBooksUI()
                    checkCanProceed()
                    searchAdapter.notifyDataSetChanged() // Refresh to show checkmarks
                } else {
                    // Remove if already selected
                    selectedBooks.removeAll { it.id == book.id }
                    updateSelectedBooksUI()
                    checkCanProceed()
                    searchAdapter.notifyDataSetChanged()
                }
            } else {
                if (selectedBooks.any { it.id == book.id }) {
                    // Remove if already selected
                    selectedBooks.removeAll { it.id == book.id }
                    updateSelectedBooksUI()
                    checkCanProceed()
                    searchAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "You can only select 3 books", Toast.LENGTH_SHORT).show()
                }
            }
        }
        resultsRecyclerView.layoutManager = LinearLayoutManager(context)
        resultsRecyclerView.adapter = searchAdapter
        
        // Search button click
        searchButton.setOnClickListener {
            searchBooks()
        }
        
        // Search on Enter key
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                searchBooks()
                true
            } else {
                false
            }
        }
        
        nextButton.setOnClickListener {
            if (selectedBooks.size == 3) {
                activity.updateBooks(selectedBooks)
                activity.goToNextStep()
            }
        }
        
        updateSelectedBooksUI()
    }
    
    private fun searchBooks() {
        val query = searchInput.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(context, "Please enter a book title or author", Toast.LENGTH_SHORT).show()
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
                    searchButton.text = "Search"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to search: ${e.message}", Toast.LENGTH_SHORT).show()
                    searchButton.isEnabled = true
                    searchButton.text = "Search"
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
            
            searchResults.clear()
            searchResults.addAll(books)
            searchAdapter.updateBooks(books)
            searchResultsContainer.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(context, "Error parsing results: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateSelectedBooksUI() {
        selectedBooksContainer.removeAllViews()
        selectedBooksLabel.text = "Selected Books (${selectedBooks.size}/3)"
        
        if (selectedBooks.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No books selected yet. Search and tap to add books."
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_white, null))
                alpha = 0.7f
                setPadding(16, 16, 16, 16)
            }
            selectedBooksContainer.addView(emptyText)
        } else {
            selectedBooks.forEach { book ->
                val bookView = layoutInflater.inflate(R.layout.item_selected_book, selectedBooksContainer, false)
                val titleText = bookView.findViewById<TextView>(R.id.selectedBookTitle)
                val authorText = bookView.findViewById<TextView>(R.id.selectedBookAuthor)
                val removeButton = bookView.findViewById<Button>(R.id.removeButton)
                
                titleText.text = book.title
                authorText.text = "by ${book.author}"
                
                removeButton.setOnClickListener {
                    selectedBooks.remove(book)
                    updateSelectedBooksUI()
                    checkCanProceed()
                    searchAdapter.notifyDataSetChanged()
                }
                
                selectedBooksContainer.addView(bookView)
            }
        }
    }
    
    private fun checkCanProceed() {
        val canProceed = selectedBooks.size == 3
        nextButton.isEnabled = canProceed
    }
    
    private class BookSearchAdapter(
        private val selectedBooks: List<Book>,
        private val onBookToggled: (Book) -> Unit
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
            private val checkBox = itemView.findViewById<CheckBox>(R.id.bookCheckbox)
            
            fun bind(book: Book) {
                titleText.text = book.title
                authorText.text = "by ${book.author}"
                
                val isSelected = selectedBooks.any { it.id == book.id }
                checkBox.isChecked = isSelected
                
                itemView.setOnClickListener {
                    onBookToggled(book)
                }
                
                checkBox.setOnClickListener {
                    onBookToggled(book)
                }
            }
        }
    }
}
