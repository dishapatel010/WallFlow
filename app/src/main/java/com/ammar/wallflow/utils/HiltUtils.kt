package com.ammar.wallflow.utils

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel as hiltViewModelCompose
import androidx.lifecycle.ViewModel

@Composable
inline fun <reified VM : ViewModel> hiltViewModel(key: String): VM =
    hiltViewModelCompose(key = key)
