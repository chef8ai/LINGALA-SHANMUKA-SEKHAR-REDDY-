package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.api.FridgeChefAIResponse
import com.example.data.Ingredient
import com.example.data.SavedRecipe
import com.example.ui.theme.*
import com.example.viewmodel.*
import kotlinx.coroutines.delay

enum class ActiveTab {
    FRIDGE,
    RECIPE_LAB,
    COOKBOOK
}

// --- Main Framework Composable ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeChefMainScreen(
    viewModel: FridgeViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ActiveTab.FRIDGE) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FridgeChef",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerracottaAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Zero Waste",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (userEmail.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = userEmail.substringBefore("@"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.logout() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == ActiveTab.FRIDGE,
                    onClick = { activeTab = ActiveTab.FRIDGE },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == ActiveTab.FRIDGE) Icons.Filled.Kitchen else Icons.Outlined.Kitchen,
                            contentDescription = "My Fridge"
                        )
                    },
                    label = { Text("My Fridge") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SageGreenPrimary,
                        selectedTextColor = SageGreenPrimary,
                        indicatorColor = SageGreenLight
                    )
                )

                NavigationBarItem(
                    selected = activeTab == ActiveTab.RECIPE_LAB,
                    onClick = { activeTab = ActiveTab.RECIPE_LAB },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == ActiveTab.RECIPE_LAB) Icons.Filled.Restaurant else Icons.Outlined.Restaurant,
                            contentDescription = "Recipe Lab"
                        )
                    },
                    label = { Text("Recipe Lab") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SageGreenPrimary,
                        selectedTextColor = SageGreenPrimary,
                        indicatorColor = SageGreenLight
                    )
                )

                NavigationBarItem(
                    selected = activeTab == ActiveTab.COOKBOOK,
                    onClick = { activeTab = ActiveTab.COOKBOOK },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == ActiveTab.COOKBOOK) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = "My Cookbook"
                        )
                    },
                    label = { Text("Cookbook") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SageGreenPrimary,
                        selectedTextColor = SageGreenPrimary,
                        indicatorColor = SageGreenLight
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                ActiveTab.FRIDGE -> FridgeInventoryScreen(viewModel = viewModel)
                ActiveTab.RECIPE_LAB -> RecipeLabScreen(viewModel = viewModel)
                ActiveTab.COOKBOOK -> CookbookScreen(viewModel = viewModel)
            }
        }
    }
}

