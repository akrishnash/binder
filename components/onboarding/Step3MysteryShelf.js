import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  ActivityIndicator,
  Image,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';

// Using Open Library API for book search
const BOOK_API_URL = 'https://openlibrary.org/search.json';

export default function Step3MysteryShelf({ formData, updateFormData, onNext, onBack }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [selectedBooks, setSelectedBooks] = useState(formData.books || []);
  const [isSearching, setIsSearching] = useState(false);

  const searchBooks = async () => {
    if (!searchQuery.trim()) {
      alert('Please enter a book title or author');
      return;
    }

    setIsSearching(true);
    try {
      const response = await fetch(`${BOOK_API_URL}?q=${encodeURIComponent(searchQuery)}&limit=10`);
      const data = await response.json();
      
      if (data.docs && data.docs.length > 0) {
        const books = data.docs.map((doc) => ({
          id: doc.key,
          title: doc.title,
          author: doc.author_name ? doc.author_name[0] : 'Unknown Author',
          coverId: doc.cover_i,
          isbn: doc.isbn ? doc.isbn[0] : null,
        }));
        setSearchResults(books);
      } else {
        setSearchResults([]);
        alert('No books found. Try a different search term.');
      }
    } catch (error) {
      console.error('Error searching books:', error);
      alert('Failed to search books. Please try again.');
    } finally {
      setIsSearching(false);
    }
  };

  const handleBookSelect = (book) => {
    if (selectedBooks.find((b) => b.id === book.id)) {
      // Deselect if already selected
      setSelectedBooks((prev) => prev.filter((b) => b.id !== book.id));
    } else {
      // Select if not already selected (max 3)
      if (selectedBooks.length >= 3) {
        alert('You can only select 3 books for your Mystery Shelf');
        return;
      }
      setSelectedBooks((prev) => [...prev, book]);
    }
  };

  const handleRemoveBook = (bookId) => {
    setSelectedBooks((prev) => prev.filter((b) => b.id !== bookId));
  };

  const handleNext = () => {
    if (selectedBooks.length !== 3) {
      alert('Please select exactly 3 books for your Mystery Shelf');
      return;
    }
    updateFormData('books', selectedBooks);
    onNext();
  };

  const getCoverImageUrl = (coverId) => {
    if (!coverId) return null;
    return `https://covers.openlibrary.org/b/id/${coverId}-M.jpg`;
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={styles.container}
    >
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.title}>My Mystery Shelf</Text>
          <Text style={styles.subtitle}>Step 3 of 3</Text>
          <Text style={styles.description}>
            Search for 3 books that represent your taste. These will be shown on your profile!
          </Text>
        </View>

        <View style={styles.searchContainer}>
          <TextInput
            style={styles.searchInput}
            placeholder="Search for books by title or author..."
            value={searchQuery}
            onChangeText={setSearchQuery}
            onSubmitEditing={searchBooks}
            returnKeyType="search"
          />
          <TouchableOpacity style={styles.searchButton} onPress={searchBooks}>
            {isSearching ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={styles.searchButtonText}>Search</Text>
            )}
          </TouchableOpacity>
        </View>

        {selectedBooks.length > 0 && (
          <View style={styles.selectedContainer}>
            <Text style={styles.selectedTitle}>Selected Books ({selectedBooks.length}/3)</Text>
            {selectedBooks.map((book) => (
              <View key={book.id} style={styles.selectedBook}>
                <View style={styles.selectedBookInfo}>
                  <Text style={styles.selectedBookTitle}>{book.title}</Text>
                  <Text style={styles.selectedBookAuthor}>by {book.author}</Text>
                </View>
                <TouchableOpacity
                  style={styles.removeButton}
                  onPress={() => handleRemoveBook(book.id)}
                >
                  <Text style={styles.removeButtonText}>×</Text>
                </TouchableOpacity>
              </View>
            ))}
          </View>
        )}

        {searchResults.length > 0 && (
          <View style={styles.resultsContainer}>
            <Text style={styles.resultsTitle}>Search Results</Text>
            {searchResults.map((book) => {
              const isSelected = selectedBooks.find((b) => b.id === book.id);
              const coverUrl = getCoverImageUrl(book.coverId);
              
              return (
                <TouchableOpacity
                  key={book.id}
                  style={[
                    styles.bookResult,
                    isSelected && styles.bookResultSelected,
                  ]}
                  onPress={() => handleBookSelect(book)}
                >
                  {coverUrl && (
                    <Image source={{ uri: coverUrl }} style={styles.bookCover} />
                  )}
                  <View style={styles.bookInfo}>
                    <Text style={styles.bookTitle} numberOfLines={2}>
                      {book.title}
                    </Text>
                    <Text style={styles.bookAuthor} numberOfLines={1}>
                      by {book.author}
                    </Text>
                  </View>
                  {isSelected && (
                    <View style={styles.checkmark}>
                      <Text style={styles.checkmarkText}>✓</Text>
                    </View>
                  )}
                </TouchableOpacity>
              );
            })}
          </View>
        )}

        <View style={styles.buttonContainer}>
          <TouchableOpacity style={styles.backButton} onPress={onBack}>
            <Text style={styles.backButtonText}>Back</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.nextButton,
              selectedBooks.length !== 3 && styles.nextButtonDisabled,
            ]}
            onPress={handleNext}
            disabled={selectedBooks.length !== 3}
          >
            <Text style={styles.nextButtonText}>Complete</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  scrollContent: {
    padding: 20,
    paddingTop: 60,
  },
  header: {
    marginBottom: 32,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#1a1a1a',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    marginBottom: 12,
  },
  description: {
    fontSize: 16,
    color: '#666',
    lineHeight: 24,
  },
  searchContainer: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 24,
  },
  searchInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 12,
    padding: 16,
    fontSize: 16,
    backgroundColor: '#f9f9f9',
  },
  searchButton: {
    backgroundColor: '#6C5CE7',
    paddingHorizontal: 24,
    paddingVertical: 16,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    minWidth: 100,
  },
  searchButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  selectedContainer: {
    marginBottom: 24,
    padding: 16,
    backgroundColor: '#f0f0f0',
    borderRadius: 12,
  },
  selectedTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1a1a1a',
    marginBottom: 12,
  },
  selectedBook: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  selectedBookInfo: {
    flex: 1,
  },
  selectedBookTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1a1a1a',
    marginBottom: 4,
  },
  selectedBookAuthor: {
    fontSize: 14,
    color: '#666',
  },
  removeButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#ff4444',
    justifyContent: 'center',
    alignItems: 'center',
  },
  removeButtonText: {
    color: '#fff',
    fontSize: 20,
    fontWeight: 'bold',
  },
  resultsContainer: {
    marginBottom: 24,
  },
  resultsTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1a1a1a',
    marginBottom: 12,
  },
  bookResult: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    marginBottom: 12,
    borderRadius: 12,
    borderWidth: 2,
    borderColor: '#ddd',
    backgroundColor: '#fff',
  },
  bookResultSelected: {
    borderColor: '#6C5CE7',
    backgroundColor: '#f0edff',
  },
  bookCover: {
    width: 50,
    height: 75,
    borderRadius: 4,
    marginRight: 12,
    backgroundColor: '#ddd',
  },
  bookInfo: {
    flex: 1,
  },
  bookTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1a1a1a',
    marginBottom: 4,
  },
  bookAuthor: {
    fontSize: 14,
    color: '#666',
  },
  checkmark: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#6C5CE7',
    justifyContent: 'center',
    alignItems: 'center',
  },
  checkmarkText: {
    color: '#fff',
    fontSize: 20,
    fontWeight: 'bold',
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 40,
  },
  backButton: {
    flex: 1,
    backgroundColor: '#f0f0f0',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  backButtonText: {
    color: '#666',
    fontSize: 18,
    fontWeight: '600',
  },
  nextButton: {
    flex: 1,
    backgroundColor: '#6C5CE7',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  nextButtonDisabled: {
    backgroundColor: '#ccc',
  },
  nextButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
});
