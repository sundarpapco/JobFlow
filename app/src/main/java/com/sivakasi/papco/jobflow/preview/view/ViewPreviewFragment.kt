package com.sivakasi.papco.jobflow.preview.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

class ViewPreviewFragment:Fragment() {

    companion object{
        const val KEY_IMAGE_URL="jobFlow:view:preview:image:url:key"

        fun getArgument(imageUrl:String):Bundle{
            return Bundle().apply {
                putString(KEY_IMAGE_URL,imageUrl)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.d("SUNDAR",getImageUrl())
        return ComposeView(requireContext()).apply{
            setContent {
                JobFlowTheme {
                    ViewPreviewScreen(imageUrl = getImageUrl(),this@ViewPreviewFragment::onBack)
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

    private fun onBack(){
        findNavController().popBackStack()
    }

    private fun getImageUrl():String=
        arguments?.getString(KEY_IMAGE_URL) ?: error("Image Url argument not found")
}