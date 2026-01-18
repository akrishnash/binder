// Supabase configuration
// To enable Supabase, install: npm install @supabase/supabase-js
// Then uncomment and configure the code below

// Replace these with your Supabase project URL and anon key
// You can find these in your Supabase project settings
// Example: 'https://your-project.supabase.co'
const SUPABASE_URL = 'https://nslffpqvdnhrlefpurhy.supabase.co';
// Example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
const SUPABASE_ANON_KEY = 'sb_publishable_2FO0Ogo67lHJ0T4bJMlaeA_OHCdPeuL';

// Validate that Supabase credentials are configured
const isConfigured = 
  SUPABASE_URL && 
  SUPABASE_URL !== 'YOUR_SUPABASE_URL' && 
  SUPABASE_ANON_KEY && 
  SUPABASE_ANON_KEY !== 'YOUR_SUPABASE_ANON_KEY' &&
  (SUPABASE_URL.startsWith('http://') || SUPABASE_URL.startsWith('https://'));

// Get Supabase client (lazy-loaded to avoid build errors)
export const getSupabase = async () => {
  if (!isConfigured) {
    console.log('[Supabase] Not configured, returning null');
    return null;
  }
  
  try {
    // Dynamic import to avoid breaking the build if package is not installed
    const supabaseModule = await import('@supabase/supabase-js');
    const { createClient } = supabaseModule;
    
    if (!createClient) {
      console.warn('[Supabase] createClient not found in module');
      return null;
    }
    
    const client = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
    console.log('[Supabase] Client created successfully');
    return client;
  } catch (error) {
    console.warn('[Supabase] Failed to load Supabase (package may not be installed):', error.message);
    return null;
  }
};

export const isSupabaseConfigured = () => isConfigured;
