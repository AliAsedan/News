package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "عاجل", "سياسة", "اقتصاد", "اجتماع"
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "وكالة الأخبار العربية",
    val imageUrl: String? = null,
    val isBookmarked: Boolean = false,
    val viewsCount: Int = 0
)

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY timestamp DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY timestamp DESC")
    fun getArticlesByCategory(category: String): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchArticles(query: String): Flow<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<Article>)

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: Int, isBookmarked: Boolean)

    @Query("UPDATE articles SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViews(id: Int)

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteArticleById(id: Int)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getCount(): Int
}

@Database(entities = [Article::class], version = 1, exportSchema = false)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: NewsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "news_database"
                )
                .addCallback(NewsDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class NewsDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.articleDao())
                }
            }
        }

        suspend fun populateDatabase(articleDao: ArticleDao) {
            // Check if we already have items to prevent double insertions
            if (articleDao.getCount() > 0) return

            val now = System.currentTimeMillis()
            val initialArticles = listOf(
                Article(
                    title = "عاجل | إطلاق مشروع وطني ضخم للتحول الرقمي والخدمات الحكومية الذكية",
                    content = "أعلنت وزارة الاتصالات اليوم عن إطلاق الخطة الشاملة للتحول الرقمي الكامل، والتي تهدف إلى رقمنة جميع المعاملات الحكومية وتوفيرها عبر منصة موحدة فائقة السرعة، تسهيلاً على المواطنين والمستثمرين، وضمن رؤية التنمية المستدامة لعام 2026.",
                    category = "عاجل",
                    timestamp = now - 600000, // 10 mins ago
                    source = "مراسل عاجل",
                    viewsCount = 1250
                ),
                Article(
                    title = "انطلاق أعمال القمة الاقتصادية لتعزيز الاستثمارات المشتركة وتنشيط الأسواق المحلية",
                    content = "بدأت اليوم فعاليات القمة الاقتصادية الكبرى بمشاركة واسعة من كبار المستثمرين وقادة الشركات العربية والعالمية. تناقش القمة سبل تيسير التبادل التجاري وتوطين الصناعات المتقدمة، مع توقيع اتفاقيات استثمارية كبرى تقدر بمليارات الدولارات لدعم البنية التحتية المستدامة والمشاريع الواعدة.",
                    category = "اقتصاد",
                    timestamp = now - 3600000, // 1 hour ago
                    source = "القسم الاقتصادي",
                    viewsCount = 740
                ),
                Article(
                    title = "وزارة التربية والتعليم تطلق مبادرة وطنية لتطوير مهارات الذكاء الاصطناعي بالمدارس",
                    content = "بهدف إعداد جيل متمكن تكنولوجياً، أطلقت وزارة التعليم مبادرة شاملة لدمج مفاهيم البرمجة والذكاء الاصطناعي في المناهج الدراسية، مع توفير مختبرات رقمية متطورة وتدريب مئات المعلمين لتقديم تجربة تعليمية مبتكرة تواكب التطور المتسارع عالمياً.",
                    category = "اجتماع",
                    timestamp = now - 7200000, // 2 hours ago
                    source = "الشؤون الاجتماعية",
                    viewsCount = 482
                ),
                Article(
                    title = "مباحثات سياسية رفيعة المستوى لتعزيز الأمن والسلام والتعاون الإقليمي المشترك",
                    content = "شهدت العاصمة اليوم اجتماعات سياسية ثنائية مكثفة ركزت على ملفات التنسيق الأمني والسياسي لمواجهة التحديات الراهنة. وأكد البيان الختامي على عمق العلاقات الأخوية والالتزام الكامل بالعمل المشترك لترسيخ أسس السلام الدائم ودعم الاستقرار الإقليمي.",
                    category = "سياسة",
                    timestamp = now - 14400000, // 4 hours ago
                    source = "المحرر السياسي",
                    viewsCount = 952
                ),
                Article(
                    title = "عاجل | البنك المركزي يعلن عن تسهيلات غير مسبوقة لدعم المشاريع الصغيرة والناشئة",
                    content = "أصدر البنك المركزي حزمة جديدة من الحوافز والمبادرات التمويلية بفترات سداد ميسرة وفائدة مخفضة جداً، وذلك لتشجيع ريادة الأعمال ودعم المشاريع الشبابية المبتكرة وتنشيط بيئة العمل الاستثمارية المحلية.",
                    category = "عاجل",
                    timestamp = now - 18000000, // 5 hours ago
                    source = "مراسل عاجل",
                    viewsCount = 2030
                ),
                Article(
                    title = "طفرة سياحية جديدة: زيادة كبيرة في أعداد الزوار وتطوير مرافق تاريخية واعدة",
                    content = "سجل قطاع السياحة أرقاماً قياسية جديدة خلال الموسم الحالي بفضل التسهيلات الجديدة في التأشيرات وافتتاح عدد من المواقع الأثرية والترفيهية بعد صيانتها وتأهيلها بالكامل وفق أعلى المعايير العالمية لخدمة الضيوف والزوار.",
                    category = "اجتماع",
                    timestamp = now - 28800000, // 8 hours ago
                    source = "أخبار المجتمع",
                    viewsCount = 310
                ),
                Article(
                    title = "توقيع اتفاقية تعاون استراتيجي لتطوير مصادر الطاقة النظيفة ومكافحة الانبعاثات",
                    content = "وقعت وزارة الطاقة اتفاقية تاريخية مع شركات رائدة لإنشاء محطات توليد طاقة شمسية وطاقة رياح كبرى، مما يساهم بشكل فعال في تحقيق الحياد الكربوني وتأمين طاقة مستدامة وصديقة للبيئة للأجيال القادمة.",
                    category = "اقتصاد",
                    timestamp = now - 43200000, // 12 hours ago
                    source = "القسم الاقتصادي",
                    viewsCount = 560
                )
            )
            articleDao.insertArticles(initialArticles)
        }
    }
}
