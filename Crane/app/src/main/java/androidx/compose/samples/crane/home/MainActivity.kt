/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.compose.samples.crane.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.animation.FloatPropKey
import androidx.animation.Spring.StiffnessLow
import androidx.animation.spring
import androidx.animation.transitionDefinition
import androidx.animation.tween
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.remember
import androidx.compose.samples.crane.base.CraneScaffold
import androidx.compose.samples.crane.calendar.launchCalendarActivity
import androidx.compose.samples.crane.data.ExploreModel
import androidx.compose.samples.crane.details.launchDetailsActivity
import androidx.compose.samples.crane.util.observe
import androidx.compose.state
import androidx.ui.animation.DpPropKey
import androidx.ui.animation.transition
import androidx.ui.core.Modifier
import androidx.ui.core.drawOpacity
import androidx.ui.core.setContent
import androidx.ui.layout.Column
import androidx.ui.layout.Spacer
import androidx.ui.layout.Stack
import androidx.ui.layout.padding
import androidx.ui.unit.Dp
import androidx.ui.unit.dp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel by viewModels<MainViewModel>()

        setContent {
            CraneScaffold {
                val destinations = observe(viewModel.suggestedDestinations, this)

                val onExploreItemClicked: OnExploreItemClicked = remember {
                    { launchDetailsActivity(context = this, item = it) }
                }
                val onDateSelectionClicked = remember {
                    { launchCalendarActivity(this) }
                }

                val splashShown = state { SplashState.SHOWN }
                val transition = transition(splashTransitionDefinition, splashShown.value)
                Stack {
                    LandingScreen(
                        modifier = Modifier.drawOpacity(transition[splashAlphaKey]),
                        splashShownState = splashShown
                    )
                    MainContentWrapper(
                        modifier = Modifier.drawOpacity(transition[contentAlphaKey]),
                        topPadding = transition[contentTopPaddingKey],
                        onExploreItemClicked = onExploreItemClicked,
                        onDateSelectionClicked = onDateSelectionClicked,
                        destinations = destinations ?: emptyList(),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContentWrapper(
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp,
    viewModel: MainViewModel,
    destinations: List<ExploreModel>,
    onExploreItemClicked: OnExploreItemClicked,
    onDateSelectionClicked: () -> Unit
) {
    Column(modifier = modifier) {
        Spacer(Modifier.padding(top = topPadding))
        MainContent(
            viewModel = viewModel,
            destinations = destinations,
            onExploreItemClicked = onExploreItemClicked,
            onDateSelectionClicked = onDateSelectionClicked
        )
    }
}

enum class SplashState { SHOWN, COMPLETED }

private val splashAlphaKey = FloatPropKey()
private val contentAlphaKey = FloatPropKey()
private val contentTopPaddingKey = DpPropKey()

private val splashTransitionDefinition = transitionDefinition {
    state(SplashState.SHOWN) {
        this[splashAlphaKey] = 1f
        this[contentAlphaKey] = 0f
        this[contentTopPaddingKey] = 100.dp
    }
    state(SplashState.COMPLETED) {
        this[splashAlphaKey] = 0f
        this[contentAlphaKey] = 1f
        this[contentTopPaddingKey] = 0.dp
    }
    transition {
        splashAlphaKey using tween<Float>(
            durationMillis = 100
        )
        contentAlphaKey using tween<Float>(
            durationMillis = 300
        )
        contentTopPaddingKey using spring<Dp>(
            stiffness = StiffnessLow
        )
    }
}
