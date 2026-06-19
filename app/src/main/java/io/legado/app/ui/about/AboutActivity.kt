package io.legado.app.ui.about

import android.os.Bundle
import io.legado.app.R
import io.legado.app.base.BaseActivity
import io.legado.app.databinding.ActivityConfigBinding
import io.legado.app.utils.gone
import io.legado.app.utils.viewbindingdelegate.viewBinding

class AboutActivity : BaseActivity<ActivityConfigBinding>() {

    override val binding by viewBinding(ActivityConfigBinding::inflate)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.titleBar.gone()
        val fTag = "aboutFragment"
        var aboutFragment = supportFragmentManager.findFragmentByTag(fTag)
        if (aboutFragment == null) aboutFragment = AboutComposeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.configFrameLayout, aboutFragment, fTag)
            .commit()
    }

}