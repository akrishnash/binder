import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
} from 'react-native';

const GENRES = [
  'Sci-Fi',
  'Noir',
  'Fantasy',
  'Mystery',
  'Romance',
  'Thriller',
  'Horror',
  'Historical Fiction',
  'Literary Fiction',
  'Young Adult',
  'Biography',
  'Memoir',
  'Poetry',
  'Philosophy',
  'Science',
  'History',
  'Art & Design',
  'Graphic Novels',
  'Comedy',
  'Drama',
];

export default function Step2GenreSelection({ formData, updateFormData, onNext, onBack }) {
  const [selectedGenres, setSelectedGenres] = useState(formData.genres || []);

  const handleGenreToggle = (genre) => {
    setSelectedGenres((prev) => {
      if (prev.includes(genre)) {
        return prev.filter((g) => g !== genre);
      } else {
        return [...prev, genre];
      }
    });
  };

  const handleNext = () => {
    if (selectedGenres.length === 0) {
      alert('Please select at least one genre');
      return;
    }
    updateFormData('genres', selectedGenres);
    onNext();
  };

  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.title}>Choose your genres</Text>
          <Text style={styles.subtitle}>Step 2 of 3</Text>
          <Text style={styles.description}>
            Select the genres that interest you. You can choose multiple!
          </Text>
        </View>

        <View style={styles.genresContainer}>
          {GENRES.map((genre) => (
            <TouchableOpacity
              key={genre}
              style={[
                styles.genreTag,
                selectedGenres.includes(genre) && styles.genreTagSelected,
              ]}
              onPress={() => handleGenreToggle(genre)}
            >
              <Text
                style={[
                  styles.genreTagText,
                  selectedGenres.includes(genre) && styles.genreTagTextSelected,
                ]}
              >
                {genre}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        <View style={styles.buttonContainer}>
          <TouchableOpacity style={styles.backButton} onPress={onBack}>
            <Text style={styles.backButtonText}>Back</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.nextButton} onPress={handleNext}>
            <Text style={styles.nextButtonText}>Next</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
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
  genresContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 32,
  },
  genreTag: {
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 24,
    borderWidth: 2,
    borderColor: '#ddd',
    backgroundColor: '#fff',
  },
  genreTagSelected: {
    borderColor: '#6C5CE7',
    backgroundColor: '#6C5CE7',
  },
  genreTagText: {
    fontSize: 15,
    color: '#666',
    fontWeight: '500',
  },
  genreTagTextSelected: {
    color: '#fff',
    fontWeight: '600',
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
  nextButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
});
