package com.absinthe.anywhere_.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.absinthe.anywhere_.R
import com.absinthe.anywhere_.constants.GlobalValues
import com.absinthe.anywhere_.databinding.DialogFabMenuBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/** M3 replacement for the SpeedDial expandable menu. Reports R.id.fab_* ids. */
class FabMenuBottomSheetDialogFragment : BottomSheetDialogFragment() {

  fun interface OnFabActionSelectedListener {
    fun onFabActionSelected(id: Int)
  }

  private var _binding: DialogFabMenuBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DialogFabMenuBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.tvCollectorLabel.text = GlobalValues.collectorMode
    val host = activity as? OnFabActionSelectedListener
    binding.rowAdvanced.setOnClickListener { host?.onFabActionSelected(R.id.fab_advanced); dismiss() }
    binding.rowCollector.setOnClickListener { host?.onFabActionSelected(R.id.fab_collector); dismiss() }
    binding.rowActivityList.setOnClickListener { host?.onFabActionSelected(R.id.fab_activity_list); dismiss() }
    binding.rowQrCode.setOnClickListener { host?.onFabActionSelected(R.id.fab_qr_code_collection); dismiss() }
    binding.rowCloudRules.setOnClickListener { host?.onFabActionSelected(R.id.fab_cloud_rules); dismiss() }
    binding.rowShortcut.setOnClickListener { host?.onFabActionSelected(R.id.fab_third_apps_shortcut); dismiss() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
