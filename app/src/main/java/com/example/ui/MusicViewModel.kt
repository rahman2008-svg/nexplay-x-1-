package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.playback.MusicPlaybackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MusicDatabase.getDatabase(application)
    private val repository = MusicRepository(database.musicDao())
    val playbackManager = MusicPlaybackManager(application)

    // Active screen navigation
    // 0: Home, 1: Library, 2: Search, 3: Playlists, 4: Equalizer & Settings
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Full song library state
    val allSongs: StateFlow<List<SongEntity>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<SongEntity>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playbackHistory: StateFlow<List<PlaybackHistoryEntity>> = repository.playbackHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMoodFilter = MutableStateFlow<String?>(null) // "Chill", "Energy", "Focus", or null
    val selectedMoodFilter: StateFlow<String?> = _selectedMoodFilter.asStateFlow()

    private val _selectedFolderFilter = MutableStateFlow<String?>(null)
    val selectedFolderFilter: StateFlow<String?> = _selectedFolderFilter.asStateFlow()

    // Dynamic filtering combo
    val filteredSongs: StateFlow<List<SongEntity>> = combine(
        allSongs,
        _searchQuery,
        _selectedMoodFilter,
        _selectedFolderFilter
    ) { songs, query, mood, folder ->
        var list = songs
        if (query.isNotEmpty()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
            }
        }
        if (mood != null) {
            list = list.filter { it.genre.equals(mood, ignoreCase = true) }
        }
        if (folder != null) {
            list = list.filter { it.folder == folder }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique folders determined dynamically
    val folders: StateFlow<List<String>> = allSongs.map { songs ->
        songs.map { it.folder }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique artists determined dynamically
    val artists: StateFlow<List<String>> = allSongs.map { songs ->
        songs.map { it.artist }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique albums determined dynamically
    val albums: StateFlow<List<String>> = allSongs.map { songs ->
        songs.map { it.album }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active playlist being viewed detail-wise
    private val _currentPlaylistId = MutableStateFlow<Long?>(null)
    val currentPlaylistId: StateFlow<Long?> = _currentPlaylistId.asStateFlow()

    val currentPlaylistSongs: StateFlow<List<SongEntity>> = _currentPlaylistId
        .flatMapLatest { id ->
            if (id != null) repository.getSongsInPlaylist(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for details sheet expansion
    private val _showPlayerSheet = MutableStateFlow(false)
    val showPlayerSheet: StateFlow<Boolean> = _showPlayerSheet.asStateFlow()

    init {
        // Automatically sync and seed on database activation
        syncLibrary()

        // Set song completed action to auto register play counts
        playbackManager.onSongCompleted = { completedSong ->
            viewModelScope.launch(Dispatchers.IO) {
                repository.recordPlay(completedSong.id)
            }
        }
    }

    fun setTab(index: Int) {
        _activeTab.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty() && _activeTab.value != 2) {
            _activeTab.value = 2 // Switch to Search tab when typing if not already there
        }
    }

    fun setMoodFilter(mood: String?) {
        _selectedMoodFilter.value = mood
    }

    fun setFolderFilter(folder: String?) {
        _selectedFolderFilter.value = folder
    }

    fun showPlayer(show: Boolean) {
        _showPlayerSheet.value = show
    }

    fun viewPlaylist(playlistId: Long?) {
        _currentPlaylistId.value = playlistId
    }

    fun syncLibrary() {
        viewModelScope.launch {
            repository.syncMusicLibrary(getApplication())
        }
    }

    // Play operations
    fun playSong(song: SongEntity, fromList: List<SongEntity>) {
        viewModelScope.launch {
            val idx = fromList.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            playbackManager.playQueue(fromList, idx)
            repository.recordPlay(song.id)
        }
    }

    fun playAll(songs: List<SongEntity>) {
        if (songs.isNotEmpty()) {
            playSong(songs[0], songs)
        }
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSongFavorite(song.id, !song.isFavorite)
        }
    }

    // Playlist CRUD
    fun createPlaylist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlaylist(id)
            if (_currentPlaylistId.value == id) {
                _currentPlaylistId.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    // Smart mix playlist based on Current Hour + System Time
    fun playTimeBasedSmartMix() {
        viewModelScope.launch {
            val songs = allSongs.value
            if (songs.isEmpty()) return@launch

            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val smartList = if (hour in 5..15) {
                // Morning/Afternoon energetic mix, filter to Energy mode + randoms
                songs.filter { it.genre == "Energy" }.shuffled().take(10).ifEmpty { songs.shuffled().take(5) }
            } else {
                // Evening/Night calm mix, filter to Chill mode + randoms
                songs.filter { it.genre == "Chill" }.shuffled().take(10).ifEmpty { songs.shuffled().take(5) }
            }
            playAll(smartList)
        }
    }

    // Play random folder-based mix
    fun playFolderSmartMix() {
        viewModelScope.launch {
            val songs = allSongs.value
            val folderList = folders.value
            if (songs.isEmpty() || folderList.isEmpty()) return@launch
            
            val randomFolder = folderList.random()
            val folderSongs = songs.filter { it.folder == randomFolder }.shuffled()
            playAll(folderSongs)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.release()
    }
}
