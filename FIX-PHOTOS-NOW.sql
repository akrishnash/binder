-- IMMEDIATE FIX: Update existing profiles with photos
-- Run this in Supabase SQL Editor RIGHT NOW

-- Fix Anurag (developer) profile
-- Note: id is UUID type, so we only check text_id and username
UPDATE profiles 
SET photo_uri = 'android.resource://com.binder/drawable/anurag'
WHERE (text_id = 'developer' OR username = 'Anurag')
  AND (photo_uri IS NULL OR photo_uri = '');

-- Fix Fiza profile  
UPDATE profiles 
SET photo_uri = 'android.resource://com.binder/drawable/fiza'
WHERE (text_id = 'fiza' OR username = 'Fiza')
  AND (photo_uri IS NULL OR photo_uri = '');

-- Verify the fix
SELECT 
    username, 
    text_id,
    CASE 
        WHEN photo_uri IS NULL THEN '❌ NULL'
        WHEN photo_uri LIKE 'android.resource://%' THEN '✅ RESOURCE'
        WHEN photo_uri LIKE 'data:image%' THEN '✅ BASE64'
        ELSE '⚠️ OTHER'
    END as photo_status,
    LEFT(photo_uri, 50) as photo_preview
FROM profiles
ORDER BY username;
