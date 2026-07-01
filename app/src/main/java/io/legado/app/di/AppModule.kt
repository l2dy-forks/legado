package io.legado.app.di

import io.legado.app.data.repository.DirectLinkUploadRepository
import io.legado.app.data.repository.UploadRepository
import io.legado.app.ui.about.CrashViewModel
import io.legado.app.ui.book.explore.ExploreShowViewModel
import io.legado.app.ui.book.search.SearchViewModel
import io.legado.app.ui.book.toc.rule.TxtTocRuleViewModel
import io.legado.app.ui.dict.DictViewModel
import io.legado.app.ui.dict.rule.DictRuleViewModel
import io.legado.app.ui.main.MainViewModel
import io.legado.app.ui.main.bookshelf.BookshelfViewModel
import io.legado.app.ui.main.explore.ExploreViewModel
import io.legado.app.ui.main.rss.RssViewModel
import io.legado.app.ui.replace.ReplaceEditRoute
import io.legado.app.ui.replace.ReplaceRuleViewModel
import io.legado.app.ui.replace.edit.ReplaceEditViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::SearchViewModel)
    viewModelOf(::ExploreShowViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::BookshelfViewModel)
    viewModelOf(::ExploreViewModel)
    viewModelOf(::RssViewModel)
    viewModelOf(::DictViewModel)
    viewModelOf(::CrashViewModel)

    // Rule management pages
    viewModelOf(::TxtTocRuleViewModel)
    viewModelOf(::DictRuleViewModel)
    viewModelOf(::ReplaceRuleViewModel)

    // ReplaceEdit (keyed by route session)
    viewModel { (route: ReplaceEditRoute) ->
        ReplaceEditViewModel(get(), get(), route)
    }

    // Upload
    single<UploadRepository> { DirectLinkUploadRepository() }
}
