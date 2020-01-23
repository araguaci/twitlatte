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

package com.github.moko256.twitlatte.api

import com.github.moko256.latte.client.base.MediaUrlConverter
import com.github.moko256.latte.client.mastodon.CLIENT_TYPE_MASTODON
import com.github.moko256.latte.client.mastodon.MastodonMediaUrlConverter
import com.github.moko256.latte.client.twitter.CLIENT_TYPE_TWITTER
import com.github.moko256.latte.client.twitter.TwitterMediaUrlConverter

/**
 * Created by moko256 on 2019/04/07.
 *
 * @author moko256
 */

fun generateMediaUrlConverter(clientType: Int): MediaUrlConverter {
    return when (clientType) {
        CLIENT_TYPE_TWITTER -> TwitterMediaUrlConverter
        CLIENT_TYPE_MASTODON -> MastodonMediaUrlConverter
        else -> error("Invalid clientType: $clientType")
    }
}