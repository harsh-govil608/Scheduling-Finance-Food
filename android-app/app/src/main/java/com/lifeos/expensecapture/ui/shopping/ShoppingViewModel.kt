package com.lifeos.expensecapture.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.ShoppingItemDao
import com.lifeos.expensecapture.data.db.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(private val dao: ShoppingItemDao) : ViewModel() {

    val items: StateFlow<List<ShoppingItemEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(name: String, quantity: String) {
        if (name.isBlank()) return
        viewModelScope.launch { dao.insert(ShoppingItemEntity(name = name.trim(), quantity = quantity.trim())) }
    }

    fun toggleChecked(item: ShoppingItemEntity) {
        viewModelScope.launch { dao.update(item.copy(checked = !item.checked)) }
    }

    fun delete(item: ShoppingItemEntity) {
        viewModelScope.launch { dao.delete(item) }
    }
}
