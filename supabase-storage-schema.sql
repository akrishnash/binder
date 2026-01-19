-- Supabase Storage Bucket for Profile Photos
-- Run this SQL in your Supabase SQL Editor to create the storage bucket

-- Create bucket for profile photos
INSERT INTO storage.buckets (id, name, public)
VALUES ('profile-photos', 'profile-photos', true)
ON CONFLICT (id) DO NOTHING;

-- Enable RLS on storage.objects
-- Note: RLS is usually enabled by default, but we'll make sure

-- Drop existing policies if they exist (to allow re-running this script)
DROP POLICY IF EXISTS "Allow public uploads" ON storage.objects;
DROP POLICY IF EXISTS "Allow public reads" ON storage.objects;
DROP POLICY IF EXISTS "Allow public updates" ON storage.objects;
DROP POLICY IF EXISTS "Allow public deletes" ON storage.objects;

-- Policy: Allow anyone to upload photos (public insert)
CREATE POLICY "Allow public uploads" ON storage.objects
FOR INSERT
TO public
WITH CHECK (bucket_id = 'profile-photos');

-- Policy: Allow anyone to read photos (public read)
CREATE POLICY "Allow public reads" ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'profile-photos');

-- Policy: Allow users to update their own photos
-- Note: This requires auth, but for now we'll allow public updates
CREATE POLICY "Allow public updates" ON storage.objects
FOR UPDATE
TO public
USING (bucket_id = 'profile-photos');

-- Policy: Allow users to delete their own photos
-- Note: This requires auth, but for now we'll allow public deletes
CREATE POLICY "Allow public deletes" ON storage.objects
FOR DELETE
TO public
USING (bucket_id = 'profile-photos');