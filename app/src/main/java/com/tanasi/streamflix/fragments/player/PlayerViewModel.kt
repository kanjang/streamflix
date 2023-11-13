package com.tanasi.streamflix.fragments.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanasi.streamflix.models.Video
import com.tanasi.streamflix.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerViewModel(
    videoType: PlayerFragment.VideoType,
    id: String,
) : ViewModel() {

    private val _state = MutableLiveData<State>(State.Loading)
    val state: LiveData<State> = _state

    sealed class State {
        object Loading : State()
        data class SuccessLoading(val video: Video) : State()
        data class FailedLoading(val error: Exception) : State()
    }

    init {
        getVideo(videoType, id)
    }


    private fun getVideo(
        videoType: PlayerFragment.VideoType,
        id: String,
    ) = viewModelScope.launch(Dispatchers.IO) {
        _state.postValue(State.Loading)

        try {
            val servers = UserPreferences.currentProvider!!.getServers(id, videoType)

            val video = UserPreferences.currentProvider!!.getVideo(servers[0])

            if (video.sources.isEmpty()) throw Exception("No links found")

            _state.postValue(State.SuccessLoading(video))
        } catch (e: Exception) {
            _state.postValue(State.FailedLoading(e))
        }
    }
}