-- Supabase Matches/Likes Table Schema
-- Run this SQL in your Supabase SQL Editor to create the likes and matches tables

-- Likes table: tracks when user A likes user B
CREATE TABLE IF NOT EXISTS likes (
  id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
  liker_id TEXT NOT NULL,  -- User who liked
  liked_id TEXT NOT NULL,  -- User who was liked
  created_at TEXT DEFAULT NOW()::TEXT,
  UNIQUE(liker_id, liked_id)  -- Prevent duplicate likes
);

-- Matches table: tracks mutual likes (bindered)
CREATE TABLE IF NOT EXISTS matches (
  id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
  user1_id TEXT NOT NULL,
  user2_id TEXT NOT NULL,
  created_at TEXT DEFAULT NOW()::TEXT,
  UNIQUE(user1_id, user2_id)  -- Prevent duplicate matches
);

-- Indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_likes_liker ON likes(liker_id);
CREATE INDEX IF NOT EXISTS idx_likes_liked ON likes(liked_id);
CREATE INDEX IF NOT EXISTS idx_matches_user1 ON matches(user1_id);
CREATE INDEX IF NOT EXISTS idx_matches_user2 ON matches(user2_id);

-- RLS policies for likes
ALTER TABLE likes ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow public insert likes" ON likes;
DROP POLICY IF EXISTS "Allow public read likes" ON likes;

CREATE POLICY "Allow public insert likes" ON likes
  FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Allow public read likes" ON likes
  FOR SELECT
  USING (true);

-- RLS policies for matches
ALTER TABLE matches ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow public insert matches" ON matches;
DROP POLICY IF EXISTS "Allow public read matches" ON matches;

CREATE POLICY "Allow public insert matches" ON matches
  FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Allow public read matches" ON matches
  FOR SELECT
  USING (true);
