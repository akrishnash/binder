package com.binder.utils

import com.binder.R

object GenreColorMapper {
    // Map genres to gradient colors for "Reading Aura"
    fun getGenreAuraColors(genre: String): Pair<Int, Int> {
        return when (genre.lowercase()) {
            "sci-fi", "science fiction" -> Pair(R.color.aura_sci_fi_start, R.color.aura_sci_fi_end)
            "noir" -> Pair(R.color.aura_noir_start, R.color.aura_noir_end)
            "fantasy" -> Pair(R.color.aura_fantasy_start, R.color.aura_fantasy_end)
            "mystery" -> Pair(R.color.aura_mystery_start, R.color.aura_mystery_end)
            "romance" -> Pair(R.color.aura_romance_start, R.color.aura_romance_end)
            "thriller" -> Pair(R.color.aura_thriller_start, R.color.aura_thriller_end)
            "horror" -> Pair(R.color.aura_horror_start, R.color.aura_horror_end)
            "historical fiction" -> Pair(R.color.aura_historical_start, R.color.aura_historical_end)
            "literary fiction" -> Pair(R.color.aura_literary_start, R.color.aura_literary_end)
            "young adult", "ya" -> Pair(R.color.aura_ya_start, R.color.aura_ya_end)
            "biography", "memoir" -> Pair(R.color.aura_biography_start, R.color.aura_biography_end)
            "poetry" -> Pair(R.color.aura_poetry_start, R.color.aura_poetry_end)
            "philosophy" -> Pair(R.color.aura_philosophy_start, R.color.aura_philosophy_end)
            "science" -> Pair(R.color.aura_science_start, R.color.aura_science_end)
            "history" -> Pair(R.color.aura_history_start, R.color.aura_history_end)
            "art & design", "art" -> Pair(R.color.aura_art_start, R.color.aura_art_end)
            "graphic novels" -> Pair(R.color.aura_graphic_start, R.color.aura_graphic_end)
            "comedy" -> Pair(R.color.aura_comedy_start, R.color.aura_comedy_end)
            "drama" -> Pair(R.color.aura_drama_start, R.color.aura_drama_end)
            else -> Pair(R.color.aura_default_start, R.color.aura_default_end)
        }
    }
    
    fun getPrimaryGenre(genres: List<String>): String {
        return genres.firstOrNull() ?: "Mystery"
    }
}
