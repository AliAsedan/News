package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Article
import com.example.data.GeminiService
import com.example.data.NewsDatabase
import com.example.data.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NewsDatabase.getDatabase(application, viewModelScope)
    private val repository = NewsRepository(database.articleDao())

    // Selected Category ("الكل", "عاجل", "سياسة", "اقتصاد", "اجتماع", "المحفوظات")
    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // AI Loading State
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Error message if AI generation fails or key is missing
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Combined UI Articles State Flow
    val articlesState: StateFlow<List<Article>> = combine(
        repository.allArticles,
        _selectedCategory,
        _searchQuery
    ) { all, category, query ->
        var filtered = all

        // Apply Category Filter
        if (category == "المحفوظات") {
            filtered = filtered.filter { it.isBookmarked }
        } else if (category != "الكل") {
            filtered = filtered.filter { it.category == category }
        }

        // Apply Search Query Filter
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }

        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Breaking news feed for the running ticker
    val breakingNews: StateFlow<List<Article>> = repository.allArticles
        .combine(_searchQuery) { all, query ->
            val breaking = all.filter { it.category == "عاجل" }
            if (query.isNotBlank()) {
                breaking.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                }
            } else {
                breaking
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(articleId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateBookmarkStatus(articleId, !currentStatus)
        }
    }

    fun incrementViews(articleId: Int) {
        viewModelScope.launch {
            repository.incrementViews(articleId)
        }
    }

    fun deleteArticle(articleId: Int) {
        viewModelScope.launch {
            repository.deleteArticleById(articleId)
        }
    }

    fun createCustomArticle(title: String, content: String, category: String, source: String) {
        viewModelScope.launch {
            val article = Article(
                title = title,
                content = content,
                category = category,
                source = source.ifBlank { "مساهمة مجتمعية" },
                timestamp = System.currentTimeMillis(),
                viewsCount = (10..50).random()
            )
            repository.insertArticle(article)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Generates fresh news using the Gemini AI API and saves them directly to the database.
     */
    fun generateAiNews(theme: String? = null, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _errorMessage.value = null
            try {
                val news = GeminiService.generateArabicNews(theme)
                if (news.isNotEmpty()) {
                    for (article in news) {
                        repository.insertArticle(article)
                    }
                    onComplete(true)
                } else {
                    // Fallback / Key check warning
                    val key = com.example.BuildConfig.GEMINI_API_KEY
                    if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
                        _errorMessage.value = "مفتاح Gemini API غير مكوّن. الرجاء إدخال المفتاح في لوحة الأسرار (Secrets) لتفعيل التوليد بالذكاء الاصطناعي."
                    } else {
                        _errorMessage.value = "فشل في توليد الأخبار بالذكاء الاصطناعي. يرجى التحقق من اتصال الإنترنت أو المحاولة لاحقاً."
                    }
                    onComplete(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "حدث خطأ غير متوقع: ${e.localizedMessage}"
                onComplete(false)
            } finally {
                _isAiLoading.value = false
            }
        }
    }
}
