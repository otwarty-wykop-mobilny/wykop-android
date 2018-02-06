package io.github.feelfreelinux.wykopmobilny.ui.modules.profile.links

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.feelfreelinux.wykopmobilny.R
import io.github.feelfreelinux.wykopmobilny.base.BaseFragment
import io.github.feelfreelinux.wykopmobilny.ui.modules.profile.ProfileActivity
import io.github.feelfreelinux.wykopmobilny.ui.modules.profile.ProfileFragmentNotifier
import io.github.feelfreelinux.wykopmobilny.utils.printout
import kotlinx.android.synthetic.main.profile_subtab_layout.*

class LinksFragment : BaseFragment(), ProfileFragmentNotifier {
    companion object {
        val DATA_FRAGMENT_TAG = "USER_LINKS_FRAGMENT"

        fun newInstance() : LinksFragment {
            val fragment = LinksFragment()
            return fragment
        }
    }

    val pagerAdapter by lazy { LinksPagerAdapter(resources, childFragmentManager) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.profile_subtab_layout, container, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser && pager.adapter != pagerAdapter) {
            pager.offscreenPageLimit = 1
            pager.adapter = pagerAdapter
            tabLayout.setupWithViewPager(pager)
        }
    }

    override fun removeDataFragment() {
        if (isAdded) {
            for (i in 0..pagerAdapter.registeredFragments.size()) {
                (pagerAdapter.registeredFragments.get(i) as? ProfileFragmentNotifier)?.removeDataFragment()
            }
        }
    }
}