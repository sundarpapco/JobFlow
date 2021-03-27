package com.sivakasi.papco.jobflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.sivakasi.papco.jobflow.data.Family
import com.sivakasi.papco.jobflow.data.Person
import com.sivakasi.papco.jobflow.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {


    private var _viewBinding: FragmentFirstBinding? = null
    private val viewBinding
        get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _viewBinding = FragmentFirstBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.btnSave.setOnClickListener {
            saveSomethingToFireStore()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }


    private fun saveSomethingToFireStore() {

        val nateshFamily = Family()
        nateshFamily.father = Person("Natesh", 42)
        nateshFamily.mother = Person("Madhana", 35)
        nateshFamily.children = listOf(
            Person("Dharshana", 14),
            Person("Gayathri", 8)
        )
        nateshFamily.familyName = "Mariappan"

        val surajFamily = Family()
        surajFamily.father = Person("Suraj Anand", 29)
        surajFamily.mother = Person("Samyuktha", 26)

        FirebaseFirestore.getInstance()
            .collection("test")
            .document("natesh")
            .set(nateshFamily)
            .addOnSuccessListener {
                viewBinding.lblStatus.text="Written successfully"
            }

    }

}