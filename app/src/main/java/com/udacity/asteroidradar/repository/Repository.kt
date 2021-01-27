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

package com.udacity.asteroidradar.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.domain.Filter
import com.udacity.asteroidradar.domain.asDatabaseModel
import com.udacity.asteroidradar.domain.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate

/**
 * Repository for asteroids and picture of the day
 */
class Repository(private val database: AsteroidsDatabase) {

    // filter for the asteroid list defaulted to week
    // conceptual suggestions provided in the udacity chat
    private val _filter =
        MutableLiveData(Filter.WEEK)
    val filter: LiveData<Filter>
        get() = _filter

    // start date is 'now' - this library is only available for Android O+, but it's simple
    // so to make it work for older devices, a calendar object with manipulation might be needed
    // so maybe this is ok just for this project?
    @RequiresApi(Build.VERSION_CODES.O)
    private val startDate = LocalDate.now()

    // again, see above comment, but using this is just so easy - project calls for 'today' and 'a week'
    @RequiresApi(Build.VERSION_CODES.O)
    private val endDate = startDate.plusDays(7)

    // switch map to transformations map list of asteroids as domain models to help filter the list
    // using week, today, all (saved - using db)
    // https://developer.android.com/reference/android/arch/lifecycle/Transformations
    // https://developer.android.com/reference/androidx/lifecycle/Transformations#switchMap(androidx.lifecycle.LiveData%3CX%3E,%20androidx.arch.core.util.Function%3CX,%20androidx.lifecycle.LiveData%3CY%3E%3E)
    @RequiresApi(Build.VERSION_CODES.O)
    val asteroids: LiveData<List<Asteroid>> = Transformations.switchMap(filter) { filter ->
        when (filter) {
            Filter.WEEK -> Transformations.map(
                database.asteroidDao.getAsteroidsWeekly(
                    startDate.toString(),
                    endDate.toString()
                )
            ) {
                it.asDomainModel()
            }
            Filter.TODAY -> Transformations.map(
                database.asteroidDao.getAsteroidsToday(
                    startDate.toString()
                )
            ) {
                it.asDomainModel()
            }
            else -> Transformations.map(database.asteroidDao.getAsteroids()) {
                it.asDomainModel()
            }
        }
    }

    // picture of the day
    val pictureOfTheDay: LiveData<PictureOfDay> =
        Transformations.map(database.pictureOfTheDayDao.getPictureOfTheDay()) {
            it?.asDomainModel()
        }

    /**
     * Apply the filter for the asteroid list
     * just as stated above, the conceptual suggestions were provided in the udacity chat
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun applyAsteroidFilter(filter: Filter) {
        _filter.value = filter
    }

    /**
     * refresh the asteroids, delete old ones when refreshing
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            // try and catch if no network when started
            try {
                // remove old asteroids previous to start date
                database.asteroidDao.removeAsteroids(startDate.toString())
                // get asteroids from start date to end date
                val response = Network.asteroidService.getAsteroids(
                    startDate.toString(),
                    endDate.toString()
                ).await()
                database.asteroidDao.insertAll(*response.asDatabaseModel())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * refresh the picture of the day
     */
    suspend fun refreshPictureOfTheDay() {
        withContext(Dispatchers.IO) {
            // try and catch if no network when started
            try {
                val response = Network.asteroidService.getPctureOfTheDay().await()
                database.pictureOfTheDayDao.insertAll(response.asDatabaseModel())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}