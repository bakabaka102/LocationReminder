package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.ToastUtils
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    // Use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    private val mMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.main_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.logout -> {
                    ToastUtils.showToast(requireActivity(), "Logout")
                    signOut()
                }
            }
            return true
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(requireActivity())
            .addOnCompleteListener { // user is now signed out
                startActivity(Intent(activity, AuthenticationActivity::class.java))
                activity?.finish()
            }

        /*AuthUI.getInstance().signOut(requireContext())
            .addOnSuccessListener {
                val intent = Intent(activity, AuthenticationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }*/

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reminders, container, false
        )
        binding.viewModel = _viewModel
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))
        requireActivity().addMenuProvider(
            mMenuProvider,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        // Load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        // Use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(ReminderListFragmentDirections.toSaveReminder())
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {}
        // Setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ToastUtils.cancelToast()
    }
}