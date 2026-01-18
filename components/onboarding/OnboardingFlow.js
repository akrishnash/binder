import React, { useState, useRef, useEffect } from 'react';
import { View, StyleSheet, Dimensions, Animated } from 'react-native';
import Step1BasicInfo from './Step1BasicInfo';
import Step2GenreSelection from './Step2GenreSelection';
import Step3MysteryShelf from './Step3MysteryShelf';
import { getSupabase, isSupabaseConfigured } from '../../config/supabase';

const { width } = Dimensions.get('window');

export default function OnboardingFlow({ onComplete }) {
  console.log('[OnboardingFlow] Component initialized');
  
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState({
    age: null,
    gender: null,
    interests: [],
    genres: [],
    books: [],
  });

  const translateX = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    console.log('[OnboardingFlow] Component mounted, current step:', currentStep);
    return () => {
      console.log('[OnboardingFlow] Component unmounting');
    };
  }, [currentStep]);

  const goToStep = (step) => {
    console.log('[OnboardingFlow] Navigating to step:', step);
    Animated.timing(translateX, {
      toValue: -(step - 1) * width,
      duration: 300,
      useNativeDriver: false, // Changed to false to avoid native module issues
    }).start(() => {
      console.log('[OnboardingFlow] Animation completed for step:', step);
    });
    setCurrentStep(step);
  };

  const updateFormData = (key, value) => {
    console.log('[OnboardingFlow] Updating form data:', key, value);
    setFormData((prev) => {
      const updated = { ...prev, [key]: value };
      console.log('[OnboardingFlow] Form data updated:', updated);
      return updated;
    });
  };

  const handleNext = () => {
    console.log('[OnboardingFlow] Next button pressed, current step:', currentStep);
    if (currentStep < 3) {
      goToStep(currentStep + 1);
    } else {
      console.log('[OnboardingFlow] Last step reached, submitting...');
      handleSubmit();
    }
  };

  const handleBack = () => {
    console.log('[OnboardingFlow] Back button pressed, current step:', currentStep);
    if (currentStep > 1) {
      goToStep(currentStep - 1);
    }
  };

  const handleSubmit = async () => {
    console.log('[OnboardingFlow] Submitting form data:', formData);
    
    // Check if Supabase is configured
    const supabaseConfigured = isSupabaseConfigured();
    
    if (supabaseConfigured) {
      try {
        console.log('[OnboardingFlow] Attempting to save to Supabase...');
        const supabase = await getSupabase();
        
        if (supabase) {
          // Prepare profile data for Supabase
          const profileData = {
            age: formData.age,
            gender: formData.gender,
            interests: formData.interests,
            genres: formData.genres,
            books: formData.books, // JSONB field
          };
          
          // Insert into Supabase
          const { data, error } = await supabase
            .from('profiles')
            .insert([profileData])
            .select()
            .single();
          
          if (error) {
            console.error('[OnboardingFlow] Supabase insert error:', error);
            // Fall back to local storage on error
            createLocalProfile();
          } else {
            console.log('[OnboardingFlow] Profile saved to Supabase:', data);
            // Use Supabase profile data
            const profile = {
              ...formData,
              id: data.id,
              created_at: data.created_at,
            };
            
            if (onComplete) {
              console.log('[OnboardingFlow] Calling onComplete with Supabase profile');
              onComplete(profile);
            }
            return;
          }
        } else {
          console.warn('[OnboardingFlow] Supabase client not available, using local storage');
          createLocalProfile();
        }
      } catch (error) {
        console.error('[OnboardingFlow] Error saving to Supabase:', error);
        // Fall back to local storage on error
        createLocalProfile();
      }
    } else {
      console.log('[OnboardingFlow] Supabase not configured, using local storage');
      createLocalProfile();
    }
  };
  
  const createLocalProfile = () => {
    // Create local profile as fallback
    const localProfile = {
      ...formData,
      id: `local-${Date.now()}`,
      created_at: new Date().toISOString(),
    };
    
    console.log('[OnboardingFlow] Profile created locally:', localProfile);
    
    if (onComplete) {
      console.log('[OnboardingFlow] Calling onComplete with local profile');
      onComplete(localProfile);
    }
  };

  const animatedStyle = {
    transform: [{ translateX }],
  };

  return (
    <View style={styles.container}>
      <Animated.View style={[styles.stepsContainer, animatedStyle]}>
        <View style={[styles.step, { width }]}>
          <Step1BasicInfo
            formData={formData}
            updateFormData={updateFormData}
            onNext={handleNext}
          />
        </View>
        <View style={[styles.step, { width }]}>
          <Step2GenreSelection
            formData={formData}
            updateFormData={updateFormData}
            onNext={handleNext}
            onBack={handleBack}
          />
        </View>
        <View style={[styles.step, { width }]}>
          <Step3MysteryShelf
            formData={formData}
            updateFormData={updateFormData}
            onNext={handleNext}
            onBack={handleBack}
          />
        </View>
      </Animated.View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    overflow: 'hidden',
  },
  stepsContainer: {
    flexDirection: 'row',
    height: '100%',
  },
  step: {
    flex: 1,
  },
});
