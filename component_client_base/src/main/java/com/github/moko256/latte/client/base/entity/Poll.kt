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

package com.github.moko256.latte.client.base.entity

import java.util.*

/**
 * Created by moko256 on 2019/03/07.
 *
 * @author moko256
 */
data class Poll(
        val id: Long,
        val expiresAt: Date?,
        val expired: Boolean,
        val multiple: Boolean,
        val votesCount: Int,
        val optionTitles: List<String>,
        val optionCounts: List<Int>,
        val voted: Boolean
) {
        override fun hashCode(): Int {
            return id.toInt()
        }

        override fun equals(other: Any?): Boolean {
            return this === other || other is User && other.id == this.id
        }
}