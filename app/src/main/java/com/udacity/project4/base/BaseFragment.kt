package com.udacity.project4.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment<VDB : ViewDataBinding> : Fragment(), IBaseFragment {

    protected lateinit var mBinding: VDB

    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val _viewModel: BaseViewModel

    abstract fun layoutViewDataBinding(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            DataBindingUtil.inflate(layoutInflater, layoutViewDataBinding(), container, false)
        initData(arguments)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initActions()
        initObservers()
    }

    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(this) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        _viewModel.showToast.observe(this) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        _viewModel.showSnackBar.observe(this) {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        }
        _viewModel.showSnackBarInt.observe(this) {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        }

        _viewModel.navigationCommand.observe(this) { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        }
    }
}