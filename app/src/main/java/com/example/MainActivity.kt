package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Article
import com.example.ui.NewsViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CardBorderGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Wrap in RTL Layout Direction specifically for Arabic Interface
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NewsAppMainScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsAppMainScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = viewModel()
) {
    val context = LocalContext.current
    val articles by viewModel.articlesState.collectAsState()
    val breakingArticles by viewModel.breakingNews.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // State for viewing full article
    var activeArticleDetail by remember { mutableStateOf<Article?>(null) }
    
    // State for Custom Article dialog
    var showAddArticleDialog by remember { mutableStateOf(false) }

    // State for Custom AI generation theme dialog
    var showAiThemeDialog by remember { mutableStateOf(false) }

    // Trigger error messages as Toast
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. Dashboard Brand Header with generated Hero image background
            DashboardHeader(
                onAiClick = { showAiThemeDialog = true },
                isAiLoading = isAiLoading
            )

            // 2. Live Breaking News Ticker (شريط الأخبار العاجلة)
            if (breakingArticles.isNotEmpty()) {
                BreakingNewsTicker(
                    breakingNews = breakingArticles,
                    onArticleClick = { article ->
                        viewModel.incrementViews(article.id)
                        activeArticleDetail = article
                    }
                )
            }

            // 3. Search Bar
            SearchBarSection(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )

            // 4. Large Eye-comfortable Category Pills (أيقونات وفئات كبيرة واضحة)
            CategoryTabsSection(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            // 5. Responsive Articles Feed List / Grid
            if (articles.isEmpty()) {
                EmptyStateView(
                    category = selectedCategory,
                    query = searchQuery,
                    onGenerateDefault = { viewModel.generateAiNews() }
                )
            } else {
                ArticlesFeedSection(
                    articles = articles,
                    onArticleClick = { article ->
                        viewModel.incrementViews(article.id)
                        // Trigger locally updated model
                        activeArticleDetail = article.copy(viewsCount = article.viewsCount + 1)
                    },
                    onBookmarkToggle = { article ->
                        viewModel.toggleBookmark(article.id, article.isBookmarked)
                    },
                    onShareClick = { article ->
                        shareArticle(context, article)
                    },
                    onDeleteClick = { article ->
                        viewModel.deleteArticle(article.id)
                    }
                )
            }
        }

        // Floating Action Button (FAB) to Add Custom News (إضافة خبر جديد)
        FloatingActionButton(
            onClick = { showAddArticleDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .navigationBarsPadding()
                .testTag("add_article_fab"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة خبر جديد",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إضافة خبر",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Overlay 1: Article Detailed View Overlay (عرض تفاصيل الخبر بالكامل)
        AnimatedVisibility(
            visible = activeArticleDetail != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            activeArticleDetail?.let { article ->
                ArticleDetailsOverlay(
                    article = article,
                    onClose = { activeArticleDetail = null },
                    onBookmarkToggle = {
                        viewModel.toggleBookmark(article.id, article.isBookmarked)
                        // Update overlay state reactively
                        activeArticleDetail = article.copy(isBookmarked = !article.isBookmarked)
                    },
                    onShare = {
                        shareArticle(context, article)
                    }
                )
            }
        }

        // Overlay 2: Add Custom Article Dialog (نافذة إضافة خبر يدوي)
        if (showAddArticleDialog) {
            AddCustomArticleDialog(
                onDismiss = { showAddArticleDialog = false },
                onSave = { title, content, cat, source ->
                    viewModel.createCustomArticle(title, content, cat, source)
                    showAddArticleDialog = false
                    Toast.makeText(context, "تم نشر الخبر محلياً بنجاح!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Overlay 3: AI News Theme Input Dialog (توليد الأخبار بالذكاء الاصطناعي)
        if (showAiThemeDialog) {
            AiGenerationThemeDialog(
                onDismiss = { showAiThemeDialog = false },
                onGenerate = { theme ->
                    showAiThemeDialog = false
                    Toast.makeText(context, "جاري توليد الأخبار بالذكاء الاصطناعي...", Toast.LENGTH_SHORT).show()
                    viewModel.generateAiNews(theme) { success ->
                        if (success) {
                            Toast.makeText(context, "تم توليد الأخبار وتحديث القائمة بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

// ---------------- COMPONENTS ----------------

@Composable
fun DashboardHeader(
    onAiClick: () -> Unit,
    isAiLoading: Boolean
) {
    // Elegant Live Date Display in Arabic
    val arabicDate = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ar"))
        sdf.format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        // Hero Graphic Backdrop
        Image(
            painter = painterResource(id = R.drawable.img_hero_news_1783472142689),
            contentDescription = "خلفية واجهة الأخبار",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for reading contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Core Brand Details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large white branding with clean typography
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_1783472132924),
                            contentDescription = "شعار التطبيق",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "آخر الأخبار",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "مصداقية وسرعة على مدار الساعة",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // AI Sparkle Generation Button (ميزة التوليد الذكي)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isAiLoading) Color.White.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        )
                        .clickable(enabled = !isAiLoading) { onAiClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "توليد بالذكاء الاصطناعي",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAiLoading) "جاري التوليد..." else "توليد ذكي",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Arabic dynamic live date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = arabicDate,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                // Small badge highlighting Live state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE53935))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "مباشر",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BreakingNewsTicker(
    breakingNews: List<Article>,
    onArticleClick: (Article) -> Unit
) {
    // Simple state-based horizontal shifting ticker simulating a real-time live ticker
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(breakingNews) {
        while (true) {
            delay(5000) // Change news every 5 seconds
            if (breakingNews.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % breakingNews.size
            }
        }
    }

    if (breakingNews.isNotEmpty() && currentIndex < breakingNews.size) {
        val article = breakingNews[currentIndex]

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xFFD32F2F)) // Red background for urgent news
                .clickable { onArticleClick(article) }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "شريط عاجل",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "عاجل",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = article.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "مشاركة",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_bar"),
            placeholder = {
                Text(
                    text = "ابحث عن العناوين أو كلمات مفتاحية في الأخبار...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "مسح البحث",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CategoryTabsSection(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "الكل" to Icons.Default.Feed,
        "عاجل" to Icons.Default.FlashOn,
        "سياسة" to Icons.Default.Public,
        "اقتصاد" to Icons.Default.TrendingUp,
        "اجتماع" to Icons.Default.People,
        "المحفوظات" to Icons.Default.Bookmark
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { (categoryName, icon) ->
            val isSelected = selectedCategory == categoryName
            
            val contentColor = if (isSelected) Color.White else Color(0xFF065F46) // emerald-800
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
            val borderColor = if (isSelected) Color.Transparent else Color(0xFFD1FAE5) // emerald-100

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(containerColor)
                    .border(1.dp, borderColor, CircleShape)
                    .clickable { onCategorySelected(categoryName) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("category_pill_$categoryName"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = categoryName,
                        tint = if (categoryName == "عاجل" && !isSelected) Color(0xFFD32F2F) else contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = categoryName,
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ArticlesFeedSection(
    articles: List<Article>,
    onArticleClick: (Article) -> Unit,
    onBookmarkToggle: (Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onDeleteClick: (Article) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        
        // Responsive Layout: If width >= 600dp (such as tablets), show in double grid, else single column
        if (width >= 600.dp) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    NewsArticleCard(
                        article = article,
                        onClick = { onArticleClick(article) },
                        onBookmarkToggle = { onBookmarkToggle(article) },
                        onShareClick = { onShareClick(article) },
                        onDeleteClick = { onDeleteClick(article) }
                    )
                }
                
                // Centered copyright block at bottom of scrollable grid
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    CopyrightFooter()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, start = 20.dp, end = 20.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    NewsArticleCard(
                        article = article,
                        onClick = { onArticleClick(article) },
                        onBookmarkToggle = { onBookmarkToggle(article) },
                        onShareClick = { onShareClick(article) },
                        onDeleteClick = { onDeleteClick(article) }
                    )
                }
                
                // Center copyright footer at bottom of single column list
                item {
                    CopyrightFooter()
                }
            }
        }
    }
}

@Composable
fun NewsArticleCard(
    article: Article,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, CardBorderGreen, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("article_card_${article.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            
            // Visual Card Header: Decorative or Category Theme Block
            val headerColor = when (article.category) {
                "عاجل" -> Color(0xFFEF4444) // Red-500
                "سياسة" -> Color(0xFF065F46) // Emerald-800
                "اقتصاد" -> Color(0xFF047857) // Emerald-700
                "اجتماع" -> Color(0xFF10B981) // Emerald-500
                else -> Color(0xFF0F172A)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(headerColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                
                // Source and Category Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.0.dp)
                                .background(headerColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = article.category,
                            color = headerColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "•  ${article.source}",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Views Counter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "مشاهدات",
                            tint = Color.LightGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = article.viewsCount.toString(),
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title (Arabic, clean, readable)
                Text(
                    text = article.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 25.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content Snippet
                Text(
                    text = article.content,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 2,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time Elapsed and Interactive Action Row (Large easily clickable icons > 48dp target sizes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimeElapsedArabic(article.timestamp),
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row {
                        // Delete Local User Added Stories (If source is User or contributed)
                        if (article.source == "مساهمة مجتمعية" || article.source == "مراسل محلي") {
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFC62828))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف الخبر",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Bookmark Button
                        IconButton(
                            onClick = onBookmarkToggle,
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (article.isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Icon(
                                imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "حفظ في المفضلة",
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة الخبر",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleDetailsOverlay(
    article: Article,
    onClose: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit
) {
    // Zoom/Scale controls for accessible reading (أحجام خط مريحة وواضحة)
    var textScale by remember { mutableStateOf(16f) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("article_detail_overlay"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Details Header Backbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Accessibility controls: Font Adjuster
                    IconButton(
                        onClick = { if (textScale > 12f) textScale -= 2f },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Text(
                            text = "أ-",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = { if (textScale < 28f) textScale += 2f },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Text(
                            text = "أ+",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Bookmark
                    IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "حفظ",
                            tint = if (article.isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Share
                    IconButton(onClick = onShare, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Article core content container
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                
                // Large Category Badge
                val headerColor = when (article.category) {
                    "عاجل" -> Color(0xFFEF4444) // Red-500
                    "سياسة" -> Color(0xFF065F46) // Emerald-800
                    "اقتصاد" -> Color(0xFF047857) // Emerald-700
                    "اجتماع" -> Color(0xFF10B981) // Emerald-500
                    else -> Color(0xFF0F172A)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(headerColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = article.category,
                        color = headerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Arabic Title
                Text(
                    text = article.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Source metadata and clock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المصدر: ${article.source}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Text(
                        text = formatTimeElapsedArabic(article.timestamp),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                
                // Subline separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Full Article Content
                Text(
                    text = article.content,
                    fontSize = textScale.sp,
                    lineHeight = (textScale * 1.6f).sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Additional footer branding for visual balance
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "معلومات",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مصدر هذا الخبر الرسمي هو: ${article.source}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun EmptyStateView(
    category: String,
    query: String,
    onGenerateDefault: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Feed,
            contentDescription = "لا يوجد أخبار",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isNotEmpty()) "لم نجد نتائج للبحث المكتوب!" else "لا توجد أخبار في فئة \"$category\" حالياً.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (query.isNotEmpty()) "يرجى تعديل الكلمات المدخلة أو البحث بصياغة أخرى." else "يمكنك توليد أخبار جديدة وحصرية فوراً باستخدام قوة الذكاء الاصطناعي بالضغط على الزر أدناه.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.widthIn(max = 280.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (query.isEmpty()) {
            Button(
                onClick = onGenerateDefault,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 180.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "توليد الأخبار",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "توليد أخبار عاجلة الآن",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CopyrightFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(3.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "حقوق النشر والملكيات محفوظة © أبو حسام Aliasedan 2026",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "جميع الحقوق محفوظة لوكالة آخر الأخبار الإخبارية",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddCustomArticleDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String, source: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("عاجل") }

    val categories = listOf("عاجل", "سياسة", "اقتصاد", "اجتماع")

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نشر خبر جديد محلياً",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان الخبر المباشر") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثال: تدشين مركز تكنولوجي جديد في المدينة") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("تفاصيل ومحتوى الخبر الإخباري") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("اكتب تفاصيل الخبر بشكل متكامل وواضح هنا...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Source
                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text("المصدر المنسوب") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثال: مراسل محلي / شاهد عيان (تلقائي: مساهمة مجتمعية)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category selection radio
                    Text(
                        text = "اختر فئة الخبر لتصنيفه:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column {
                        categories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit & Cancel Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                            Text(text = "إلغاء", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && content.isNotBlank()) {
                                    val finalSource = source.ifBlank { "مراسل محلي" }
                                    onSave(title, content, selectedCategory, finalSource)
                                } else {
                                    // Normally we would validate inputs
                                }
                            },
                            enabled = title.isNotBlank() && content.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(text = "نشر الآن", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiGenerationThemeDialog(
    onDismiss: () -> Unit,
    onGenerate: (theme: String?) -> Unit
) {
    var themeQuery by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "توليد ذكي",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "توليد بالذكاء الاصطناعي",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "قم بتحديث وتغذية تطبيق الأخبار بأحدث المستجدات الفورية التي يولدها نموذج Gemini AI باللغة العربية الفصحى.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input theme (optional)
                    OutlinedTextField(
                        value = themeQuery,
                        onValueChange = { themeQuery = it },
                        label = { Text("موضوع الأخبار المفضّل (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثال: أخبار التكنولوجيا، الفضاء، البيئة...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "اتركه فارغاً لتوليد باقة متنوعة من الأخبار السياسية والاقتصادية والاجتماعية تلقائياً.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                            Text(text = "إلغاء", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { onGenerate(themeQuery.ifBlank { null }) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "تحديث",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ابدأ التوليد",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to share article contents securely
fun shareArticle(context: Context, article: Article) {
    try {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.title)
            putExtra(Intent.EXTRA_TEXT, """
                🚨 *${article.title}*
                
                ${article.content}
                
                🌐 المصدر الرسمي للخبر: ${article.source}
                📅 الوقت المقدر: ${formatTimeElapsedArabic(article.timestamp)}
                
                📥 تمت المشاركة من تطبيق "آخر الأخبار" الاحترافي.
            """.trimIndent())
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الخبر عبر:"))
    } catch (e: Exception) {
        Toast.makeText(context, "حدث خطأ أثناء محاولة المشاركة.", Toast.LENGTH_SHORT).show()
    }
}

// Helper to format time strings for visual polish
fun formatTimeElapsedArabic(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / 60000
    if (mins < 1) return "الآن"
    if (mins < 60) {
        return "منذ $mins دقيقة"
    }
    val hours = mins / 60
    if (hours < 24) {
        return "منذ $hours ساعة"
    }
    val days = hours / 24
    if (days == 1L) return "أمس"
    if (days == 2L) return "منذ يومين"
    return "منذ $days أيام"
}
