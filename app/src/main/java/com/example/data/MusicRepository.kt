package com.example.data

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

class MusicRepository(private val musicDao: MusicDao) {

    val allSongs: Flow<List<SongEntity>> = musicDao.getAllSongs()
    val favoriteSongs: Flow<List<SongEntity>> = musicDao.getFavoriteSongs()
    val playlists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()
    val playbackHistory: Flow<List<PlaybackHistoryEntity>> = musicDao.getPlaybackHistory()

    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>> {
        return musicDao.getSongsInPlaylist(playlistId)
    }

    suspend fun setSongFavorite(songId: String, isFavorite: Boolean) {
        musicDao.setFavorite(songId, isFavorite)
    }

    suspend fun recordPlay(songId: String) {
        musicDao.recordSongPlay(songId, System.currentTimeMillis())
        val song = musicDao.getSongById(songId)
        if (song != null) {
            val mood = when (song.genre) {
                "Focus" -> "Focus"
                "Energy" -> "Energy"
                else -> "Chill"
            }
            musicDao.insertHistory(
                PlaybackHistoryEntity(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    moodScored = mood
                )
            )
        }
    }

    suspend fun createPlaylist(name: String): Long {
        return musicDao.createPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        musicDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun syncMusicLibrary(context: Context) {
        val scanned = scanMediaStore(context)
        val existing = musicDao.getAllSongs().firstOrNull() ?: emptyList()

        if (scanned.isNotEmpty()) {
            musicDao.insertSongs(scanned)
        }

        // If even after scanning there are no songs, seed our gorgeous standard smart library!
        if (scanned.isEmpty() && existing.isEmpty()) {
            val seeded = getSeededSongs()
            musicDao.insertSongs(seeded)
            
            // Seed a couple default smart playlists for first use as requested!
            val morningId = musicDao.createPlaylist(PlaylistEntity(name = "☀️ Morning Energy Mix"))
            val nightId = musicDao.createPlaylist(PlaylistEntity(name = "🌙 Night Calm Beats"))
            val focusId = musicDao.createPlaylist(PlaylistEntity(name = "🧠 Deep Focus Session"))

            // Associate some seeded songs with them
            musicDao.addSongToPlaylist(PlaylistSongCrossRef(morningId, "synth_pulse"))
            musicDao.addSongToPlaylist(PlaylistSongCrossRef(morningId, "morning_glory"))
            musicDao.addSongToPlaylist(PlaylistSongCrossRef(nightId, "midnight_rain"))
            musicDao.addSongToPlaylist(PlaylistSongCrossRef(nightId, "stars_rahman"))
            musicDao.addSongToPlaylist(PlaylistSongCrossRef(focusId, "focus_waves"))
        }
    }

    private fun scanMediaStore(context: Context): List<SongEntity> {
        val songsList = mutableListOf<SongEntity>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol).toString()
                    val title = cursor.getString(titleCol) ?: "Unknown Song"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val path = cursor.getString(dataCol) ?: ""
                    val duration = cursor.getLong(durationCol)

                    val folder = if (path.isNotEmpty()) {
                        File(path).parentFile?.name ?: "Internal Memory"
                    } else {
                        "Internal Memory"
                    }

                    val normalizedArtist = if (artist == "<unknown>" || artist.trim().isEmpty()) "Unknown Artist" else artist
                    val normalizedAlbum = if (album == "<unknown>" || album.trim().isEmpty()) "Unknown Album" else album

                    // Dynamic Smart Mood tag
                    val genre = when {
                        title.contains("chill", true) || path.contains("lofi", true) -> "Chill"
                        title.contains("rock", true) || title.contains("workout", true) || title.contains("energy", true) -> "Energy"
                        title.contains("focus", true) || title.contains("study", true) || title.contains("ambient", true) -> "Focus"
                        else -> "Chill"
                    }

                    songsList.add(
                        SongEntity(
                            id = "local_$id",
                            title = title,
                            artist = normalizedArtist,
                            album = normalizedAlbum,
                            path = path,
                            duration = if (duration > 0) duration else 180000L,
                            folder = folder,
                            genre = genre
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songsList
    }

    private fun getSeededSongs(): List<SongEntity> {
        return listOf(
            SongEntity(
                id = "synth_pulse",
                title = "NexVora Pulse (Beats)",
                artist = "NexVora Lab's Ofc",
                album = "Ecosystem Alpha",
                path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                duration = 372000,
                folder = "NexVora Beats",
                genre = "Energy",
                isFavorite = true
            ),
            SongEntity(
                id = "midnight_rain",
                title = "Midnight Rain (Lofi)",
                artist = "Prince AR Abdur Rahman",
                album = "Midnight Diary",
                path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                duration = 423000,
                folder = "Prince's Studio",
                genre = "Chill"
            ),
            SongEntity(
                id = "focus_waves",
                title = "Deep Focus Ambient",
                artist = "Alpha Waves Studio",
                album = "Mind Sync",
                path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                duration = 302000,
                folder = "Brainwave Labs",
                genre = "Focus"
            ),
            SongEntity(
                id = "stars_rahman",
                title = "Starlight Whispers",
                artist = "Prince AR feat. Rahman",
                album = "Symphony of Stars",
                path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                duration = 318000,
                folder = "Classic Gems",
                genre = "Chill"
            ),
            SongEntity(
                id = "morning_glory",
                title = "Morning Sunrise (Energetic)",
                artist = "NexVora Lab Orchestrator",
                album = "Day Planner Sync",
                path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                duration = 279000,
                folder = "NexVora Beats",
                genre = "Energy"
            )
        )
    }
}
