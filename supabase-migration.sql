-- Migration Script: Add missing columns to existing profiles table
-- Run this in your Supabase SQL Editor

-- Step 1: Add missing columns if they don't exist
DO $$ 
BEGIN
    -- Add username column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'username') THEN
        ALTER TABLE profiles ADD COLUMN username TEXT DEFAULT '';
    END IF;
    
    -- Add photo_uri column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'photo_uri') THEN
        ALTER TABLE profiles ADD COLUMN photo_uri TEXT;
    END IF;
    
    -- Add bio column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'bio') THEN
        ALTER TABLE profiles ADD COLUMN bio TEXT DEFAULT '';
    END IF;
    
    -- Add city column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'city') THEN
        ALTER TABLE profiles ADD COLUMN city TEXT DEFAULT '';
    END IF;
    
    -- Add pages_read_today column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'pages_read_today') THEN
        ALTER TABLE profiles ADD COLUMN pages_read_today INTEGER DEFAULT 0;
    END IF;
    
    -- Add text_id column for TEXT-based IDs (since your id is UUID)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'text_id') THEN
        ALTER TABLE profiles ADD COLUMN text_id TEXT;
        -- Make it unique
        CREATE UNIQUE INDEX IF NOT EXISTS idx_profiles_text_id ON profiles(text_id) WHERE text_id IS NOT NULL;
    END IF;
    
    -- Add created_at_text and updated_at_text for TEXT timestamps
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'created_at_text') THEN
        ALTER TABLE profiles ADD COLUMN created_at_text TEXT;
        -- Migrate existing data
        UPDATE profiles SET created_at_text = created_at::TEXT WHERE created_at_text IS NULL;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'updated_at_text') THEN
        ALTER TABLE profiles ADD COLUMN updated_at_text TEXT;
        UPDATE profiles SET updated_at_text = updated_at::TEXT WHERE updated_at_text IS NULL;
    END IF;
END $$;

-- Step 2: Insert Demo Users using text_id (works with UUID id column)
-- Anurag (Developer)
INSERT INTO profiles (text_id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at_text, photo_uri) 
SELECT 'developer', 'Anurag', 27, 'Male', 
 ARRAY['Philosophy', 'History', 'Science', 'Music', 'Culture'],
 ARRAY['Philosophy', 'Classics', 'Literary Fiction'],
 '{"books": [{"id": "anurag1", "title": "Book of Mirad", "author": "Unknown", "coverId": null, "coverUrl": null}, {"id": "anurag2", "title": "Selected Stories", "author": "Anton Chekhov", "coverId": null, "coverUrl": null}, {"id": "anurag3", "title": "The Brothers Karamazov", "author": "Fyodor Dostoevsky", "coverId": null, "coverUrl": null}]}'::jsonb,
 'Building Binder to connect book lovers. Always reading, always thinking. Looking for someone who appreciates deep conversations and great stories.',
 '', 42, NOW()::TEXT, 'android.resource://com.binder/drawable/anurag'
WHERE NOT EXISTS (SELECT 1 FROM profiles WHERE text_id = 'developer');

-- Fiza
INSERT INTO profiles (text_id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at_text, photo_uri) 
SELECT 'fiza', 'Fiza', 29, 'Female',
 ARRAY['Mastikhori', 'Kalesh', 'Maar Pitayi', 'Rona Dhona'],
 ARRAY['Comedy', 'Cooking', 'Feminism'],
 '{"books": [{"id": "fiza1", "title": "B Grade Cooking Books in Hindi", "author": "Various", "coverId": null, "coverUrl": null}, {"id": "fiza2", "title": "Some Book on Dumb Feminism", "author": "Various", "coverId": null, "coverUrl": null}, {"id": "fiza3", "title": "Santa Banta Jokes Book", "author": "Various", "coverId": null, "coverUrl": null}]}'::jsonb,
 'Living life with drama, humor, and lots of kalesh! Looking for someone who can handle my energy.',
 '', 15, NOW()::TEXT, 'android.resource://com.binder/drawable/fiza'
WHERE NOT EXISTS (SELECT 1 FROM profiles WHERE text_id = 'fiza');

-- Sarah
INSERT INTO profiles (text_id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at_text) 
SELECT 'demo1', 'Sarah', 24, 'Female',
 ARRAY['Fiction', 'Poetry', 'Art'],
 ARRAY['Sci-Fi', 'Fantasy', 'Literary Fiction'],
 '{"books": [{"id": "1", "title": "Dune", "author": "Frank Herbert", "coverId": 123456, "coverUrl": "https://covers.openlibrary.org/b/id/123456-M.jpg"}, {"id": "3", "title": "Project Hail Mary", "author": "Andy Weir", "coverId": 345678, "coverUrl": "https://covers.openlibrary.org/b/id/345678-M.jpg"}, {"id": "5", "title": "Circe", "author": "Madeline Miller", "coverId": 567890, "coverUrl": "https://covers.openlibrary.org/b/id/567890-M.jpg"}]}'::jsonb,
 'Looking for someone to finish ''Dune'' with.',
 'New York', 30, NOW()::TEXT
WHERE NOT EXISTS (SELECT 1 FROM profiles WHERE text_id = 'demo1');

-- Alex
INSERT INTO profiles (text_id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at_text) 
SELECT 'demo2', 'Alex', 28, 'Male',
 ARRAY['History', 'Philosophy', 'Travel'],
 ARRAY['Historical Fiction', 'Biography', 'Philosophy'],
 '{"books": [{"id": "2", "title": "The Seven Husbands of Evelyn Hugo", "author": "Taylor Jenkins Reid", "coverId": 234567, "coverUrl": "https://covers.openlibrary.org/b/id/234567-M.jpg"}, {"id": "4", "title": "The Midnight Library", "author": "Matt Haig", "coverId": 456789, "coverUrl": "https://covers.openlibrary.org/b/id/456789-M.jpg"}, {"id": "6", "title": "The Song of Achilles", "author": "Madeline Miller", "coverId": 678901, "coverUrl": "https://covers.openlibrary.org/b/id/678901-M.jpg"}]}'::jsonb,
 'Bookworm seeking intellectual conversations.',
 'San Francisco', 45, NOW()::TEXT
WHERE NOT EXISTS (SELECT 1 FROM profiles WHERE text_id = 'demo2');

-- Jordan
INSERT INTO profiles (text_id, username, age, gender, interests, genres, books, bio, city, pages_read_today, created_at_text) 
SELECT 'demo3', 'Jordan', 26, 'Non-binary',
 ARRAY['Science', 'Non-fiction', 'Cooking'],
 ARRAY['Science', 'Biography', 'Memoir'],
 '{"books": [{"id": "3", "title": "Project Hail Mary", "author": "Andy Weir", "coverId": 345678, "coverUrl": "https://covers.openlibrary.org/b/id/345678-M.jpg"}, {"id": "7", "title": "1984", "author": "George Orwell", "coverId": 789012, "coverUrl": "https://covers.openlibrary.org/b/id/789012-M.jpg"}, {"id": "1", "title": "Dune", "author": "Frank Herbert", "coverId": 123456, "coverUrl": "https://covers.openlibrary.org/b/id/123456-M.jpg"}]}'::jsonb,
 'Sci-fi enthusiast and coffee lover.',
 'Seattle', 25, NOW()::TEXT
WHERE NOT EXISTS (SELECT 1 FROM profiles WHERE text_id = 'demo3');
