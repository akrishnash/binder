-- Fix existing profiles with NULL photo_uri
-- Run this in Supabase SQL Editor to update existing profiles

-- Update Anurag (developer) profile with android resource URI
UPDATE profiles 
SET photo_uri = 'android.resource://com.binder/drawable/anurag'
WHERE text_id = 'developer' OR id = 'developer' OR username = 'Anurag';

-- Update Fiza profile with android resource URI
UPDATE profiles 
SET photo_uri = 'android.resource://com.binder/drawable/fiza'
WHERE text_id = 'fiza' OR id = 'fiza' OR username = 'Fiza';

-- For demo profiles, you can add placeholder photos or leave them null
-- They will use base64 when users upload photos

-- Verify the updates
SELECT text_id, username, photo_uri FROM profiles WHERE photo_uri IS NOT NULL;
