package com.binder.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
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

class Step3MysteryShelfFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var resultsList: LinearLayout
    private lateinit var selectedBooksContainer: LinearLayout
    private lateinit var selectedBooksTitle: TextView
    private lateinit var backButton: Button
    private lateinit var completeButton: Button
    
    private val selectedBooks = mutableListOf<Book>()
    private val gson = Gson()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step3, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchButton)
        resultsList = view.findViewById(R.id.resultsList)
        selectedBooksContainer = view.findViewById(R.id.selectedBooksContainer)
        selectedBooksTitle = view.findViewById(R.id.selectedBooksTitle)
        backButton = view.findViewById(R.id.backButton)
        completeButton = view.findViewById(R.id.completeButton)
        
        // Add focus listener to change EditText background
        searchInput.setOnFocusChangeListener { _, hasFocus ->
            searchInput.background = resources.getDrawable(
                if (hasFocus) R.drawable.edit_text_background_focused
                else R.drawable.edit_text_background,
                null
            )
        }
        
        searchButton.setOnClickListener {
            searchBooks()
        }
        
        backButton.setOnClickListener {
            activity.goToPreviousStep()
        }
        
        completeButton.setOnClickListener {
            validateAndComplete()
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
                val url = URL("https://openlibrary.org/search.json?q=$encodedQuery&limit=10")
                val response = url.readText()
                
                withContext(Dispatchers.Main) {
                    parseSearchResults(response)
                    searchButton.isEnabled = true
                    searchButton.text = getString(R.string.search)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to search books: ${e.message}", Toast.LENGTH_SHORT).show()
                    searchButton.isEnabled = true
                    searchButton.text = getString(R.string.search)
                }
            }
        }
    }
    
    private fun parseSearchResults(json: String) {
        resultsList.removeAllViews()
        
        try {
            val jsonObject = gson.fromJson(json, JsonObject::class.java)
            val docs = jsonObject.getAsJsonArray("docs")
            
            if (docs == null || docs.size() == 0) {
                Toast.makeText(context, "No books found. Try a different search term.", Toast.LENGTH_SHORT).show()
                return
            }
            
            for (i in 0 until docs.size()) {
                val doc = docs.get(i).asJsonObject
                val title = doc.get("title")?.asString ?: "Unknown Title"
                val author = doc.getAsJsonArray("author_name")?.get(0)?.asString ?: "Unknown Author"
                val coverId = doc.get("cover_i")?.asInt
                val bookId = doc.get("key")?.asString ?: "unknown"
                
                val book = Book(
                    id = bookId,
                    title = title,
                    author = author,
                    coverId = coverId,
                    coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
                )
                
                addBookToResults(book)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error parsing results: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addBookToResults(book: Book) {
        val bookView = LayoutInflater.from(context).inflate(R.layout.item_book_result, resultsList, false)
        
        val titleText = bookView.findViewById<TextView>(R.id.bookTitle)
        val authorText = bookView.findViewById<TextView>(R.id.bookAuthor)
        val selectButton = bookView.findViewById<Button>(R.id.selectButton)
        
        titleText.text = book.title
        authorText.text = "by ${book.author}"
        
        val isSelected = selectedBooks.any { it.id == book.id }
        selectButton.text = if (isSelected) "✓ Selected" else "Select"
        selectButton.isEnabled = !isSelected || selectedBooks.size < 3
        
        selectButton.setOnClickListener {
            toggleBookSelection(book)
        }
        
        resultsList.addView(bookView)
    }
    
    private fun toggleBookSelection(book: Book) {
        val existingIndex = selectedBooks.indexOfFirst { it.id == book.id }
        
        if (existingIndex >= 0) {
            selectedBooks.removeAt(existingIndex)
        } else {
            if (selectedBooks.size >= 3) {
                Toast.makeText(context, "You can only select 3 books for your Mystery Shelf", Toast.LENGTH_SHORT).show()
                return
            }
            selectedBooks.add(book)
        }
        
        updateSelectedBooksUI()
        refreshResultsList()
    }
    
    private fun updateSelectedBooksUI() {
        if (selectedBooks.isEmpty()) {
            selectedBooksContainer.visibility = View.GONE
        } else {
            selectedBooksContainer.visibility = View.VISIBLE
            selectedBooksTitle.text = getString(R.string.selected_books, selectedBooks.size)
            
            val selectedList = selectedBooksContainer.findViewById<LinearLayout>(R.id.selectedBooksList)
            selectedList.removeAllViews()
            
            selectedBooks.forEach { book ->
                val bookView = LayoutInflater.from(context).inflate(R.layout.item_selected_book, selectedList, false)
                val titleText = bookView.findViewById<TextView>(R.id.selectedBookTitle)
                val authorText = bookView.findViewById<TextView>(R.id.selectedBookAuthor)
                val removeButton = bookView.findViewById<Button>(R.id.removeButton)
                
                titleText.text = book.title
                authorText.text = "by ${book.author}"
                
                removeButton.setOnClickListener {
                    selectedBooks.remove(book)
                    updateSelectedBooksUI()
                    refreshResultsList()
                }
                
                selectedList.addView(bookView)
            }
        }
        
        completeButton.isEnabled = selectedBooks.size == 3
    }
    
    private fun refreshResultsList() {
        // Re-render results with updated selection states
        // Note: In a production app, you'd store search results and re-render them
        // For now, user needs to search again to see updated selection states
    }
    
    private fun validateAndComplete() {
        if (selectedBooks.size != 3) {
            Toast.makeText(context, "Please select exactly 3 books for your Mystery Shelf", Toast.LENGTH_SHORT).show()
            return
        }
        
        activity.updateBooks(selectedBooks)
        activity.goToNextStep()
    }
}
