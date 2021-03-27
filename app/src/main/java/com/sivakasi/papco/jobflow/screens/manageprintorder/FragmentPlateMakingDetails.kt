package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sivakasi.papco.jobflow.*
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentPlateMakingDetailsBinding
import com.sivakasi.papco.jobflow.util.FormValidator
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.NoFilterArrayAdapter
import com.wajahatkarim3.easyvalidation.core.rules.GreaterThanOrEqualRule
import com.wajahatkarim3.easyvalidation.core.rules.GreaterThanRule
import com.wajahatkarim3.easyvalidation.core.rules.LessThanOrEqualRule
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class FragmentPlateMakingDetails : Fragment() {

    companion object {
        private const val KEY_SELECTED_BACKSIDE_OPTION = "selected:backside:index"
        private const val NUMBER_BLANK_STRING = -1
    }

    private var _viewBinding: FragmentPlateMakingDetailsBinding? = null
    private val viewBinding get() = _viewBinding!!
    private var jobType: Int = PrintOrder.TYPE_NEW_JOB
    private var plateMakingDetail = PlateMakingDetail()

    private val backsideAdapter: NoFilterArrayAdapter<String> by lazy {
        val backsideOptions = resources.getStringArray(R.array.backsideOptions)
        NoFilterArrayAdapter(
            requireContext(),
            R.layout.spinner_drop_down,
            R.id.text1, backsideOptions.toList()
        )
    }

    private val viewModel: ManagePrintOrderVM by navGraphViewModels(R.id.print_order_flow)

    private val autoGripperTailUpdater = object : AbstractTextWatcher() {
        override fun afterTextChanged(p0: Editable?) {
            assignAutoGripperAndTail()
        }
    }
    private var backsidePrintingOptionIndex = 0
    private var plateNumber = PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backsidePrintingOptionIndex = savedInstanceState?.getInt(KEY_SELECTED_BACKSIDE_OPTION) ?: 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_BACKSIDE_OPTION, backsidePrintingOptionIndex)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentPlateMakingDetailsBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        attachEditTextListeners()
        observeViewModel()
    }

    override fun onStop() {
        super.onStop()
        saveStateToViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {

        initBacksideOptions()
        viewBinding.btnNext.setOnClickListener {
            if (validateForm()) {
                saveStateToViewModel()
                findNavController().navigate(R.id.action_fragmentPlateMakingDetails_to_fragmentPrintingDetail)
            }
        }

        viewBinding.checkboxPartyPlate.setOnClickListener {
            if (viewBinding.checkboxPartyPlate.isChecked) {
                plateMakingDetail.plateNumber = PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE
                enableNewPlateOnlyFields(false)
                clearNewPlateOnlyFields()
            } else {
                plateMakingDetail.plateNumber = PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED
                enableNewPlateOnlyFields(true)
            }
        }

    }

    private fun observeViewModel() {

        viewModel.loadedJob.observe(viewLifecycleOwner) {
            if (it is LoadingStatus.Success<*>) {
                val printOrder = it.data as PrintOrder
                jobType = printOrder.jobType
                plateMakingDetail = printOrder.plateMakingDetail
                renderPlateMakingDetail()
            }
        }
    }

    private fun initBacksideOptions() {
        with(viewBinding.txtBackside) {
            setAdapter(backsideAdapter)
            listSelection = backsidePrintingOptionIndex
            setText(adapter.getItem(backsidePrintingOptionIndex) as String)

        }
    }

    private fun attachEditTextListeners() {


        viewBinding.txtTrimHeight.addTextChangedListener(autoGripperTailUpdater)
        viewBinding.txtJobHeight.addTextChangedListener(autoGripperTailUpdater)
        viewBinding.txtTrimHeight.clearErrorOnTextChange()
        viewBinding.txtTrimWidth.clearErrorOnTextChange()
        viewBinding.txtJobHeight.clearErrorOnTextChange()
        viewBinding.txtJobWidth.clearErrorOnTextChange()
        viewBinding.txtScreen.clearErrorOnTextChange()
        viewBinding.txtMachine.clearErrorOnTextChange()


        viewBinding.txtJobHeight.setOnFocusChangeListener { _, isFocused ->
            if (isFocused &&
                viewBinding.txtJobHeight.text.toString().isBlank() &&
                viewBinding.txtTrimHeight.error == null
            ) {
                viewBinding.txtJobHeight.setText(viewBinding.txtTrimHeight.text.toString())
                viewBinding.txtJobHeight.selectAll()
            }
        }

        viewBinding.txtJobWidth.setOnFocusChangeListener { _, isFocused ->
            if (isFocused &&
                viewBinding.txtJobWidth.text.toString().isBlank() &&
                viewBinding.txtTrimWidth.error == null
            ) {
                viewBinding.txtJobWidth.setText(viewBinding.txtTrimWidth.text.toString())
                viewBinding.txtJobWidth.selectAll()
            }
        }

    }

    private fun assignAutoGripperAndTail() {

        if (viewBinding.txtTrimHeight.text.toString()
                .isBlank() || viewBinding.txtJobHeight.text.toString().isBlank()
        )
            return

        val trimHeight = viewBinding.txtTrimHeight.text.toString().toInt()
        val jobHeight = viewBinding.txtJobHeight.text.toString().toInt()

        if (trimHeight < jobHeight){

            viewBinding.txtGripper.setText("")
            viewBinding.txtTail.setText("")

        }else{

            val gripper = if (trimHeight - jobHeight > 10) 10 else trimHeight - jobHeight
            val tail = trimHeight - jobHeight - gripper

            viewBinding.txtGripper.setText(gripper.toString())
            viewBinding.txtTail.setText(tail.toString())

        }

    }

    private fun saveStateToViewModel() {
        with(plateMakingDetail) {
            plateNumber = createPlateNumber() ?: this@FragmentPlateMakingDetails.plateNumber
            trimmingHeight = viewBinding.txtTrimHeight.number(NUMBER_BLANK_STRING)
            trimmingWidth = viewBinding.txtTrimWidth.number(NUMBER_BLANK_STRING)
            jobHeight = viewBinding.txtJobHeight.number(NUMBER_BLANK_STRING)
            jobWidth = viewBinding.txtJobWidth.number(NUMBER_BLANK_STRING)
            gripper = viewBinding.txtGripper.number(NUMBER_BLANK_STRING)
            tail = viewBinding.txtTail.number(NUMBER_BLANK_STRING)
            machine = viewBinding.txtMachine.text.toString()
            screen = viewBinding.txtScreen.text.toString()
            backsideMachine = viewBinding.txtBackMachine.text.toString()
            backsidePrinting = viewBinding.txtBackside.text.toString()
        }
    }


    private fun renderPlateMakingDetail() {

        plateNumber = plateMakingDetail.plateNumber

        //renderJobHeight()
        with(viewBinding) {

            loadPlateNumber()
            txtTrimHeight.setText(plateMakingDetail.trimmingHeight.asString())
            txtTrimWidth.setText(plateMakingDetail.trimmingWidth.asString())
            txtJobHeight.setText(plateMakingDetail.jobHeight.asString())
            txtJobWidth.setText(plateMakingDetail.jobWidth.asString())
            txtGripper.setText(plateMakingDetail.gripper.asString())
            txtTail.setText(plateMakingDetail.tail.asString())
            txtMachine.setText(plateMakingDetail.machine)
            txtScreen.setText(plateMakingDetail.screen)
            txtBackMachine.setText(plateMakingDetail.backsideMachine)
            txtBackside.setText(plateMakingDetail.backsidePrinting)
        }

        if (plateNumber == PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED)
            enableNewPlateOnlyFields(true)
        else
            enableNewPlateOnlyFields(false)

    }

    private fun Int.asString(): String {
        return if (this == NUMBER_BLANK_STRING)
            ""
        else
            this.toString()
    }

    private fun loadPlateNumber() {

        if (jobType == PrintOrder.TYPE_NEW_JOB) {
            viewBinding.lblOldPlateNumber.visibility = View.GONE
            viewBinding.checkboxPartyPlate.visibility = View.VISIBLE
            viewBinding.checkboxPartyPlate.isChecked =
                plateMakingDetail.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE
        } else {
            viewBinding.lblOldPlateNumber.visibility = View.VISIBLE
            viewBinding.checkboxPartyPlate.visibility = View.GONE

            if (plateMakingDetail.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
                viewBinding.lblOldPlateNumber.text = getString(R.string.party_plate)
            else
                viewBinding.lblOldPlateNumber.text =
                    getString(R.string.old_plate_number, plateMakingDetail.plateNumber)
        }
    }

    private fun createPlateNumber(): Int? {

        return if (viewBinding.checkboxPartyPlate.visibility == View.VISIBLE) {
            //Its new Job
            if (viewBinding.checkboxPartyPlate.isChecked)
                PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE
            else
                PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED
        } else {
            //Reprint. So, don't do anything. The plate number is already in the saved state in VM
            null
        }

    }


    //Validation methods

    private fun validateForm(): Boolean {

        val validator = FormValidator()

        validator
            .validate(checkTrimHeight())
            .validate(checkTrimWidth())
            .validate(checkJobHeight())
            .validate(checkJobWidth())
            .validate(viewBinding.txtMachine.validateForNonBlank(getString(R.string.required_field)))
            .validate(checkScreenField())

        return validator.isValid()

    }


    private fun checkTrimHeight(): Boolean {

        val minValue = 360
        val maxValue = 720

        return viewBinding.txtTrimHeight.validator()
            .addRule(GreaterThanOrEqualRule(minValue))
            .addRule(LessThanOrEqualRule(maxValue))
            .addErrorCallback {
                viewBinding.txtLayoutTrimHeight.error =
                    getString(R.string.error_invalid_trim_size, minValue, maxValue)
            }
            .check()

    }


    private fun checkTrimWidth(): Boolean {

        val minValue = 560
        val maxValue = 1020

        return viewBinding.txtTrimWidth.validator()
            .addRule(GreaterThanOrEqualRule(minValue))
            .addRule(LessThanOrEqualRule(maxValue))
            .addErrorCallback {
                viewBinding.txtLayoutTrimWidth.error =
                    getString(R.string.error_invalid_trim_size, minValue, maxValue)
            }
            .check()

    }

    private fun checkJobHeight(): Boolean {

        //If the field is disabled, then skip validation and pass it
        if (!viewBinding.txtLayoutJobHeight.isEnabled)
            return true

        val trimmedHeight = viewBinding.txtTrimHeight.number()

        return viewBinding.txtJobHeight.validator()
            .addRule(GreaterThanRule(0))
            .addRule(LessThanOrEqualRule(trimmedHeight))
            .addErrorCallback {
                viewBinding.txtLayoutJobHeight.error = getString(R.string.error_invalid_job_height)
            }
            .check()
    }

    private fun checkJobWidth(): Boolean {

        //If the field is disabled, then skip validation and pass it
        if (!viewBinding.txtLayoutJobWidth.isEnabled)
            return true

        val trimmedWidth = viewBinding.txtTrimWidth.number()

        val validator = viewBinding.txtJobWidth.validator()
            .addRule(GreaterThanRule(0))
            .addRule(LessThanOrEqualRule(trimmedWidth))
            .addErrorCallback {
                viewBinding.txtLayoutJobWidth.error = getString(R.string.error_invalid_job_width)
            }

        return validator.check()

    }

    private fun checkScreenField(): Boolean {

        //If the field is disabled, then skip validation and pass it
        return if (!viewBinding.txtLayoutScreen.isEnabled)
            true
        else
            viewBinding.txtScreen.validateForNonBlank(getString(R.string.required_field))
    }


    private fun enableNewPlateOnlyFields(editable: Boolean) {

        with(viewBinding) {
            txtLayoutJobHeight.isEnabled = editable
            txtJobHeight.error=null
            txtLayoutJobHeight.isFocusable=editable
            txtJobHeight.isFocusable=editable

            txtLayoutJobWidth.isEnabled = editable
            txtJobWidth.error=null
            txtLayoutJobWidth.isFocusable = editable
            txtJobWidth.isFocusable=editable

            txtLayoutGripper.isEnabled = editable
            txtLayoutGripper.isFocusable = editable
            txtGripper.isFocusable=editable

            txtLayoutTail.isEnabled = editable
            txtLayoutTail.isFocusable = editable
            txtTail.isFocusable=editable

            txtLayoutScreen.isEnabled = editable
            txtScreen.error=null
            txtLayoutScreen.isFocusable = editable
            txtScreen.isFocusable=editable
        }

    }

    private fun clearNewPlateOnlyFields(){

        with(viewBinding) {
            plateMakingDetail.jobHeight= NUMBER_BLANK_STRING
            plateMakingDetail.jobWidth= NUMBER_BLANK_STRING
            plateMakingDetail.gripper= NUMBER_BLANK_STRING
            plateMakingDetail.tail= NUMBER_BLANK_STRING
            plateMakingDetail.screen=""

            txtJobHeight.setText("")
            txtJobWidth.setText("")
            txtGripper.setText("")
            txtTail.setText("")
            txtScreen.setText("")
        }

    }


}