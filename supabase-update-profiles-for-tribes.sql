-- Update profiles table to support currentlyReading books for Tribe feature
-- Run this SQL in your Supabase SQL Editor

-- Add currently_reading column to profiles table
ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS currently_reading JSONB DEFAULT '{"books": []}'::jsonb;

-- Create index for faster queries on currently_reading
CREATE INDEX IF NOT EXISTS idx_profiles_currently_reading ON profiles USING GIN (currently_reading);

-- Update existing profiles to have empty currently_reading if null
UPDATE profiles 
SET currently_reading = '{"books": []}'::jsonb 
WHERE currently_reading IS NULL;
