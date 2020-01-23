/*
 * Copyright 2015-2019 The twitlatte authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twitlatte

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager

/**
 * Created by moko256 on 2016/05/28.
 *
 * @author moko256
 */
class MyFollowFollowerFragment : Fragment(R.layout.fragment_follow_follower), ToolbarTitleInterface, NavigationPositionInterface, UseTabsInterface {
    override lateinit var tabsViewPager: ViewPager

    override val titleResourceId = R.string.following_and_followers

    override val navigationPosition = R.id.nav_follow_and_follower

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabsViewPager = view.findViewById(R.id.follow_follower_pager)

        val activity = requireActivity()
        FollowFollowerTabsPagerAdapter(
                childFragmentManager,
                activity,
                activity.requireClient().accessToken.userId
        ).initAdapter(tabsViewPager)
    }
}