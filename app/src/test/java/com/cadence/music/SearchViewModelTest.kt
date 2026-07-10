package com.cadence.music

import app.cash.turbine.test
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.usecase.music.SearchSongsUseCase
import com.cadence.music.playback.PlayerController
import com.cadence.music.presentation.search.SearchViewModel
import com.cadence.music.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/** Exercises debounce + result mapping without touching Firestore/Room — the ViewModel only
 *  knows about SearchSongsUseCase, so everything below it is a plain mock. */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var searchSongs: SearchSongsUseCase
    private lateinit var playerController: PlayerController
    private lateinit var viewModel: SearchViewModel

    private val sampleSong = Song(
        id = "1", title = "Test Song", artistId = "a1", artistName = "Test Artist", durationMs = 180_000
    )

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        searchSongs = mockk()
        playerController = mockk(relaxed = true)
        viewModel = SearchViewModel(searchSongs, playerController)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `blank query never calls the search use case`() = runTest(dispatcher) {
        viewModel.onQueryChange("")
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.results.isEmpty())
        }
    }

    @Test
    fun `typed query eventually surfaces results after debounce`() = runTest(dispatcher) {
        coEvery { searchSongs("indie") } returns Resource.Success(listOf(sampleSong))

        viewModel.onQueryChange("indie")
        dispatcher.scheduler.advanceTimeBy(350) // clear the 300ms debounce window
        dispatcher.scheduler.runCurrent()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.results == listOf(sampleSong))
            assert(!state.isSearching)
        }
    }

    @Test
    fun `search error surfaces as an error message not a crash`() = runTest(dispatcher) {
        coEvery { searchSongs("xyz") } returns Resource.Error("offline")

        viewModel.onQueryChange("xyz")
        dispatcher.scheduler.advanceTimeBy(350)
        dispatcher.scheduler.runCurrent()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.errorMessage == "offline")
        }
    }
}
