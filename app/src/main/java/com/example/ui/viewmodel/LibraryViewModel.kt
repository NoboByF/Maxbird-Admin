package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.CloudflareR2Client
import com.example.data.model.LibraryChapter
import com.example.data.model.LibraryClass
import com.example.data.model.LibraryHtmlFile
import com.example.data.model.LibraryIndex
import com.example.data.model.LibrarySubject
import com.example.data.model.LibraryUtils
import com.example.data.repository.LibraryR2Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class LibraryLevel {
    CLASSES,
    SUBJECTS,
    CHAPTERS,
    FILES
}

data class LibraryUiState(
    val index: LibraryIndex = LibraryIndex(),
    val hasLoadedOnce: Boolean = false,
    val existedOnR2: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val uploadStatusText: String? = null,
    val uploadProgressPercent: Int = 0,
    val errorMessage: String? = null,
    val selectedClassId: String? = null,
    val selectedSubjectId: String? = null,
    val selectedChapterId: String? = null,
    val searchQuery: String = ""
) {
    val selectedClass: LibraryClass?
        get() = selectedClassId?.let { id -> index.classes.find { it.id == id } }

    val selectedSubject: LibrarySubject?
        get() = selectedSubjectId?.let { id -> selectedClass?.subjects?.find { it.id == id } }

    val selectedChapter: LibraryChapter?
        get() = selectedChapterId?.let { id -> selectedSubject?.chapters?.find { it.id == id } }

    val currentLevel: LibraryLevel
        get() = when {
            selectedClass == null -> LibraryLevel.CLASSES
            selectedSubject == null -> LibraryLevel.SUBJECTS
            selectedChapter == null -> LibraryLevel.CHAPTERS
            else -> LibraryLevel.FILES
        }
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val r2Client = CloudflareR2Client(application.applicationContext)
    private val repository = LibraryR2Repository(r2Client)
    private val mutationMutex = Mutex()

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableStateFlow<String?>(null)
    val toastEvent: StateFlow<String?> = _toastEvent.asStateFlow()

    fun consumeToast() {
        _toastEvent.value = null
    }

    private fun emitToast(message: String) {
        _toastEvent.value = message
    }

    fun inspectUri(uri: Uri): CloudflareR2Client.UriMetadata {
        return repository.inspectUri(uri)
    }

    fun loadLibraryIndex(forceRefresh: Boolean = false) {
        val current = _uiState.value
        if (current.isLoading) return
        if (current.hasLoadedOnce && !forceRefresh) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.fetchLibraryIndex()
            result.fold(
                onSuccess = { fetch ->
                    _uiState.update { state ->
                        val nextState = state.copy(
                            index = fetch.index,
                            hasLoadedOnce = true,
                            existedOnR2 = fetch.existedOnR2,
                            isLoading = false,
                            errorMessage = null
                        )
                        // Validate breadcrumb selections still exist after refresh
                        sanitizeSelections(nextState)
                    }
                    if (forceRefresh) {
                        if (fetch.existedOnR2) {
                            emitToast("✅ R2 থেকে লাইব্রেরি সূচিপত্র রিফ্রেশ হয়েছে।")
                        } else {
                            emitToast("ℹ️ নতুন লাইব্রেরি প্রস্তুত — প্রথম শ্রেণী যোগ করলেই library_index.json তৈরি হবে।")
                        }
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "লাইব্রেরি লোড করা যায়নি।"
                        )
                    }
                    emitToast("❌ ${err.message ?: "লাইব্রেরি লোড ব্যর্থ"}")
                }
            )
        }
    }

    private fun sanitizeSelections(state: LibraryUiState): LibraryUiState {
        val cls = state.selectedClassId?.let { id -> state.index.classes.find { it.id == id } }
        if (cls == null) {
            return state.copy(selectedClassId = null, selectedSubjectId = null, selectedChapterId = null)
        }
        val sub = state.selectedSubjectId?.let { id -> cls.subjects.find { it.id == id } }
        if (sub == null) {
            return state.copy(selectedSubjectId = null, selectedChapterId = null)
        }
        val ch = state.selectedChapterId?.let { id -> sub.chapters.find { it.id == id } }
        if (ch == null) {
            return state.copy(selectedChapterId = null)
        }
        return state
    }

    // =========================================================================
    // Breadcrumb & Folder Navigation
    // =========================================================================

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openClass(classId: String) {
        _uiState.update {
            it.copy(
                selectedClassId = classId,
                selectedSubjectId = null,
                selectedChapterId = null,
                searchQuery = ""
            )
        }
    }

    fun openSubject(subjectId: String) {
        _uiState.update {
            it.copy(
                selectedSubjectId = subjectId,
                selectedChapterId = null,
                searchQuery = ""
            )
        }
    }

    fun openChapter(chapterId: String) {
        _uiState.update {
            it.copy(
                selectedChapterId = chapterId,
                searchQuery = ""
            )
        }
    }

    fun navigateToRoot() {
        _uiState.update {
            it.copy(
                selectedClassId = null,
                selectedSubjectId = null,
                selectedChapterId = null,
                searchQuery = ""
            )
        }
    }

    fun navigateToClassLevel() {
        _uiState.update {
            it.copy(
                selectedSubjectId = null,
                selectedChapterId = null,
                searchQuery = ""
            )
        }
    }

    fun navigateToSubjectLevel() {
        _uiState.update {
            it.copy(
                selectedChapterId = null,
                searchQuery = ""
            )
        }
    }

    /**
     * Steps up 1 folder level. Returns `true` if handled internally, `false` if already at root.
     */
    fun navigateUp(): Boolean {
        val state = _uiState.value
        return when (state.currentLevel) {
            LibraryLevel.FILES -> {
                navigateToSubjectLevel()
                true
            }
            LibraryLevel.CHAPTERS -> {
                navigateToClassLevel()
                true
            }
            LibraryLevel.SUBJECTS -> {
                navigateToRoot()
                true
            }
            LibraryLevel.CLASSES -> false
        }
    }

    // =========================================================================
    // Level 1: Class (শ্রেণী) CRUD + Reorder
    // =========================================================================

    fun saveClass(
        editingClassId: String?,
        customIdInput: String,
        title: String,
        subtitle: String,
        badge: String,
        colorHex: String,
        isActive: Boolean
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            emitToast("⚠️ শ্রেণীর নাম লিখুন।")
            return
        }

        persistIndexMutation(
            statusMessage = "R2-তে শ্রেণী সেভ হচ্ছে...",
            successToast = if (editingClassId == null) "✅ নতুন শ্রেণী যুক্ত হয়েছে।" else "✅ শ্রেণী আপডেট হয়েছে।"
        ) { currentIndex ->
            val cleanColor = LibraryUtils.normalizeHexColor(colorHex, "#10B981")
            val updatedClasses = if (editingClassId == null) {
                val existingIds = currentIndex.classes.map { it.id }.toSet()
                val desiredId = customIdInput.trim().ifBlank { cleanTitle }
                val uniqueId = LibraryUtils.ensureUniqueId(desiredId, existingIds, "class")
                val nextOrder = (currentIndex.classes.maxOfOrNull { it.order } ?: 0) + 1
                currentIndex.classes + LibraryClass(
                    id = uniqueId,
                    title = cleanTitle,
                    subtitle = subtitle.trim(),
                    badge = badge.trim(),
                    colorHex = cleanColor,
                    order = nextOrder,
                    isActive = isActive,
                    subjects = emptyList()
                )
            } else {
                currentIndex.classes.map { cls ->
                    if (cls.id == editingClassId) {
                        cls.copy(
                            title = cleanTitle,
                            subtitle = subtitle.trim(),
                            badge = badge.trim(),
                            colorHex = cleanColor,
                            isActive = isActive
                        )
                    } else cls
                }
            }
            currentIndex.copy(classes = renumberClasses(updatedClasses))
        }
    }

    fun toggleClassActive(classId: String) {
        persistIndexMutation(
            statusMessage = "স্ট্যাটাস আপডেট হচ্ছে...",
            successToast = "✅ শ্রেণীর স্ট্যাটাস আপডেট হয়েছে।"
        ) { currentIndex ->
            currentIndex.copy(
                classes = currentIndex.classes.map { cls ->
                    if (cls.id == classId) cls.copy(isActive = !cls.isActive) else cls
                }
            )
        }
    }

    fun moveClass(classId: String, moveUp: Boolean) {
        val sorted = _uiState.value.index.classes.sortedBy { it.order }
        val idx = sorted.indexOfFirst { it.id == classId }
        val targetIdx = if (moveUp) idx - 1 else idx + 1
        if (idx < 0 || targetIdx !in sorted.indices) return

        persistIndexMutation(
            statusMessage = "ক্রমানুসারে সাজানো হচ্ছে...",
            successToast = "✅ শ্রেণীর ক্রম আপডেট হয়েছে।"
        ) { currentIndex ->
            val mutable = currentIndex.classes.sortedBy { it.order }.toMutableList()
            val cur = mutable.indexOfFirst { it.id == classId }
            val dst = if (moveUp) cur - 1 else cur + 1
            if (cur >= 0 && dst in mutable.indices) {
                val item = mutable.removeAt(cur)
                mutable.add(dst, item)
            }
            currentIndex.copy(classes = renumberClasses(mutable))
        }
    }

    fun deleteClass(classId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val currentIndex = _uiState.value.index
                val target = currentIndex.classes.find { it.id == classId } ?: return@withLock
                val keysToDelete = target.collectAllR2Keys()

                _uiState.update {
                    it.copy(
                        isSaving = true,
                        uploadStatusText = if (keysToDelete.isNotEmpty()) {
                            "R2 থেকে ${keysToDelete.size}টি HTML ফাইল ও শ্রেণী ডিলিট হচ্ছে..."
                        } else {
                            "শ্রেণী ডিলিট হচ্ছে..."
                        },
                        uploadProgressPercent = 0
                    )
                }

                val deletedFilesCount = repository.deleteR2Objects(keysToDelete)
                val updatedIndex = currentIndex.copy(
                    classes = renumberClasses(currentIndex.classes.filterNot { it.id == classId })
                )

                val saveResult = repository.saveLibraryIndex(updatedIndex)
                saveResult.fold(
                    onSuccess = { saved ->
                        _uiState.update { state ->
                            sanitizeSelections(
                                state.copy(
                                    index = saved,
                                    existedOnR2 = true,
                                    isSaving = false,
                                    uploadStatusText = null
                                )
                            )
                        }
                        emitToast(
                            if (deletedFilesCount > 0) {
                                "🗑️ শ্রেণী এবং R2 থেকে ${deletedFilesCount}টি ফাইল ডিলিট হয়েছে।"
                            } else {
                                "🗑️ শ্রেণী ডিলিট হয়েছে।"
                            }
                        )
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isSaving = false, uploadStatusText = null) }
                        emitToast("❌ ডিলিট ব্যর্থ: ${err.message}")
                    }
                )
            }
        }
    }

    // =========================================================================
    // Level 2: Subject (সাবজেক্ট) CRUD + Reorder
    // =========================================================================

    fun uploadSubjectCoverImage(uri: Uri, onCoverUrlUploaded: (String) -> Unit) {
        val classId = _uiState.value.selectedClassId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    uploadStatusText = "কভার ছবি R2-তে আপলোড হচ্ছে...",
                    uploadProgressPercent = 40
                )
            }
            val result = repository.uploadSubjectCover(
                uri = uri,
                classId = classId,
                subjectId = "sub-${System.currentTimeMillis() % 10000}"
            )
            result.fold(
                onSuccess = { uploaded ->
                    _uiState.update { it.copy(isSaving = false, uploadStatusText = null, uploadProgressPercent = 0) }
                    onCoverUrlUploaded(uploaded.publicUrl)
                    emitToast("✅ কভার ছবি R2-তে আপলোড হয়েছে।")
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isSaving = false, uploadStatusText = null, uploadProgressPercent = 0) }
                    emitToast("❌ কভার আপলোড ব্যর্থ: ${err.message}")
                }
            )
        }
    }

    fun saveSubject(
        editingSubjectId: String?,
        customIdInput: String,
        title: String,
        subtitle: String,
        coverUrl: String,
        colorHex: String,
        isActive: Boolean
    ) {
        val classId = _uiState.value.selectedClassId ?: return
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            emitToast("⚠️ সাবজেক্টের নাম লিখুন।")
            return
        }

        persistIndexMutation(
            statusMessage = "R2-তে সাবজেক্ট সেভ হচ্ছে...",
            successToast = if (editingSubjectId == null) "✅ নতুন সাবজেক্ট যুক্ত হয়েছে।" else "✅ সাবজেক্ট আপডেট হয়েছে।"
        ) { currentIndex ->
            val cleanColor = LibraryUtils.normalizeHexColor(colorHex, "#0284C7")
            currentIndex.copy(
                classes = currentIndex.classes.map { cls ->
                    if (cls.id != classId) return@map cls
                    val updatedSubjects = if (editingSubjectId == null) {
                        val existingIds = cls.subjects.map { it.id }.toSet()
                        val desiredId = customIdInput.trim().ifBlank { cleanTitle }
                        val uniqueId = LibraryUtils.ensureUniqueId(desiredId, existingIds, "subject")
                        val nextOrder = (cls.subjects.maxOfOrNull { it.order } ?: 0) + 1
                        cls.subjects + LibrarySubject(
                            id = uniqueId,
                            title = cleanTitle,
                            subtitle = subtitle.trim(),
                            coverUrl = coverUrl.trim(),
                            colorHex = cleanColor,
                            order = nextOrder,
                            isActive = isActive,
                            chapters = emptyList()
                        )
                    } else {
                        cls.subjects.map { sub ->
                            if (sub.id == editingSubjectId) {
                                sub.copy(
                                    title = cleanTitle,
                                    subtitle = subtitle.trim(),
                                    coverUrl = coverUrl.trim(),
                                    colorHex = cleanColor,
                                    isActive = isActive
                                )
                            } else sub
                        }
                    }
                    cls.copy(subjects = renumberSubjects(updatedSubjects))
                }
            )
        }
    }

    fun toggleSubjectActive(subjectId: String) {
        val classId = _uiState.value.selectedClassId ?: return
        persistIndexMutation(
            statusMessage = "স্ট্যাটাস আপডেট হচ্ছে...",
            successToast = "✅ সাবজেক্ট স্ট্যাটাস আপডেট হয়েছে।"
        ) { currentIndex ->
            currentIndex.copy(
                classes = currentIndex.classes.map { cls ->
                    if (cls.id != classId) return@map cls
                    cls.copy(
                        subjects = cls.subjects.map { sub ->
                            if (sub.id == subjectId) sub.copy(isActive = !sub.isActive) else sub
                        }
                    )
                }
            )
        }
    }

    fun moveSubject(subjectId: String, moveUp: Boolean) {
        val classId = _uiState.value.selectedClassId ?: return
        persistIndexMutation(
            statusMessage = "সাবজেক্ট রি-অর্ডার হচ্ছে...",
            successToast = "✅ সাবজেক্টের ক্রম আপডেট হয়েছে।"
        ) { currentIndex ->
            currentIndex.copy(
                classes = currentIndex.classes.map { cls ->
                    if (cls.id != classId) return@map cls
                    val mutable = cls.subjects.sortedBy { it.order }.toMutableList()
                    val cur = mutable.indexOfFirst { it.id == subjectId }
                    val dst = if (moveUp) cur - 1 else cur + 1
                    if (cur >= 0 && dst in mutable.indices) {
                        val item = mutable.removeAt(cur)
                        mutable.add(dst, item)
                    }
                    cls.copy(subjects = renumberSubjects(mutable))
                }
            )
        }
    }

    fun deleteSubject(subjectId: String) {
        val classId = _uiState.value.selectedClassId ?: return
        viewModelScope.launch {
            mutationMutex.withLock {
                val currentIndex = _uiState.value.index
                val targetSubject = currentIndex.classes
                    .find { it.id == classId }
                    ?.subjects?.find { it.id == subjectId } ?: return@withLock
                val keysToDelete = targetSubject.collectAllR2Keys()

                _uiState.update {
                    it.copy(
                        isSaving = true,
                        uploadStatusText = if (keysToDelete.isNotEmpty()) {
                            "R2 থেকে ${keysToDelete.size}টি ফাইল ও সাবজেক্ট ডিলিট হচ্ছে..."
                        } else {
                            "সাবজেক্ট ডিলিট হচ্ছে..."
                        }
                    )
                }

                val deletedFilesCount = repository.deleteR2Objects(keysToDelete)
                val updatedIndex = currentIndex.copy(
                    classes = currentIndex.classes.map { cls ->
                        if (cls.id != classId) return@map cls
                        cls.copy(subjects = renumberSubjects(cls.subjects.filterNot { it.id == subjectId }))
                    }
                )

                repository.saveLibraryIndex(updatedIndex).fold(
                    onSuccess = { saved ->
                        _uiState.update { state ->
                            sanitizeSelections(
                                state.copy(
                                    index = saved,
                                    existedOnR2 = true,
                                    isSaving = false,
                                    uploadStatusText = null
                                )
                            )
                        }
                        emitToast(
                            if (deletedFilesCount > 0) {
                                "🗑️ সাবজেক্ট এবং R2 থেকে ${deletedFilesCount}টি ফাইল ডিলিট হয়েছে।"
                            } else {
                                "🗑️ সাবজেক্ট ডিলিট হয়েছে।"
                            }
                        )
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isSaving = false, uploadStatusText = null) }
                        emitToast("❌ ডিলিট ব্যর্থ: ${err.message}")
                    }
                )
            }
        }
    }

    // =========================================================================
    // Level 3: Chapter (অধ্যায়) CRUD + Reorder
    // =========================================================================

    fun saveChapter(
        editingChapterId: String?,
        customIdInput: String,
        chapterNumber: Int,
        title: String,
        subtitle: String,
        isActive: Boolean
    ) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            emitToast("⚠️ অধ্যায়ের নাম লিখুন।")
            return
        }
        val safeChapterNum = chapterNumber.coerceAtLeast(1)

        persistIndexMutation(
            statusMessage = "R2-তে অধ্যায় সেভ হচ্ছে...",
            successToast = if (editingChapterId == null) "✅ নতুন অধ্যায় যুক্ত হয়েছে।" else "✅ অধ্যায় আপডেট হয়েছে।"
        ) { currentIndex ->
            currentIndex.copy(
                classes = currentIndex.classes.map { cls ->
                    if (cls.id != classId) return@map cls
                    cls.copy(
                        subjects = cls.subjects.map { sub ->
                            if (sub.id != subjectId) return@map sub
                            val updatedChapters = if (editingChapterId == null) {
                                val existingIds = sub.chapters.map { it.id }.toSet()
                                val desiredId = customIdInput.trim().ifBlank { "ch-$safeChapterNum" }
                                val uniqueId = LibraryUtils.ensureUniqueId(desiredId, existingIds, "ch")
                                val nextOrder = (sub.chapters.maxOfOrNull { it.order } ?: 0) + 1
                                sub.chapters + LibraryChapter(
                                    id = uniqueId,
                                    chapterNumber = safeChapterNum,
                                    title = cleanTitle,
                                    subtitle = subtitle.trim(),
                                    order = nextOrder,
                                    isActive = isActive,
                                    files = emptyList()
                                )
                            } else {
                                sub.chapters.map { ch ->
                                    if (ch.id == editingChapterId) {
                                        ch.copy(
                                            chapterNumber = safeChapterNum,
                                            title = cleanTitle,
                                            subtitle = subtitle.trim(),
                                            isActive = isActive
                                        )
                                    } else ch
                                }
                            }
                            sub.copy(chapters = renumberChapters(updatedChapters))
                        }
                    )
                }
            )
        }
    }

    fun toggleChapterActive(chapterId: String) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        persistIndexMutation(
            statusMessage = "স্ট্যাটাস আপডেট হচ্ছে...",
            successToast = "✅ অধ্যায়ের স্ট্যাটাস আপডেট হয়েছে।"
        ) { currentIndex ->
            currentIndex.copy(
                classes = currentIndex.classes.map { cls ->
                    if (cls.id != classId) return@map cls
                    cls.copy(
                        subjects = cls.subjects.map { sub ->
                            if (sub.id != subjectId) return@map sub
                            sub.copy(
                                chapters = sub.chapters.map { ch ->
                                    if (ch.id == chapterId) ch.copy(isActive = !ch.isActive) else ch
                                }
                            )
                        }
                    )
                }
            )
        }
    }

    fun moveChapter(chapterId: String, moveUp: Boolean) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        persistIndexMutation(
            statusMessage = "অধ্যায় রি-অর্ডার হচ্ছে...",
            successToast = "✅ অধ্যায়ের ক্রম আপডেট হয়েছে।"
        ) { currentIndex ->
            currentIndex.copy(
                classes = currentIndex.classes.map { cls ->
                    if (cls.id != classId) return@map cls
                    cls.copy(
                        subjects = cls.subjects.map { sub ->
                            if (sub.id != subjectId) return@map sub
                            val mutable = sub.chapters.sortedBy { it.order }.toMutableList()
                            val cur = mutable.indexOfFirst { it.id == chapterId }
                            val dst = if (moveUp) cur - 1 else cur + 1
                            if (cur >= 0 && dst in mutable.indices) {
                                val item = mutable.removeAt(cur)
                                mutable.add(dst, item)
                            }
                            sub.copy(chapters = renumberChapters(mutable))
                        }
                    )
                }
            )
        }
    }

    fun deleteChapter(chapterId: String) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        viewModelScope.launch {
            mutationMutex.withLock {
                val currentIndex = _uiState.value.index
                val targetChapter = currentIndex.classes
                    .find { it.id == classId }
                    ?.subjects?.find { it.id == subjectId }
                    ?.chapters?.find { it.id == chapterId } ?: return@withLock

                val keysToDelete = targetChapter.collectAllR2Keys()

                _uiState.update {
                    it.copy(
                        isSaving = true,
                        uploadStatusText = if (keysToDelete.isNotEmpty()) {
                            "R2 থেকে ${keysToDelete.size}টি HTML ফাইল ও অধ্যায় ডিলিট হচ্ছে..."
                        } else {
                            "অধ্যায় ডিলিট হচ্ছে..."
                        }
                    )
                }

                val deletedFilesCount = repository.deleteR2Objects(keysToDelete)
                val updatedIndex = currentIndex.copy(
                    classes = currentIndex.classes.map { cls ->
                        if (cls.id != classId) return@map cls
                        cls.copy(
                            subjects = cls.subjects.map { sub ->
                                if (sub.id != subjectId) return@map sub
                                sub.copy(chapters = renumberChapters(sub.chapters.filterNot { it.id == chapterId }))
                            }
                        )
                    }
                )

                repository.saveLibraryIndex(updatedIndex).fold(
                    onSuccess = { saved ->
                        _uiState.update { state ->
                            sanitizeSelections(
                                state.copy(
                                    index = saved,
                                    existedOnR2 = true,
                                    isSaving = false,
                                    uploadStatusText = null
                                )
                            )
                        }
                        emitToast(
                            if (deletedFilesCount > 0) {
                                "🗑️ অধ্যায় এবং R2 থেকে ${deletedFilesCount}টি HTML ফাইল ডিলিট হয়েছে।"
                            } else {
                                "🗑️ অধ্যায় ডিলিট হয়েছে।"
                            }
                        )
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isSaving = false, uploadStatusText = null) }
                        emitToast("❌ ডিলিট ব্যর্থ: ${err.message}")
                    }
                )
            }
        }
    }

    // =========================================================================
    // Level 4: HTML Files (Upload single/multiple, Edit, Replace +v1, Delete)
    // =========================================================================

    fun uploadSingleHtmlFile(
        uri: Uri,
        customIdInput: String,
        title: String,
        description: String,
        isActive: Boolean
    ) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        val chapterId = _uiState.value.selectedChapterId ?: return

        val meta = repository.inspectUri(uri)
        val cleanTitle = title.trim().ifBlank { LibraryUtils.cleanTitleFromFileName(meta.fileName) }

        viewModelScope.launch {
            mutationMutex.withLock {
                val currentIndex = _uiState.value.index
                val chapter = currentIndex.classes
                    .find { it.id == classId }
                    ?.subjects?.find { it.id == subjectId }
                    ?.chapters?.find { it.id == chapterId } ?: return@withLock

                val existingIds = chapter.files.map { it.id }.toSet()
                val desiredId = customIdInput.trim().ifBlank {
                    meta.fileName.substringBeforeLast('.')
                }
                val fileId = LibraryUtils.ensureUniqueId(desiredId, existingIds, "lesson")

                _uiState.update {
                    it.copy(
                        isSaving = true,
                        uploadStatusText = "আপলোড হচ্ছে: ${meta.fileName}",
                        uploadProgressPercent = 0
                    )
                }

                val uploadResult = repository.uploadHtmlFile(
                    uri = uri,
                    classId = classId,
                    subjectId = subjectId,
                    chapterId = chapterId,
                    fileId = fileId,
                    onProgress = { pct, _, _ ->
                        _uiState.update {
                            it.copy(
                                uploadProgressPercent = pct,
                                uploadStatusText = "আপলোড হচ্ছে: ${meta.fileName} ($pct%)"
                            )
                        }
                    }
                )

                val uploaded = uploadResult.getOrElse { err ->
                    _uiState.update { it.copy(isSaving = false, uploadStatusText = null, uploadProgressPercent = 0) }
                    emitToast("❌ HTML আপলোড ব্যর্থ: ${err.message}")
                    return@withLock
                }

                _uiState.update {
                    it.copy(uploadStatusText = "library_index.json আপডেট হচ্ছে...", uploadProgressPercent = 100)
                }

                val nextOrder = (chapter.files.maxOfOrNull { it.order } ?: 0) + 1
                val newFile = LibraryHtmlFile(
                    id = fileId,
                    title = cleanTitle,
                    description = description.trim(),
                    fileUrl = uploaded.fileKey,
                    fileSizeBytes = uploaded.sizeBytes,
                    version = 1,
                    order = nextOrder,
                    isActive = isActive
                )

                val updatedIndex = updateChapterFiles(currentIndex, classId, subjectId, chapterId) { files ->
                    renumberFiles(files + newFile)
                }

                repository.saveLibraryIndex(updatedIndex).fold(
                    onSuccess = { saved ->
                        _uiState.update {
                            it.copy(
                                index = saved,
                                existedOnR2 = true,
                                isSaving = false,
                                uploadStatusText = null,
                                uploadProgressPercent = 0
                            )
                        }
                        emitToast("✅ HTML ফাইল R2-তে আপলোড ও ইনডেক্স আপডেট হয়েছে!")
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isSaving = false, uploadStatusText = null, uploadProgressPercent = 0) }
                        emitToast("❌ ইনডেক্স সেভ ব্যর্থ: ${err.message}")
                    }
                )
            }
        }
    }

    fun uploadMultipleHtmlFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        val chapterId = _uiState.value.selectedChapterId ?: return

        viewModelScope.launch {
            mutationMutex.withLock {
                val currentIndex = _uiState.value.index
                val chapter = currentIndex.classes
                    .find { it.id == classId }
                    ?.subjects?.find { it.id == subjectId }
                    ?.chapters?.find { it.id == chapterId } ?: return@withLock

                val usedIds = chapter.files.map { it.id }.toMutableSet()
                val addedFiles = mutableListOf<LibraryHtmlFile>()
                var nextOrder = (chapter.files.maxOfOrNull { it.order } ?: 0) + 1
                var skippedNonHtml = 0
                var failedCount = 0

                _uiState.update {
                    it.copy(
                        isSaving = true,
                        uploadStatusText = "${uris.size}টি HTML ফাইল আপলোড শুরু হচ্ছে...",
                        uploadProgressPercent = 0
                    )
                }

                uris.forEachIndexed { idx, uri ->
                    val meta = repository.inspectUri(uri)
                    val lower = meta.fileName.lowercase()
                    val isHtml = lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".xhtml")
                    if (!isHtml) {
                        skippedNonHtml++
                        return@forEachIndexed
                    }

                    val fileId = LibraryUtils.ensureUniqueId(
                        desiredId = meta.fileName.substringBeforeLast('.'),
                        existingIds = usedIds,
                        fallbackPrefix = "lesson"
                    )
                    usedIds.add(fileId)

                    val stepLabel = "(${idx + 1}/${uris.size}) ${meta.fileName}"
                    _uiState.update {
                        it.copy(
                            uploadStatusText = "আপলোড হচ্ছে $stepLabel",
                            uploadProgressPercent = ((idx * 100) / uris.size).coerceIn(0, 99)
                        )
                    }

                    val uploadRes = repository.uploadHtmlFile(
                        uri = uri,
                        classId = classId,
                        subjectId = subjectId,
                        chapterId = chapterId,
                        fileId = fileId,
                        onProgress = { pct, _, _ ->
                            val overall = (((idx * 100) + pct) / uris.size).coerceIn(0, 99)
                            _uiState.update {
                                it.copy(
                                    uploadStatusText = "আপলোড হচ্ছে $stepLabel — $pct%",
                                    uploadProgressPercent = overall
                                )
                            }
                        }
                    )

                    uploadRes.fold(
                        onSuccess = { uploaded ->
                            addedFiles.add(
                                LibraryHtmlFile(
                                    id = fileId,
                                    title = LibraryUtils.cleanTitleFromFileName(meta.fileName),
                                    description = "",
                                    fileUrl = uploaded.fileKey,
                                    fileSizeBytes = uploaded.sizeBytes,
                                    version = 1,
                                    order = nextOrder++,
                                    isActive = true
                                )
                            )
                        },
                        onFailure = {
                            failedCount++
                        }
                    )
                }

                if (addedFiles.isEmpty()) {
                    _uiState.update { it.copy(isSaving = false, uploadStatusText = null, uploadProgressPercent = 0) }
                    val msg = when {
                        skippedNonHtml > 0 -> "⚠️ শুধুমাত্র .html বা .htm ফাইল সিলেক্ট করুন।"
                        else -> "❌ কোনো HTML ফাইল আপলোড করা যায়নি।"
                    }
                    emitToast(msg)
                    return@withLock
                }

                _uiState.update {
                    it.copy(
                        uploadStatusText = "library_index.json সেভ হচ্ছে...",
                        uploadProgressPercent = 100
                    )
                }

                val updatedIndex = updateChapterFiles(currentIndex, classId, subjectId, chapterId) { files ->
                    renumberFiles(files + addedFiles)
                }

                repository.saveLibraryIndex(updatedIndex).fold(
                    onSuccess = { saved ->
                        _uiState.update {
                            it.copy(
                                index = saved,
                                existedOnR2 = true,
                                isSaving = false,
                                uploadStatusText = null,
                                uploadProgressPercent = 0
                            )
                        }
                        val extra = buildString {
                            if (skippedNonHtml > 0) append(" ($skippedNonHtml টি নন-HTML বাদ)")
                            if (failedCount > 0) append(" ($failedCount টি ব্যর্থ)")
                        }
                        emitToast("✅ ${addedFiles.size}টি HTML ফাইল আপলোড হয়েছে!$extra")
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isSaving = false, uploadStatusText = null, uploadProgressPercent = 0) }
                        emitToast("❌ ইনডেক্স আপডেট ব্যর্থ: ${err.message}")
                    }
                )
            }
        }
    }

    fun updateHtmlFileMetadata(
        fileId: String,
        title: String,
        description: String,
        isActive: Boolean
    ) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        val chapterId = _uiState.value.selectedChapterId ?: return
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            emitToast("⚠️ ফাইলের নাম লিখুন।")
            return
        }

        persistIndexMutation(
            statusMessage = "ফাইলের তথ্য আপডেট হচ্ছে...",
            successToast = "✅ HTML ফাইলের তথ্য আপডেট হয়েছে।"
        ) { currentIndex ->
            updateChapterFiles(currentIndex, classId, subjectId, chapterId) { files ->
                files.map { f ->
                    if (f.id == fileId) {
                        f.copy(
                            title = cleanTitle,
                            description = description.trim(),
                            isActive = isActive
                        )
                    } else f
                }
            }
        }
    }

    /**
     * Replaces an existing `.html` file on R2 at its exact `file_url` key and increments `version` by +1
     * so the Student App immediately detects the update and shows an update badge.
     */
    fun replaceHtmlFile(fileId: String, newUri: Uri) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        val chapterId = _uiState.value.selectedChapterId ?: return

        viewModelScope.launch {
            mutationMutex.withLock {
                val currentIndex = _uiState.value.index
                val existingFile = currentIndex.classes
                    .find { it.id == classId }
                    ?.subjects?.find { it.id == subjectId }
                    ?.chapters?.find { it.id == chapterId }
                    ?.files?.find { it.id == fileId } ?: return@withLock

                val targetKey = existingFile.fileUrl.trim().ifBlank {
                    LibraryUtils.buildHtmlFileKey(classId, subjectId, chapterId, existingFile.id)
                }
                val meta = repository.inspectUri(newUri)

                _uiState.update {
                    it.copy(
                        isSaving = true,
                        uploadStatusText = "রিপ্লেস হচ্ছে: ${meta.fileName} (v${existingFile.version + 1})",
                        uploadProgressPercent = 0
                    )
                }

                val uploadResult = repository.uploadHtmlFileToKey(
                    uri = newUri,
                    fileKey = targetKey,
                    onProgress = { pct, _, _ ->
                        _uiState.update {
                            it.copy(
                                uploadProgressPercent = pct,
                                uploadStatusText = "রিপ্লেস হচ্ছে: ${meta.fileName} ($pct%)"
                            )
                        }
                    }
                )

                val uploaded = uploadResult.getOrElse { err ->
                    _uiState.update { it.copy(isSaving = false, uploadStatusText = null, uploadProgressPercent = 0) }
                    emitToast("❌ ফাইল রিপ্লেস ব্যর্থ: ${err.message}")
                    return@withLock
                }

                val newVersion = existingFile.version + 1
                _uiState.update {
                    it.copy(uploadStatusText = "ভার্শন v$newVersion ইনডেক্সে সেভ হচ্ছে...", uploadProgressPercent = 100)
                }

                val updatedIndex = updateChapterFiles(currentIndex, classId, subjectId, chapterId) { files ->
                    files.map { f ->
                        if (f.id == fileId) {
                            f.copy(
                                fileUrl = uploaded.fileKey,
                                fileSizeBytes = uploaded.sizeBytes,
                                version = newVersion
                            )
                        } else f
                    }
                }

                repository.saveLibraryIndex(updatedIndex).fold(
                    onSuccess = { saved ->
                        _uiState.update {
                            it.copy(
                                index = saved,
                                existedOnR2 = true,
                                isSaving = false,
                                uploadStatusText = null,
                                uploadProgressPercent = 0
                            )
                        }
                        emitToast("✅ HTML ফাইল রিপ্লেস হয়েছে! নতুন ভার্শন: v$newVersion")
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isSaving = false, uploadStatusText = null, uploadProgressPercent = 0) }
                        emitToast("❌ ইনডেক্স আপডেট ব্যর্থ: ${err.message}")
                    }
                )
            }
        }
    }

    fun toggleHtmlFileActive(fileId: String) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        val chapterId = _uiState.value.selectedChapterId ?: return
        persistIndexMutation(
            statusMessage = "স্ট্যাটাস আপডেট হচ্ছে...",
            successToast = "✅ ফাইলের স্ট্যাটাস আপডেট হয়েছে।"
        ) { currentIndex ->
            updateChapterFiles(currentIndex, classId, subjectId, chapterId) { files ->
                files.map { f ->
                    if (f.id == fileId) f.copy(isActive = !f.isActive) else f
                }
            }
        }
    }

    fun moveHtmlFile(fileId: String, moveUp: Boolean) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        val chapterId = _uiState.value.selectedChapterId ?: return
        persistIndexMutation(
            statusMessage = "ফাইল রি-অর্ডার হচ্ছে...",
            successToast = "✅ ফাইলের ক্রম আপডেট হয়েছে।"
        ) { currentIndex ->
            updateChapterFiles(currentIndex, classId, subjectId, chapterId) { files ->
                val mutable = files.sortedBy { it.order }.toMutableList()
                val cur = mutable.indexOfFirst { it.id == fileId }
                val dst = if (moveUp) cur - 1 else cur + 1
                if (cur >= 0 && dst in mutable.indices) {
                    val item = mutable.removeAt(cur)
                    mutable.add(dst, item)
                }
                renumberFiles(mutable)
            }
        }
    }

    fun deleteHtmlFile(fileId: String) {
        val classId = _uiState.value.selectedClassId ?: return
        val subjectId = _uiState.value.selectedSubjectId ?: return
        val chapterId = _uiState.value.selectedChapterId ?: return

        viewModelScope.launch {
            mutationMutex.withLock {
                val currentIndex = _uiState.value.index
                val targetFile = currentIndex.classes
                    .find { it.id == classId }
                    ?.subjects?.find { it.id == subjectId }
                    ?.chapters?.find { it.id == chapterId }
                    ?.files?.find { it.id == fileId } ?: return@withLock

                _uiState.update {
                    it.copy(
                        isSaving = true,
                        uploadStatusText = "R2 থেকে HTML ফাইল ডিলিট হচ্ছে..."
                    )
                }

                repository.deleteR2Objects(listOf(targetFile.fileUrl))

                val updatedIndex = updateChapterFiles(currentIndex, classId, subjectId, chapterId) { files ->
                    renumberFiles(files.filterNot { it.id == fileId })
                }

                repository.saveLibraryIndex(updatedIndex).fold(
                    onSuccess = { saved ->
                        _uiState.update {
                            it.copy(
                                index = saved,
                                existedOnR2 = true,
                                isSaving = false,
                                uploadStatusText = null
                            )
                        }
                        emitToast("🗑️ HTML ফাইল R2 ও সূচিপত্র থেকে ডিলিট হয়েছে।")
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isSaving = false, uploadStatusText = null) }
                        emitToast("❌ ডিলিট ব্যর্থ: ${err.message}")
                    }
                )
            }
        }
    }

    // =========================================================================
    // Internal Mutation & Order Helpers
    // =========================================================================

    private fun persistIndexMutation(
        statusMessage: String,
        successToast: String,
        transform: (LibraryIndex) -> LibraryIndex
    ) {
        viewModelScope.launch {
            mutationMutex.withLock {
                _uiState.update {
                    it.copy(
                        isSaving = true,
                        uploadStatusText = statusMessage,
                        uploadProgressPercent = 50
                    )
                }
                val updated = transform(_uiState.value.index)
                repository.saveLibraryIndex(updated).fold(
                    onSuccess = { saved ->
                        _uiState.update { state ->
                            sanitizeSelections(
                                state.copy(
                                    index = saved,
                                    existedOnR2 = true,
                                    isSaving = false,
                                    uploadStatusText = null,
                                    uploadProgressPercent = 0,
                                    errorMessage = null
                                )
                            )
                        }
                        emitToast(successToast)
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                uploadStatusText = null,
                                uploadProgressPercent = 0
                            )
                        }
                        emitToast("❌ R2 সেভ ব্যর্থ: ${err.message}")
                    }
                )
            }
        }
    }

    private fun updateChapterFiles(
        index: LibraryIndex,
        classId: String,
        subjectId: String,
        chapterId: String,
        transform: (List<LibraryHtmlFile>) -> List<LibraryHtmlFile>
    ): LibraryIndex {
        return index.copy(
            classes = index.classes.map { cls ->
                if (cls.id != classId) return@map cls
                cls.copy(
                    subjects = cls.subjects.map { sub ->
                        if (sub.id != subjectId) return@map sub
                        sub.copy(
                            chapters = sub.chapters.map { ch ->
                                if (ch.id != chapterId) return@map ch
                                ch.copy(files = transform(ch.files))
                            }
                        )
                    }
                )
            }
        )
    }

    private fun renumberClasses(list: List<LibraryClass>): List<LibraryClass> {
        return list.mapIndexed { idx, item -> item.copy(order = idx + 1) }
    }

    private fun renumberSubjects(list: List<LibrarySubject>): List<LibrarySubject> {
        return list.mapIndexed { idx, item -> item.copy(order = idx + 1) }
    }

    private fun renumberChapters(list: List<LibraryChapter>): List<LibraryChapter> {
        return list.mapIndexed { idx, item -> item.copy(order = idx + 1) }
    }

    private fun renumberFiles(list: List<LibraryHtmlFile>): List<LibraryHtmlFile> {
        return list.mapIndexed { idx, item -> item.copy(order = idx + 1) }
    }
}
