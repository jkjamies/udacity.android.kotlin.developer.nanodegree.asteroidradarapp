/*
 *
 * PROJECT LICENSE
 *
 * This project was submitted by Jason Jamieson as part of the Android Kotlin Developer Nanodegree At Udacity.
 *
 * As part of Udacity Honor code, your submissions must be of your own work.
 * Submission of this project will cause you to break the Udacity Honor Code
 * and possible suspension of your account.
 *
 * I, Jason Jamieson, the author of the project, allow you to check this code as reference, but if
 * used as submission, it's your responsibility if you are expelled.
 *
 * Copyright (c) 2021 Jason Jamieson
 *
 * Besides the above notice, the following license applies and this license notice
 * must be included in all works derived from this project.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.domain.Filter
import com.udacity.asteroidradar.repository.Repository
import kotlinx.coroutines.launch

/**
 * Main View Model for picture of the day and list of asteroids
 * The DevByte Viewer in Lesson 3 was a great model for this
 */
@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // database
    private val database = getDatabase(application)

    // repository
    private val repository = Repository(database)

    private val _navigateToAsteroidDetails = MutableLiveData<Asteroid>()
    val navigateToAsteroidDetails: LiveData<Asteroid>
        get() = _navigateToAsteroidDetails

    init {
        // initially refresh the asteroid and pictures of the day
        viewModelScope.launch {
            repository.apply {
                refreshAsteroids()
                refreshPictureOfTheDay()
            }
        }
    }

    // asteroids list
    val asteroidsList: LiveData<List<Asteroid>> = repository.asteroids

    // picture of the day
    val pictureOfTheDay = repository.pictureOfTheDay

    /**
     * handle asteroid click event on recyclerview item from adapter
     */
    fun navigateToAsteroidDetail(asteroid: Asteroid) {
        _navigateToAsteroidDetails.value = asteroid
    }

    /**
     * handle navigate completed
     */
    fun onAsteroidNavigateNavigated() {
        _navigateToAsteroidDetails.value = null
    }

    /**
     * apply [filter] from menu option from main fragment to repository for asteroid list
     */
    fun applyAsteroidFilter(filter: Filter) {
        repository.applyAsteroidFilter(filter)
    }

    /**
     * ViewModelProvider Factory class
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("unchecked_cast")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}