// ==========================================
// 1. MY FRIDGE SCREEN (INVENTORY MANAGEMENT)
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FridgeInventoryScreen(viewModel: FridgeViewModel) {
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val scanUiState by viewModel.scanUiState.collectAsStateWithLifecycle()

    var ingredientName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateByCode("Produce") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Quick add lists
    val popularList = listOf(
        "Egg" to "Proteins",
        "Milk" to "Dairy",
        "Tomato" to "Produce",
        "Bread" to "Pantry",
        "Cheese" to "Dairy",
        "Chicken" to "Proteins",
        "Butter" to "Dairy",
        "Rice" to "Pantry",
        "Potato" to "Produce",
        "Onion" to "Produce"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SageGreenLight
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fill Your Fridge Inventory 🥑",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tell FridgeChef what is about to expire, and let AI generate eco-friendly feast guides!",
                            fontSize = 13.sp,
                            color = SageGreenDark.copy(alpha = 0.82f)
                        )
                    }
                }
            }
        }

        // Add Input Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add Ingredient Leftover",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = ingredientName,
                            onValueChange = { ingredientName = it },
                            placeholder = { Text("E.g., leftover Spinach, heavy cream") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (ingredientName.isNotBlank()) {
                                        viewModel.addIngredient(ingredientName, selectedCategory)
                                        ingredientName = ""
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (ingredientName.isNotBlank()) {
                                    viewModel.addIngredient(ingredientName, selectedCategory)
                                    ingredientName = ""
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Categories Selector
                    Text(
                        text = "Category Tag",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val categories = listOf("Produce", "Dairy", "Proteins", "Pantry", "Spices", "Other")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenLight,
                                    selectedLabelColor = SageGreenDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // Quick Add Staples
        item {
            Column {
                Text(
                    text = "Quick Tap Kitchen Staples",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    popularList.forEach { (staple, cat) ->
                        // Only show if not already present
                        val alreadyHas = ingredients.any { it.name.lowercase() == staple.lowercase() }
                        if (!alreadyHas) {
                            InputChip(
                                selected = false,
                                onClick = {
                                    viewModel.addIngredient(staple, cat)
                                },
                                label = { Text(staple) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Camera Simulated Scanners
        item {
            Card(
                border = BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = TerracottaAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Fridge Photo Scan 📸",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Because you are in an emulator, tap a simulated fridge photo below and let our vision AI scan the leftovers!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    viewModel.simulatedPhotoTemplates.forEach { template ->
                        OutlinedButton(
                            onClick = { viewModel.runSimulatedPhotoScan(template) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = template.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Scans: " + template.items.joinToString { it.first },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Scan",
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Fridge Inventory List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "My Stored Leftovers",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = SageGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = ingredients.size.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (ingredients.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearFridgeIngredients() },
                        colors = ButtonDefaults.textButtonColors(contentColor = TerracottaAccent)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Fridge", fontSize = 12.sp)
                    }
                }
            }
        }

        // Empty Status
        if (ingredients.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Kitchen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your fridge is empty!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Type item leftovers or click an AI Photo simulation to fill up your inventory.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            // Sorted Grouping of Stored items
            val grouped = ingredients.groupBy { it.category }
            grouped.forEach { (catName, catItems) ->
                item {
                    Column {
                        Text(
                            text = catName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenPrimary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            catItems.forEach { ing ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = ing.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.removeIngredient(ing) }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Spacer to push content up
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Camera Scanning Dialog Animation Overlay
    if (scanUiState != ScanUiState.Idle) {
        Dialog(onDismissRequest = { viewModel.resetScanState() }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (scanUiState) {
                        is ScanUiState.Scanning -> {
                            Text(
                                text = "AI Vision Scanner is active...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            // Custom scanning circular loader
                            CircularProgressIndicator(
                                color = SageGreenPrimary,
                                strokeWidth = 5.dp,
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Decomposing leftover parameters and isolating food boundaries...",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        is ScanUiState.Complete -> {
                            val itemsAdded = (scanUiState as ScanUiState.Complete).ingredientsAdded
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Scan Completed!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SageGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "We've safely logged ${itemsAdded.size} leftovers to your inventory database:",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            // List items
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                itemsAdded.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.resetScanState() },
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Acknowledge & Cook")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

// Helper to handle remember code wrapper in compose
@Composable
fun rememberStateByCode(initial: String): MutableState<String> {
    return remember { mutableStateOf(initial) }
}

// Helper for property delegators in local compose layout
fun mutableStateByCode(initial: String) = mutableStateOf(initial)

// ==========================================
// 2. RECIPE LAB SCREEN (GENERATION & DISPLAY)
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeLabScreen(viewModel: FridgeViewModel) {
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val recipeUiState by viewModel.recipeUiState.collectAsStateWithLifecycle()

    val mealType by viewModel.selectedMealType.collectAsStateWithLifecycle()
    val diet by viewModel.selectedDiet.collectAsStateWithLifecycle()
    val prepTime by viewModel.selectedPrepTime.collectAsStateWithLifecycle()

    var showSavedOverlay by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Lab Setup Info
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = TerracottaLight
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FridgeChef AI Alchemy Lab 🧪",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set your eating criteria below, and FridgeChef will process your fridge ingredients to generate zero-waste culinary recipes.",
                            fontSize = 13.sp,
                            color = TerracottaAccent.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Generation Config Settings Form
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customize Your Cook Parameters",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. MEAL FORM SELECTOR
                    Text(
                        text = "Meal Form Goal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val mealTypes = MealType.values()
                        // Grid like layout using two rows or standard chips
                        // row 1
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mealTypes.forEach { type ->
                                val selected = mealType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.selectedMealType.value = type },
                                    label = { Text(type.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TerracottaLight,
                                        selectedLabelColor = TerracottaAccent
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. DIET SELECTOR
                    Text(
                        text = "Dietary Profile Preference",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DietaryRestriction.values().forEach { d ->
                            val selected = diet == d
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.selectedDiet.value = d },
                                label = { Text(d.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenLight,
                                    selectedLabelColor = SageGreenDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. COOKING TIME BUDGET
                    Text(
                        text = "Cooking Time Budget",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PrepTime.values().forEach { pt ->
                            val selected = prepTime == pt
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.selectedPrepTime.value = pt },
                                label = { Text(pt.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreenLight,
                                    selectedLabelColor = SageGreenDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Generate Primary Trigger Call
                    Button(
                        onClick = { viewModel.generateAIZeroWasteRecipe() },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Restaurant, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Conjure Zero-Waste Recipe! ✨",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Recipe Display Segment
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Culinary AI Formulation Output",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                AnimatedContent(
                    targetState = recipeUiState,
                    transitionSpec = {
                        fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                    }
                ) { state ->
                    when (state) {
                        is RecipeUiState.Idle -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Laboratory is dormant",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Confirm your parameters above and hit Generate! Your stored leftovers count: ${ingredients.size}.",
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        is RecipeUiState.Loading -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            color = TerracottaAccent,
                                            strokeWidth = 4.dp,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Eco,
                                            contentDescription = null,
                                            tint = SageGreenPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = "Stirring ingredients with AI...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Cycle helper tip descriptions
                                    var currentTipIndex by remember { mutableStateByCode("Configuring optimal heat balances...") }
                                    LaunchedEffect(Unit) {
                                        val tipsList = listOf(
                                            "Estimating leftover caloric densities...",
                                            "Squeezing out nutrient metrics...",
                                            "Bypassing trash bins to avoid green-house gases...",
                                            "Structuring safe culinary steps..."
                                        )
                                        var idx = 0
                                        while (true) {
                                            delay(1500)
                                            currentTipIndex = tipsList[idx]
                                            idx = (idx + 1) % tipsList.size
                                        }
                                    }

                                    Text(
                                        text = currentTipIndex,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        is RecipeUiState.Success -> {
                            val recipe = state.recipe
                            ElevatedCard(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Title & Time Summary row
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = recipe.recipeName,
                                                fontSize = 20.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Cook time pill
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = SageGreenLight,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Timer,
                                                    contentDescription = "Time",
                                                    tint = SageGreenDark,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = recipe.cookTime,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SageGreenDark
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Nutrition row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("⚡ " + recipe.calories) }
                                        )
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("🍖 Protein: " + recipe.protein) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Eco waste-saver statement!
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SageGreenLight.copy(alpha = 0.5f))
                                            .border(1.dp, SageGreenPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            Icon(
                                                imageVector = Icons.Default.Eco,
                                                contentDescription = null,
                                                tint = SageGreenPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Eco-Savings Log",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SageGreenDark
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = recipe.wasteSavedText,
                                                    fontSize = 12.sp,
                                                    color = SageGreenDark.copy(alpha = 0.9f),
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Ingredient list
                                    Text(
                                        text = "Matched Ingredients List",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    recipe.ingredients.forEach { item ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SageGreenPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Steps
                                    Text(
                                        text = "Preparation Steps",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    recipe.instructions.forEachIndexed { index, step ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = TerracottaAccent.copy(alpha = 0.15f),
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = (index + 1).toString(),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TerracottaAccent
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = step,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Bookmark button
                                    Button(
                                        onClick = {
                                            viewModel.saveRecipeToHistory(recipe)
                                            showSavedOverlay = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save to My Cookbook Collection 🔖")
                                    }
                                }
                            }
                        }
                        is RecipeUiState.Error -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Formula Interruption",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = state.message,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Space at bottom
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Success Toast Overlay Dialog
    if (showSavedOverlay) {
        Dialog(onDismissRequest = { showSavedOverlay = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = TerracottaAccent,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Added to Cookbook!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This recipe is now saved locally in your offline bookshelf! You can browse, read, and delete it anytime in the Cookbook tab.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showSavedOverlay = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                    ) {
                        Text("Awesome")
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. MY COOKBOOK SCREEN (LOCAL HISTORY SAVED)
// ==========================================

@Composable
fun CookbookScreen(viewModel: FridgeViewModel) {
    val savedRecipes by viewModel.savedRecipes.collectAsStateWithLifecycle()

    // Track which item indices/ids are expanded
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Welcoming Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SageGreenLight
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Saved Zero-Waste Cookbook 📚",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your personalized eco-feast recipes stored offline. Redo your favorites to save green and avoid food waste!",
                            fontSize = 13.sp,
                            color = SageGreenDark.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Header for Bookshelf Count
        item {
            Text(
                text = "Bookshelf Collection (${savedRecipes.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        if (savedRecipes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bookshelf is empty!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You haven't added any formulation templates yet. Go generate and bookmark one in the Recipe Lab!",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(savedRecipes, key = { it.id }) { recipe ->
                val isExpanded = expandedStates[recipe.id] ?: false

                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedStates[recipe.id] = !isExpanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recipe.title,
                                    fontSize = 16.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = TerracottaAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = recipe.cookTime,
                                        fontSize = 11.sp,
                                        color = TerracottaAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = recipe.nutrition.split("|").firstOrNull()?.trim() ?: "",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.deleteSavedRecipe(recipe) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.82f)
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // Collapsible detailed instructions
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth()
                            ) {
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Eco-Savings Block
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SageGreenLight.copy(alpha = 0.5f))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "🌍 Saved Leftover Footprint",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = recipe.wasteSavedText,
                                        fontSize = 12.sp,
                                        color = SageGreenDark.copy(alpha = 0.9f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Ingredients Section
                                Text(
                                    text = "Required Ingredients",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = recipe.ingredientsListText,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Step-by-Step cooking details
                                Text(
                                    text = "Cooking Steps",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val steps = recipe.instructions.split("\n")
                                steps.forEachIndexed { index, st ->
                                    if (st.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.padding(vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "${index + 1}. ",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = TerracottaAccent
                                            )
                                            Text(
                                                text = st.trim(),
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
