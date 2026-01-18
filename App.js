import React, { useState, useEffect } from 'react';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, View, Text } from 'react-native';
import OnboardingFlow from './components/onboarding/OnboardingFlow';

export default function App() {
  console.log('[App] Component initializing...');
  
  const [onboardingComplete, setOnboardingComplete] = useState(false);
  const [userProfile, setUserProfile] = useState(null);

  useEffect(() => {
    console.log('[App] Component mounted');
    console.log('[App] Onboarding complete:', onboardingComplete);
    console.log('[App] User profile:', userProfile);
  }, [onboardingComplete, userProfile]);

  const handleOnboardingComplete = (profile) => {
    console.log('[App] Onboarding completed with profile:', profile);
    setUserProfile(profile);
    setOnboardingComplete(true);
    console.log('[App] State updated - onboarding complete');
  };

  return (
    <View style={styles.container}>
      {!onboardingComplete ? (
        <>
          <OnboardingFlow onComplete={handleOnboardingComplete} />
          <StatusBar style="auto" />
        </>
      ) : (
        <>
          <View style={styles.completedContainer}>
            <Text style={styles.completedTitle}>Welcome to Binder! 📚</Text>
            <Text style={styles.completedText}>
              Your profile has been created successfully!
            </Text>
            {userProfile && (
              <View style={styles.profileInfo}>
                <Text style={styles.profileText}>
                  Age: {userProfile.age}
                </Text>
                <Text style={styles.profileText}>
                  Gender: {userProfile.gender}
                </Text>
                <Text style={styles.profileText}>
                  Genres: {userProfile.genres.join(', ')}
                </Text>
                <Text style={styles.profileText}>
                  Books Selected: {userProfile.books.length}
                </Text>
              </View>
            )}
          </View>
          <StatusBar style="auto" />
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  completedContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  completedTitle: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#1a1a1a',
    marginBottom: 16,
  },
  completedText: {
    fontSize: 18,
    color: '#666',
    textAlign: 'center',
    marginBottom: 32,
  },
  profileInfo: {
    marginTop: 20,
    padding: 20,
    backgroundColor: '#f9f9f9',
    borderRadius: 12,
    width: '100%',
  },
  profileText: {
    fontSize: 16,
    color: '#1a1a1a',
    marginBottom: 8,
  },
});
