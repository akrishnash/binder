-- Supabase Tribes (Squads) Schema
-- Run this SQL in your Supabase SQL Editor to create the tribes feature tables

-- Tribes table: stores information about reading squads
CREATE TABLE IF NOT EXISTS tribes (
  id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
  book_id TEXT NOT NULL,  -- Book ID that the tribe is reading
  book_title TEXT NOT NULL,  -- Book title for easy reference
  book_author TEXT NOT NULL,  -- Book author for easy reference
  city TEXT NOT NULL,  -- City where the tribe is located
  status TEXT NOT NULL DEFAULT 'forming',  -- 'forming', 'active', 'completed', 'expired'
  sprint_start_time TEXT,  -- When the 48-hour sprint started
  sprint_end_time TEXT,  -- When the 48-hour sprint ends
  created_at TEXT DEFAULT NOW()::TEXT,
  updated_at TEXT DEFAULT NOW()::TEXT
);

-- Tribe members table: tracks who is in which tribe
CREATE TABLE IF NOT EXISTS tribe_members (
  id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
  tribe_id TEXT NOT NULL REFERENCES tribes(id) ON DELETE CASCADE,
  user_id TEXT NOT NULL,  -- User profile ID
  joined_at TEXT DEFAULT NOW()::TEXT,
  status TEXT NOT NULL DEFAULT 'active',  -- 'active', 'left', 'completed'
  UNIQUE(tribe_id, user_id)  -- Prevent duplicate memberships
);

-- Notifications table: stores tribe formation and other notifications
CREATE TABLE IF NOT EXISTS notifications (
  id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
  user_id TEXT NOT NULL,  -- User who receives the notification
  type TEXT NOT NULL,  -- 'tribe_forming', 'tribe_ready', 'tribe_invite', etc.
  title TEXT NOT NULL,
  message TEXT NOT NULL,
  tribe_id TEXT,  -- Reference to tribe if applicable
  book_id TEXT,  -- Reference to book if applicable
  read BOOLEAN DEFAULT FALSE,
  created_at TEXT DEFAULT NOW()::TEXT
);

-- Indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_tribes_book_city ON tribes(book_id, city);
CREATE INDEX IF NOT EXISTS idx_tribes_status ON tribes(status);
CREATE INDEX IF NOT EXISTS idx_tribe_members_tribe ON tribe_members(tribe_id);
CREATE INDEX IF NOT EXISTS idx_tribe_members_user ON tribe_members(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(user_id, read);

-- RLS policies for tribes
ALTER TABLE tribes ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow public insert tribes" ON tribes;
DROP POLICY IF EXISTS "Allow public read tribes" ON tribes;
DROP POLICY IF EXISTS "Allow public update tribes" ON tribes;

CREATE POLICY "Allow public insert tribes" ON tribes
  FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Allow public read tribes" ON tribes
  FOR SELECT
  USING (true);

CREATE POLICY "Allow public update tribes" ON tribes
  FOR UPDATE
  USING (true);

-- RLS policies for tribe_members
ALTER TABLE tribe_members ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow public insert tribe_members" ON tribe_members;
DROP POLICY IF EXISTS "Allow public read tribe_members" ON tribe_members;
DROP POLICY IF EXISTS "Allow public update tribe_members" ON tribe_members;
DROP POLICY IF EXISTS "Allow public delete tribe_members" ON tribe_members;

CREATE POLICY "Allow public insert tribe_members" ON tribe_members
  FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Allow public read tribe_members" ON tribe_members
  FOR SELECT
  USING (true);

CREATE POLICY "Allow public update tribe_members" ON tribe_members
  FOR UPDATE
  USING (true);

CREATE POLICY "Allow public delete tribe_members" ON tribe_members
  FOR DELETE
  USING (true);

-- RLS policies for notifications
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow public insert notifications" ON notifications;
DROP POLICY IF EXISTS "Allow public read notifications" ON notifications;
DROP POLICY IF EXISTS "Allow public update notifications" ON notifications;

CREATE POLICY "Allow public insert notifications" ON notifications
  FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Allow public read notifications" ON notifications
  FOR SELECT
  USING (true);

CREATE POLICY "Allow public update notifications" ON notifications
  FOR UPDATE
  USING (true);

-- Function to check and create tribes when 5 people are reading the same book in the same city
-- This will be called by the application logic, but we can also create a trigger if needed
CREATE OR REPLACE FUNCTION check_tribe_formation()
RETURNS TRIGGER AS $$
DECLARE
  book_count INTEGER;
  existing_tribe_id TEXT;
  book_title_val TEXT;
  book_author_val TEXT;
BEGIN
  -- Only check if currentlyReading is being updated
  -- Count users in the same city reading the same book
  SELECT COUNT(DISTINCT p.id) INTO book_count
  FROM profiles p
  WHERE p.city = NEW.city
    AND p.city != ''
    AND p.books::jsonb @> (
      SELECT jsonb_build_object('books', jsonb_build_array(
        jsonb_build_object('id', (NEW.books::jsonb->'books'->0->>'id'))
      ))
    )
    AND p.id != NEW.id;
  
  -- If we have 5 or more people, check if a tribe already exists
  IF book_count >= 4 THEN  -- 4 others + current user = 5 total
    -- Extract book info from the first currently reading book
    SELECT 
      NEW.books::jsonb->'books'->0->>'title',
      NEW.books::jsonb->'books'->0->>'author',
      NEW.books::jsonb->'books'->0->>'id'
    INTO book_title_val, book_author_val, existing_tribe_id;
    
    -- Check if tribe already exists for this book+city
    SELECT id INTO existing_tribe_id
    FROM tribes
    WHERE book_id = (NEW.books::jsonb->'books'->0->>'id')
      AND city = NEW.city
      AND status IN ('forming', 'active')
    LIMIT 1;
    
    -- If no existing tribe, create one
    IF existing_tribe_id IS NULL THEN
      INSERT INTO tribes (book_id, book_title, book_author, city, status)
      VALUES (
        (NEW.books::jsonb->'books'->0->>'id'),
        book_title_val,
        book_author_val,
        NEW.city,
        'forming'
      )
      RETURNING id INTO existing_tribe_id;
      
      -- Add all 5 users to the tribe
      INSERT INTO tribe_members (tribe_id, user_id)
      SELECT existing_tribe_id, p.id
      FROM profiles p
      WHERE p.city = NEW.city
        AND p.books::jsonb @> (
          SELECT jsonb_build_object('books', jsonb_build_array(
            jsonb_build_object('id', (NEW.books::jsonb->'books'->0->>'id'))
          ))
        )
      LIMIT 5
      ON CONFLICT (tribe_id, user_id) DO NOTHING;
      
      -- Create notifications for all members
      INSERT INTO notifications (user_id, type, title, message, tribe_id, book_id)
      SELECT 
        p.id,
        'tribe_forming',
        'A Tribe is Forming!',
        'A Tribe is forming for ' || book_title_val || '. Join the 48-hour Sprint?',
        existing_tribe_id,
        (NEW.books::jsonb->'books'->0->>'id')
      FROM profiles p
      WHERE p.city = NEW.city
        AND p.books::jsonb @> (
          SELECT jsonb_build_object('books', jsonb_build_array(
            jsonb_build_object('id', (NEW.books::jsonb->'books'->0->>'id'))
          ))
        )
      LIMIT 5;
    END IF;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Note: The trigger approach above is complex due to JSONB structure
-- It's better to handle this in application logic, but the function is available if needed
