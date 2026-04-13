package com.LambdaProject.MathArt.ViewModels

import androidx.lifecycle.ViewModel
import com.LambdaProject.MathArt.data.DataMaterialTopic
import com.LambdaProject.MathArt.data.model.MaterialItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MaterialViewModel @Inject constructor() : ViewModel() {

    private val _selectedMaterial = MutableStateFlow<MaterialItem?>(null)
    val selectedMaterial: StateFlow<MaterialItem?> = _selectedMaterial

    private val _tabs = MutableStateFlow<List<String>>(emptyList())
    val tabs: StateFlow<List<String>> = _tabs

    fun selectMaterial(material: MaterialItem) {
        _selectedMaterial.value = material
        _tabs.value = DataMaterialTopic[material.id] ?: listOf("Pengantar", "Kuis")
    }
}