package com.lifeos.expensecapture.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.NoteDao
import com.lifeos.expensecapture.data.db.entity.NoteEntity
import com.lifeos.expensecapture.data.db.entity.NoteType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Backs both Notes (Doc 37) and Journal (Doc 38) screens - see NoteEntity.kt for why they
 * share one ViewModel/entity, distinguished only by `type`. */
class NotesViewModel(private val noteDao: NoteDao, private val type: NoteType) : ViewModel() {

    val notes: StateFlow<List<NoteEntity>> = noteDao.observeByType(type)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(title: String, body: String) {
        if (body.isBlank()) return
        viewModelScope.launch {
            noteDao.insert(NoteEntity(type = type, title = title.trim(), body = body.trim()))
        }
    }

    fun updateNote(note: NoteEntity, title: String, body: String) {
        if (body.isBlank()) return
        viewModelScope.launch {
            noteDao.update(note.copy(title = title.trim(), body = body.trim(), updatedAt = System.currentTimeMillis()))
        }
    }

    fun delete(note: NoteEntity) {
        viewModelScope.launch { noteDao.delete(note) }
    }
}
