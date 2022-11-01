package com.sivakasi.papco.jobflow.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PreviewManagementFragment : Fragment() {

    companion object{

        private const val KEY_PREVIEW_ID="job:flow:key:preview:id"
        private const val KEY_FRAGMENT_TITLE="job:flow:key:fragment:title"

        fun arguments(previewId:String,fragmentTitle:String=""):Bundle{
            return Bundle().apply {
                putString(KEY_PREVIEW_ID,previewId)
                putString(KEY_FRAGMENT_TITLE,fragmentTitle)
            }
        }
    }

    private val viewModel : PreviewManagementVM by lazy{
        ViewModelProvider(this)[PreviewManagementVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.observePreviews(getPreviewId())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply{
            setContent {
                JobFlowTheme {
                    PreviewManagementScreen(
                        viewModel,
                        findNavController(),
                        getFragmentTitle().ifBlank { getString(R.string.job_previews) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }

    private fun getPreviewId():String=
        arguments?.getString(KEY_PREVIEW_ID) ?: error("Preview ID argument not found")

    private fun getFragmentTitle():String=
        arguments?.getString(KEY_FRAGMENT_TITLE) ?: ""
}