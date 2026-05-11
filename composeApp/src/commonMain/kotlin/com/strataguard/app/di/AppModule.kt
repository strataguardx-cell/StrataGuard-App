package com.strataguard.app.di

import com.strataguard.app.data.auth.AuthRepository
import com.strataguard.app.data.auth.FirebaseAuthRepository
import com.strataguard.app.ui.auth.LoginViewModel
import com.strataguard.app.ui.auth.RegisterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<AuthRepository> { FirebaseAuthRepository() }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
}