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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.udacity.asteroidradar.Asteroid
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.AsteroidItemBinding

/**
 * Adapter for main screen with asteroids and picture of the day showing.
 * The DevByte Viewer in Lesson 3 was a great model for this
 */
class MainAdapter(val clickListener: AsteroidClickListener) :
    ListAdapter<Asteroid, MainAdapter.AsteroidViewHolder>(AsteroidDiffCallback) {
    /**
     * Callback for calculating the diff between two non-null items in a list.
     *
     * Used by ListAdapter to calculate the minimum number of changes between an old and new list
     * that's been passed to 'submitList'
     */
    companion object AsteroidDiffCallback : DiffUtil.ItemCallback<Asteroid>() {
        override fun areItemsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean =
            oldItem.id == newItem.id
    }

    /**
     * Asteroid viewHolder class with bind function for asteroid items
     */
    class AsteroidViewHolder(val binding: AsteroidItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: AsteroidClickListener, asteroid: Asteroid) {
            binding.asteroid = asteroid
            binding.asteroidCallback = listener
            val asteroidCodeNameString = binding.root.context.getString(R.string.asteroid_code_name)
            val asteroidCloseApproachDateString =
                binding.root.context.getString(R.string.close_approach_date)
            val asteroidPotentiallyHazardousString =
                binding.root.context.getString(R.string.potentially_hazardous)
            itemView.contentDescription =
                asteroidCodeNameString + asteroid.codename +
                        asteroidCloseApproachDateString + asteroid.closeApproachDate +
                        asteroidPotentiallyHazardousString + asteroid.isPotentiallyHazardous
        }

        /**
         * companion object has from function for layout binding the viewHolder asteroid items
         */
        companion object {
            fun from(parent: ViewGroup): AsteroidViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AsteroidItemBinding.inflate(layoutInflater, parent, false)
                return AsteroidViewHolder(binding)
            }
        }
    }

    /**
     * create the viewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsteroidViewHolder {
        return AsteroidViewHolder.from(parent)
    }

    /**
     * bind the viewHolder
     */
    override fun onBindViewHolder(holder: AsteroidViewHolder, position: Int) {
        holder.bind(clickListener, getItem(position))
    }

    /**
     * click listener for recyclerview items
     */
    class AsteroidClickListener(val clickListener: (Asteroid) -> Unit) {
        fun onClick(asteroid: Asteroid) = clickListener(asteroid)
    }
}