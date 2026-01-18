import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';

const GENDERS = ['Male', 'Female', 'Non-binary', 'Prefer not to say'];
const INTERESTS = [
  'Fiction',
  'Non-fiction',
  'Poetry',
  'Biography',
  'History',
  'Science',
  'Philosophy',
  'Art',
  'Music',
  'Travel',
  'Cooking',
  'Sports',
];

export default function Step1BasicInfo({ formData, updateFormData, onNext }) {
  const [age, setAge] = useState(formData.age?.toString() || '');
  const [selectedGender, setSelectedGender] = useState(formData.gender || null);
  const [selectedInterests, setSelectedInterests] = useState(formData.interests || []);

  const handleInterestToggle = (interest) => {
    setSelectedInterests((prev) => {
      if (prev.includes(interest)) {
        return prev.filter((i) => i !== interest);
      } else {
        return [...prev, interest];
      }
    });
  };

  const handleNext = () => {
    const ageNum = parseInt(age);
    if (!ageNum || ageNum < 13 || ageNum > 120) {
      alert('Please enter a valid age (13-120)');
      return;
    }
    if (!selectedGender) {
      alert('Please select your gender');
      return;
    }
    if (selectedInterests.length === 0) {
      alert('Please select at least one interest');
      return;
    }

    updateFormData('age', ageNum);
    updateFormData('gender', selectedGender);
    updateFormData('interests', selectedInterests);
    onNext();
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={styles.container}
    >
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.title}>Tell us about yourself</Text>
          <Text style={styles.subtitle}>Step 1 of 3</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>Age</Text>
          <TextInput
            style={styles.input}
            placeholder="Enter your age"
            value={age}
            onChangeText={setAge}
            keyboardType="number-pad"
            maxLength={3}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>Gender</Text>
          <View style={styles.genderContainer}>
            {GENDERS.map((gender) => (
              <TouchableOpacity
                key={gender}
                style={[
                  styles.genderButton,
                  selectedGender === gender && styles.genderButtonSelected,
                ]}
                onPress={() => setSelectedGender(gender)}
              >
                <Text
                  style={[
                    styles.genderButtonText,
                    selectedGender === gender && styles.genderButtonTextSelected,
                  ]}
                >
                  {gender}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>Interests</Text>
          <Text style={styles.hint}>Select all that apply</Text>
          <View style={styles.interestsContainer}>
            {INTERESTS.map((interest) => (
              <TouchableOpacity
                key={interest}
                style={[
                  styles.interestTag,
                  selectedInterests.includes(interest) && styles.interestTagSelected,
                ]}
                onPress={() => handleInterestToggle(interest)}
              >
                <Text
                  style={[
                    styles.interestTagText,
                    selectedInterests.includes(interest) && styles.interestTagTextSelected,
                  ]}
                >
                  {interest}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <TouchableOpacity style={styles.nextButton} onPress={handleNext}>
          <Text style={styles.nextButtonText}>Next</Text>
        </TouchableOpacity>
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
    marginBottom: 40,
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
  },
  section: {
    marginBottom: 32,
  },
  label: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1a1a1a',
    marginBottom: 12,
  },
  hint: {
    fontSize: 14,
    color: '#666',
    marginBottom: 12,
  },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 12,
    padding: 16,
    fontSize: 16,
    backgroundColor: '#f9f9f9',
  },
  genderContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  genderButton: {
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 24,
    borderWidth: 2,
    borderColor: '#ddd',
    backgroundColor: '#fff',
  },
  genderButtonSelected: {
    borderColor: '#6C5CE7',
    backgroundColor: '#6C5CE7',
  },
  genderButtonText: {
    fontSize: 16,
    color: '#666',
    fontWeight: '500',
  },
  genderButtonTextSelected: {
    color: '#fff',
  },
  interestsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  interestTag: {
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#ddd',
    backgroundColor: '#fff',
  },
  interestTagSelected: {
    borderColor: '#6C5CE7',
    backgroundColor: '#6C5CE7',
  },
  interestTagText: {
    fontSize: 14,
    color: '#666',
  },
  interestTagTextSelected: {
    color: '#fff',
    fontWeight: '600',
  },
  nextButton: {
    backgroundColor: '#6C5CE7',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 20,
    marginBottom: 40,
  },
  nextButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
});
