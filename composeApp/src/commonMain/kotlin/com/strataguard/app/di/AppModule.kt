package com.strataguard.app.di

import com.strataguard.app.data.auth.AuthRepository
import com.strataguard.app.data.auth.FirebaseAuthRepository
import com.strataguard.app.data.evidence.EvidenceRepository
import com.strataguard.app.data.evidence.FirebaseEvidenceRepository
import com.strataguard.app.data.strata.FirestoreStrataRepository
import com.strataguard.app.data.strata.StrataRepository
import com.strataguard.app.ui.auth.LoginViewModel
import com.strataguard.app.ui.auth.RegisterViewModel
import com.strataguard.app.ui.evidence.EvidenceViewModel
import com.strataguard.app.ui.strata.SearchStrataViewModel
import com.strataguard.app.ui.strata.StrataPlanDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<AuthRepository> { FirebaseAuthRepository() }
    single<StrataRepository> { FirestoreStrataRepository() }
    single<EvidenceRepository> { FirebaseEvidenceRepository() }

    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { SearchStrataViewModel(get()) }
    viewModel { params -> StrataPlanDetailViewModel(get(), params.get()) }
    viewModel { EvidenceViewModel(get()) }
}