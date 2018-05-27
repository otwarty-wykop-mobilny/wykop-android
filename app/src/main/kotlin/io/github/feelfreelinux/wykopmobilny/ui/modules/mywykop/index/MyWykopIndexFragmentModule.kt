package io.github.feelfreelinux.wykopmobilny.ui.modules.mywykop.index

import dagger.Module
import dagger.Provides
import io.github.feelfreelinux.wykopmobilny.api.entries.EntriesApi
import io.github.feelfreelinux.wykopmobilny.api.links.LinksApi
import io.github.feelfreelinux.wykopmobilny.api.mywykop.MyWykopApi
import io.github.feelfreelinux.wykopmobilny.base.Schedulers
import io.github.feelfreelinux.wykopmobilny.ui.fragments.entries.EntriesInteractor
import io.github.feelfreelinux.wykopmobilny.ui.fragments.links.LinksInteractor

@Module
class MyWykopIndexFragmentModule {
    @Provides
    fun provideMyWykopIndexFragmentPresenter(schedulers: Schedulers, myWykopApi: MyWykopApi, linksInteractor: LinksInteractor, entriesInteractor: EntriesInteractor, entriesApi: EntriesApi, linksApi: LinksApi) =
            MyWykopIndexPresenter(schedulers, myWykopApi, entriesApi, entriesInteractor, linksInteractor, linksApi)
}