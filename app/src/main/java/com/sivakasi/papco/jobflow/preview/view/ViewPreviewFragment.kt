package com.sivakasi.papco.jobflow.preview.view

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
import com.sivakasi.papco.jobflow.extensions.sharePreview
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.extensions.toast
import com.sivakasi.papco.jobflow.preview.JobPreview
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ViewPreviewFragment:Fragment() {

    companion object{
        const val KEY_IMAGE_URL="jobFlow:view:preview:image:url:key"
        const val KEY_PREVIEW_ID="jobFlow:view:preview:image:preview:id:key"
        const val KEY_FILE_NAME="jobFlow:view:preview:key:fileName"

        fun getArgument(preview: JobPreview):Bundle{
            return Bundle().apply {
                putString(KEY_IMAGE_URL,preview.displayUrl)
                putString(KEY_PREVIEW_ID,preview.previewId)
                putString(KEY_FILE_NAME,preview.fileName)
            }
        }
    }

    private val viewModel : ViewPreviewVM by lazy{
        ViewModelProvider(this)[ViewPreviewVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadPreview(getJobPreview())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply{
            setContent {
                JobFlowTheme {
                    ViewPreviewScreen(viewModel, findNavController())
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel(){
        viewModel.sharePreview.observe(viewLifecycleOwner,EventObserver{
            when(it){
                is File ->{
                    requireContext().sharePreview(it)
                }

                is Exception ->{
                    toast(it.message ?: getString(R.string.error_unknown_error))
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }

    private fun getImageUrl():String=
        arguments?.getString(KEY_IMAGE_URL) ?: error("Image Url argument not found")

    private fun getPreviewId():String=
        arguments?.getString(KEY_PREVIEW_ID) ?: error("Preview Id not found")

    private fun getFileName():String=
        arguments?.getString(KEY_FILE_NAME) ?: error("File name argument not found")

    private fun getJobPreview():JobPreview{
        return JobPreview(
            requireContext(),
            getPreviewId(),
            getFileName(),
            getImageUrl()
        )
    }
}