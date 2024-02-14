package kz.devs.aiturm.di

import kz.devs.aiturm.login.presentation.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    viewModel {
        LoginViewModel()
    }
}