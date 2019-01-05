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

package com.github.moko256.latte.client.mastodon.date

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by moko256 on 2018/09/02.
 *
 * @author moko256
 */

@SuppressWarnings("SimpleDateFormat")
private val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").apply {
    timeZone = TimeZone.getTimeZone("GMT")
}

internal fun String.toISO8601Date(): Date {
    try {
        synchronized(dateParser) {
            return dateParser.parse(this)
        }
    } catch (e: ParseException) {
        e.printStackTrace()
        return Date(0)
    }

}