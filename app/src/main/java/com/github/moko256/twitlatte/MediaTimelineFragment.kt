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
import com.github.moko256.latte.client.base.entity.Paging
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.viewmodel.ListViewModel

/**
 * Created by moko256 on 2018/03/10.
 *
 * @author moko256
 */
class MediaTimelineFragment : BaseTweetListFragment(), ToolbarTitleInterface {

    override val listRepository = object : ListViewModel.ListRepository() {
        var userId = 0L
        override fun onCreate(client: Client, bundle: Bundle) {
            super.onCreate(client, bundle)
            userId = bundle.getLong("userId")
        }

        override fun name() = "MediaTimeline_$userId"
        override fun request(paging: Paging) = client.apiClient.getMediasTimeline(userId, paging)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (recyclerView.adapter as StatusesAdapter).shouldShowMediaOnly = true
    }

    override val titleResourceId = R.string.media

    companion object {

        fun newInstance(userId: Long): MediaTimelineFragment {
            return MediaTimelineFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                }
            }
        }
    }
}