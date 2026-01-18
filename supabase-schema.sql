-- Supabase Profiles Table Schema
-- Run this SQL in your Supabase SQL Editor to create the profiles table

CREATE TABLE IF NOT EXISTS profiles (
  id TEXT PRIMARY KEY,
  username TEXT DEFAULT '',
  age INTEGER NOT NULL,
  gender TEXT NOT NULL,
  interests TEXT[] NOT NULL,
  genres TEXT[] NOT NULL,
  books JSONB NOT NULL,
  photo_uri TEXT,
  bio TEXT DEFAULT '',
  city TEXT DEFAULT '',
  pages_read_today INTEGER DEFAULT 0,
  created_at TEXT DEFAULT NOW()::TEXT,
  updated_at TEXT DEFAULT NOW()::TEXT
);

-- Add index for faster queries
CREATE INDEX IF NOT EXISTS idx_profiles_created_at ON profiles(created_at);

-- Add RLS (Row Level Security) policies if needed
-- Enable RLS
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if they exist (to allow re-running this script)
DROP POLICY IF EXISTS "Allow public insert" ON profiles;
DROP POLICY IF EXISTS "Allow public read" ON profiles;

-- Policy to allow anyone to insert (for onboarding)
CREATE POLICY "Allow public insert" ON profiles
  FOR INSERT
  WITH CHECK (true);

-- Policy to allow users to read their own profile
-- You may want to adjust this based on your auth setup
CREATE POLICY "Allow public read" ON profiles
  FOR SELECT
  USING (true);

-- Optional: Add a trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ language 'plpgsql';

-- Drop trigger if exists (to allow re-running this script)
DROP TRIGGER IF EXISTS update_profiles_updated_at ON profiles;

CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE
  ON profiles FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- Insert Demo Users
-- Anurag (Developer)
INSERT INTO profiles (id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at) VALUES
('developer', 'Anurag', 27, 'Male', 
 ARRAY['Philosophy', 'History', 'Science', 'Music', 'Culture'],
 ARRAY['Philosophy', 'Classics', 'Literary Fiction'],
 '{"books": [{"id": "anurag1", "title": "Book of Mirad", "author": "Unknown", "coverId": null, "coverUrl": null}, {"id": "anurag2", "title": "Selected Stories", "author": "Anton Chekhov", "coverId": null, "coverUrl": null}, {"id": "anurag3", "title": "The Brothers Karamazov", "author": "Fyodor Dostoevsky", "coverId": null, "coverUrl": null}]}'::jsonb,
 'Building Binder to connect book lovers. Always reading, always thinking. Looking for someone who appreciates deep conversations and great stories.',
 '', 42, NOW()::TEXT)
ON CONFLICT (id) DO NOTHING;

-- Fiza
INSERT INTO profiles (id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at) VALUES
('fiza', 'Fiza', 29, 'Female',
 ARRAY['Mastikhori', 'Kalesh', 'Maar Pitayi', 'Rona Dhona'],
 ARRAY['Comedy', 'Cooking', 'Feminism'],
 '{"books": [{"id": "fiza1", "title": "B Grade Cooking Books in Hindi", "author": "Various", "coverId": null, "coverUrl": null}, {"id": "fiza2", "title": "Some Book on Dumb Feminism", "author": "Various", "coverId": null, "coverUrl": null}, {"id": "fiza3", "title": "Santa Banta Jokes Book", "author": "Various", "coverId": null, "coverUrl": null}]}'::jsonb,
 'Living life with drama, humor, and lots of kalesh! Looking for someone who can handle my energy.',
 '', 15, NOW()::TEXT)
ON CONFLICT (id) DO NOTHING;

-- Sarah
INSERT INTO profiles (id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at) VALUES
('demo1', 'Sarah', 24, 'Female',
 ARRAY['Fiction', 'Poetry', 'Art'],
 ARRAY['Sci-Fi', 'Fantasy', 'Literary Fiction'],
 '{"books": [{"id": "1", "title": "Dune", "author": "Frank Herbert", "coverId": 123456, "coverUrl": "https://covers.openlibrary.org/b/id/123456-M.jpg"}, {"id": "3", "title": "Project Hail Mary", "author": "Andy Weir", "coverId": 345678, "coverUrl": "https://covers.openlibrary.org/b/id/345678-M.jpg"}, {"id": "5", "title": "Circe", "author": "Madeline Miller", "coverId": 567890, "coverUrl": "https://covers.openlibrary.org/b/id/567890-M.jpg"}]}'::jsonb,
 'Looking for someone to finish ''Dune'' with.',
 'New York', 30, NOW()::TEXT)
ON CONFLICT (id) DO NOTHING;

-- Alex
INSERT INTO profiles (id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at) VALUES
('demo2', 'Alex', 28, 'Male',
 ARRAY['History', 'Philosophy', 'Travel'],
 ARRAY['Historical Fiction', 'Biography', 'Philosophy'],
 '{"books": [{"id": "2", "title": "The Seven Husbands of Evelyn Hugo", "author": "Taylor Jenkins Reid", "coverId": 234567, "coverUrl": "https://covers.openlibrary.org/b/id/234567-M.jpg"}, {"id": "4", "title": "The Midnight Library", "author": "Matt Haig", "coverId": 456789, "coverUrl": "https://covers.openlibrary.org/b/id/456789-M.jpg"}, {"id": "6", "title": "The Song of Achilles", "author": "Madeline Miller", "coverId": 678901, "coverUrl": "https://covers.openlibrary.org/b/id/678901-M.jpg"}]}'::jsonb,
 'Bookworm seeking intellectual conversations.',
 'San Francisco', 45, NOW()::TEXT)
ON CONFLICT (id) DO NOTHING;

-- Jordan
INSERT INTO profiles (id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at) VALUES
('demo3', 'Jordan', 26, 'Non-binary',
 ARRAY['Science', 'Non-fiction', 'Cooking'],
 ARRAY['Science', 'Biography', 'Memoir'],
 '{"books": [{"id": "3", "title": "Project Hail Mary", "author": "Andy Weir", "coverId": 345678, "coverUrl": "https://covers.openlibrary.org/b/id/345678-M.jpg"}, {"id": "7", "title": "1984", "author": "George Orwell", "coverId": 789012, "coverUrl": "https://covers.openlibrary.org/b/id/789012-M.jpg"}, {"id": "1", "title": "Dune", "author": "Frank Herbert", "coverId": 123456, "coverUrl": "https://covers.openlibrary.org/b/id/123456-M.jpg"}]}'::jsonb,
 'Sci-fi enthusiast and coffee lover.',
 'Seattle', 25, NOW()::TEXT)
ON CONFLICT (id) DO NOTHING;
