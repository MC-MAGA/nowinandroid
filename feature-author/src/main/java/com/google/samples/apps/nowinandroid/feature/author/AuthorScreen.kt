/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.feature.author

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.samples.apps.nowinandroid.core.model.data.Author
import com.google.samples.apps.nowinandroid.core.model.data.FollowableAuthor
import com.google.samples.apps.nowinandroid.core.ui.LoadingWheel
import com.google.samples.apps.nowinandroid.core.ui.component.NiaFilterChip
import com.google.samples.apps.nowinandroid.core.ui.newsResourceCardItems
import com.google.samples.apps.nowinandroid.feature.author.AuthorUiState.Loading
import com.google.samples.apps.nowinandroid.feature.author.R.string

@Composable
fun AuthorRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthorViewModel = hiltViewModel(),
) {
    val uiState: AuthorScreenUiState by viewModel.uiState.collectAsState()

    AuthorScreen(
        authorState = uiState.authorState,
        newsState = uiState.newsState,
        modifier = modifier,
        onBackClick = onBackClick,
        onFollowClick = viewModel::followAuthorToggle,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@VisibleForTesting
@Composable
internal fun AuthorScreen(
    authorState: AuthorUiState,
    newsState: NewsUiState,
    onBackClick: () -> Unit,
    onFollowClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(
                // TODO: Replace with windowInsetsTopHeight after
                //       https://issuetracker.google.com/issues/230383055
                Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                )
            )
        }
        when (authorState) {
            Loading -> {
                item {
                    LoadingWheel(
                        modifier = modifier,
                        contentDesc = stringResource(id = string.author_loading),
                    )
                }
            }
            AuthorUiState.Error -> {
                TODO()
            }
            is AuthorUiState.Success -> {
                item {
                    AuthorToolbar(
                        onBackClick = onBackClick,
                        onFollowClick = onFollowClick,
                        uiState = authorState.followableAuthor,
                    )
                }
                authorBody(
                    author = authorState.followableAuthor.author,
                    news = newsState
                )
            }
        }
        item {
            Spacer(
                // TODO: Replace with windowInsetsBottomHeight after
                //       https://issuetracker.google.com/issues/230383055
                Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                )
            )
        }
    }
}

private fun LazyListScope.authorBody(
    author: Author,
    news: NewsUiState
) {
    item {
        AuthorHeader(author)
    }

    authorCards(news)
}

@Composable
private fun AuthorHeader(author: Author) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .size(216.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            model = author.imageUrl,
            contentDescription = "Author profile picture",
        )
        Text(author.name, style = MaterialTheme.typography.displayMedium)
        if (author.bio.isNotEmpty()) {
            Text(
                text = author.bio,
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun LazyListScope.authorCards(news: NewsUiState) {
    when (news) {
        is NewsUiState.Success -> {
            newsResourceCardItems(
                items = news.news,
                newsResourceMapper = { it },
                isBookmarkedMapper = { /* TODO */ false },
                onToggleBookmark = { /* TODO */ },
                itemModifier = Modifier.padding(24.dp)
            )
        }
        is NewsUiState.Loading -> item {
            LoadingWheel(contentDesc = "Loading news") // TODO
        }
        else -> item {
            Text("Error") // TODO
        }
    }
}

@Composable
private fun AuthorToolbar(
    uiState: FollowableAuthor,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onFollowClick: (Boolean) -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.back)
            )
        }
        val selected = uiState.isFollowed
        NiaFilterChip(
            modifier = Modifier.padding(horizontal = 16.dp),
            checked = selected,
            onCheckedChange = onFollowClick,
        ) {
            if (selected) {
                Text(stringResource(id = string.author_following))
            } else {
                Text(stringResource(id = string.author_not_following))
            }
        }
    }
}

@Preview
@Composable
private fun AuthorBodyPreview() {
    MaterialTheme {
        LazyColumn {
            authorBody(
                author = Author(
                    id = "0",
                    name = "Android Dev",
                    bio = "Works on Compose",
                    twitter = "dev",
                    mediumPage = "",
                    imageUrl = "",
                ),
                news = NewsUiState.Success(emptyList())
            )
        }
    }
}
