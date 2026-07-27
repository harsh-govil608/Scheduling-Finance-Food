package com.lifeos.expensecapture.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.NotificationDao
import com.lifeos.expensecapture.data.db.entity.NotificationEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationCenterViewModel(private val notificationDao: NotificationDao) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = notificationDao.observeVisible()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markRead(notification: NotificationEntity) {
        viewModelScope.launch { notificationDao.markRead(notification.id) }
    }

    fun markAllRead() {
        viewModelScope.launch { notificationDao.markAllRead() }
    }

    fun dismiss(notification: NotificationEntity) {
        viewModelScope.launch { notificationDao.dismiss(notification.id) }
    }

    fun clearAll() {
        viewModelScope.launch { notificationDao.dismissAll() }
    }
}
