package com.example.data

import kotlinx.coroutines.flow.Flow

class NewsRepository(private val articleDao: ArticleDao) {

    val allArticles: Flow<List<Article>> = articleDao.getAllArticles()
    val bookmarkedArticles: Flow<List<Article>> = articleDao.getBookmarkedArticles()

    fun getArticlesByCategory(category: String): Flow<List<Article>> {
        return if (category == "الكل") {
            articleDao.getAllArticles()
        } else {
            articleDao.getArticlesByCategory(category)
        }
    }

    fun searchArticles(query: String): Flow<List<Article>> {
        return articleDao.searchArticles(query)
    }

    suspend fun insertArticle(article: Article) {
        articleDao.insertArticle(article)
    }

    suspend fun updateBookmarkStatus(id: Int, isBookmarked: Boolean) {
        articleDao.updateBookmarkStatus(id, isBookmarked)
    }

    suspend fun incrementViews(id: Int) {
        articleDao.incrementViews(id)
    }

    suspend fun deleteArticleById(id: Int) {
        articleDao.deleteArticleById(id)
    }
}
