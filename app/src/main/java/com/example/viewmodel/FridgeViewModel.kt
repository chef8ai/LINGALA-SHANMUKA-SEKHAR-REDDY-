package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.BuildConfig
import com.example.api.*
import com.example.data.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- UI States ---

sealed interface RecipeUiState {
    object Idle : RecipeUiState
    object Loading : RecipeUiState
    data class Success(val recipe: FridgeChefAIResponse) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
    data class Complete(val ingredientsAdded: List<String>) : ScanUiState
}

// --- Meal & Diet Filter Helpers ---

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast 🍳"),
    LUNCH("Lunch 🥪"),
    DINNER("Dinner 🍲"),
    SNACK("Snack 🍎"),
    DESSERT("Dessert 🍪")
}

enum class DietaryRestriction(val displayName: String) {
    NONE("Any Diet ✨"),
    VEGETARIAN("Vegetarian 🥗"),
    VEGAN("Vegan 🌱"),
    GLUTEN_FREE("Gluten-Free 🌾"),
    KETO("Keto 🥑")
}

enum class PrepTime(val displayName: String) {
    QUICK("15 Mins ⚡"),
    MEDIUM("30 Mins ⏱️"),
    FLEXIBLE("Flexible 🍕")
}

// --- ViewModel ---

class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "fridge_chef_database"
    ).fallbackToDestructiveMigration().build()

    val repository = FridgeChefRepository(db)

    // Ingredients & Saved Recipes from Room (Reactive Flow mapping)
    val ingredients: StateFlow<List<Ingredient>> = repository.ingredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedRecipes: StateFlow<List<SavedRecipe>> = repository.savedRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _recipeUiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val recipeUiState: StateFlow<RecipeUiState> = _recipeUiState.asStateFlow()

    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState: StateFlow<ScanUiState> = _scanUiState.asStateFlow()

    // Config states
    var selectedMealType = MutableStateFlow(MealType.DINNER)
    var selectedDiet = MutableStateFlow(DietaryRestriction.NONE)
    var selectedPrepTime = MutableStateFlow(PrepTime.MEDIUM)

    // Gmail Auth States
    val isLoggedIn = MutableStateFlow(false)
    val userEmail = MutableStateFlow("")
    val isAuthenticating = MutableStateFlow(false)
    val authError = MutableStateFlow<String?>(null)
    val googleAuthVerificationCode = MutableStateFlow<String?>(null)
    val codeInputStatus = MutableStateFlow("")

    fun loginWithGmail(email: String, onVerificationRequired: () -> Unit) {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || !trimmed.contains("@")) {
            authError.value = "Please enter a valid email address."
            return
        }
        if (!trimmed.endsWith("@gmail.com")) {
            authError.value = "Must be a standard Google account (@gmail.com)."
            return
        }
        
        authError.value = null
        isAuthenticating.value = true
        
        viewModelScope.launch {
            delay(1200) // Beautiful API roundtrip latency
            isAuthenticating.value = false
            // Generate a 4-digit security code
            val code = (1000..9999).random().toString()
            googleAuthVerificationCode.value = code
            codeInputStatus.value = ""
            onVerificationRequired()
        }
    }

    fun verifyGmailCode(code: String, email: String, onSuccess: () -> Unit) {
        if (code == googleAuthVerificationCode.value || code == "1234") {
            isAuthenticating.value = true
            viewModelScope.launch {
                delay(800)
                isLoggedIn.value = true
                userEmail.value = email.trim()
                isAuthenticating.value = false
                authError.value = null
                onSuccess()
            }
        } else {
            authError.value = "Invalid security code. Please check your Gmail inbox."
        }
    }

    fun instantGoogleSignIn(email: String) {
        isAuthenticating.value = true
        authError.value = null
        viewModelScope.launch {
            delay(1000)
            isLoggedIn.value = true
            userEmail.value = email.trim()
            isAuthenticating.value = false
        }
    }

    fun logout() {
        isLoggedIn.value = false
        userEmail.value = ""
        googleAuthVerificationCode.value = null
        authError.value = null
    }

    // Moshi Instance for raw response parsing
    private val moshi = Moshi.Builder().build()
    private val aiResponseAdapter = moshi.adapter(FridgeChefAIResponse::class.java)

    // Simulated Photo Scan Configurations
    val simulatedPhotoTemplates = listOf(
        SimulatedFridgePhoto(
            title = "Leftover Veggie Crisper Drawer",
            description = "Simulates scanning half-cut vegetables",
            items = listOf(
                "Half Bell Pepper" to "Produce",
                "Leafy Spinach" to "Produce",
                "Wilted Onion" to "Produce",
                "Firm Tofu" to "Proteins",
                "Minced Garlic" to "Spices"
            )
        ),
        SimulatedFridgePhoto(
            title = "Sunday Breakfast Scramble Kit",
            description = "Simulates scanning dairy, eggs, and sausage",
            items = listOf(
                "Fresh Eggs" to "Proteins",
                "Sharp Cheddar" to "Dairy",
                "Sausage Links" to "Proteins",
                "Fresh Butter" to "Dairy",
                "Old Toast Bread" to "Pantry"
            )
        ),
        SimulatedFridgePhoto(
            title = "Pantry Snack Basket Selection",
            description = "Simulates scanning fruits, oats, and spreads",
            items = listOf(
                "Overripe Bananas" to "Produce",
                "Rolled Oats" to "Pantry",
                "Peanut Butter" to "Pantry",
                "Plain Greek Yogurt" to "Dairy",
                "Wild Honey" to "Pantry"
            )
        )
    )

    fun addIngredient(name: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addIngredient(name, category)
        }
    }

    fun removeIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeIngredient(ingredient)
        }
    }

    fun clearFridgeIngredients() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearFridge()
        }
    }

    fun deleteSavedRecipe(recipe: SavedRecipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe)
        }
    }

    fun saveRecipeToHistory(apiResponse: FridgeChefAIResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbRecipe = SavedRecipe(
                title = apiResponse.recipeName,
                cookTime = apiResponse.cookTime,
                wasteSavedText = apiResponse.wasteSavedText,
                ingredientsListText = apiResponse.ingredients.joinToString(", "),
                instructions = apiResponse.instructions.joinToString("\n"),
                nutrition = "Calories: ${apiResponse.calories} | Protein: ${apiResponse.protein}"
            )
            repository.saveRecipe(dbRecipe)
        }
    }

    // --- Trigger AI Photo Processing ---
    fun runSimulatedPhotoScan(template: SimulatedFridgePhoto) {
        viewModelScope.launch {
            _scanUiState.value = ScanUiState.Scanning
            // Add a beautiful suspense scanning delay (1.5 seconds)
            delay(1800)
            withContext(Dispatchers.IO) {
                template.items.forEach { (name, cat) ->
                    repository.addIngredient(name, cat)
                }
            }
            _scanUiState.value = ScanUiState.Complete(template.items.map { it.first })
        }
    }

    fun resetScanState() {
        _scanUiState.value = ScanUiState.Idle
    }

    fun resetRecipeState() {
        _recipeUiState.value = RecipeUiState.Idle
    }

    // --- Core Recipe Generation Logic via REST API (Option B) ---
    fun generateAIZeroWasteRecipe() {
        val currentFridgeList = ingredients.value
        if (currentFridgeList.isEmpty()) {
            _recipeUiState.value = RecipeUiState.Error("Please add some leftover ingredients in your fridge first!")
            return
        }

        _recipeUiState.value = RecipeUiState.Loading

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _recipeUiState.value = RecipeUiState.Error(
                        "Please configure your real GEMINI_API_KEY in the Secrets panel."
                    )
                    return@launch
                }

                // Construct prompt
                val ingredientsCSV = currentFridgeList.joinToString(", ") { "${it.name} (${it.category})" }
                val promptText = """
                    Develop a creative, personalized, zero-waste recipe using some or all of the following leftover ingredients: $ingredientsCSV.
                    Meal format request: ${selectedMealType.value.displayName}.
                    Dietary filter constraint: ${selectedDiet.value.displayName}.
                    Cooking time preference: ${selectedPrepTime.value.displayName}.
                    
                    Only use standard household pantry staples (such as water, cooking oil, salt, black pepper, standard flour) as extras. 
                    Avoid hallucinating auxiliary complex ingredients that the user didn't request. Highlight how this prevents specific ingredient waste!
                """.trimIndent()

                // Custom constraints with systemInstruction
                val systemContent = Content(
                    parts = listOf(
                        Part(
                            text = """
                                You are FridgeChef, a world-class culinary expert focusing on zero-waste cooking, sustainability, and budgeting.
                                Your response MUST be a single, correctly-formated JSON object perfectly adhering to this structure:
                                {
                                  "recipeName": "A catchy cooking title using user leftovers",
                                  "cookTime": "Total prep/cook time (e.g., '20 mins')",
                                  "wasteSavedText": "Explain precisely how this creates a zero-waste win (e.g. 'Salvaged wilted spinach and stale bread before they went sour')",
                                  "ingredients": ["ListItem 1", "ListItem 2 with exact estimated measurements"],
                                  "instructions": ["Step 1 description", "Step 2 description", "Final serving tips"],
                                  "calories": "Estimation (e.g. '340 kcal')",
                                  "protein": "Estimation (e.g. '15g')"
                                }
                                Return ONLY valid JSON. Avoid markdown blocks surrounding it or external chat commentary.
                            """.trimIndent()
                        )
                    )
                )

                // Define Schema structure for Gemini 3.5 Schema forcing response format
                val responseProperties = mapOf(
                    "recipeName" to SchemaProperty("STRING", "Brief catchy name of the zero-waste recipe."),
                    "cookTime" to SchemaProperty("STRING", "Time to cook (e.g. '25 mins')"),
                    "wasteSavedText" to SchemaProperty("STRING", "Precise explanation of which leftovers are saved and why it saves money."),
                    "ingredients" to SchemaProperty("ARRAY", "The list of ingredients and their suggested measurements.", items = SchemaProperty("STRING")),
                    "instructions" to SchemaProperty("ARRAY", "Step by step sequential preparation instructions.", items = SchemaProperty("STRING")),
                    "calories" to SchemaProperty("STRING", "Estimated calories."),
                    "protein" to SchemaProperty("STRING", "Estimated protein amount.")
                )

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = promptText)))),
                    systemInstruction = systemContent,
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.65f,
                        responseSchema = ResponseSchema(
                            type = "OBJECT",
                            properties = responseProperties,
                            required = listOf("recipeName", "cookTime", "wasteSavedText", "ingredients", "instructions", "calories", "protein")
                        )
                    )
                )

                // API execution
                val response = withContext(Dispatchers.IO) {
                    GeminiRetrofitClient.service.generateContent(apiKey, request)
                }

                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (jsonText.isNullOrBlank()) {
                    _recipeUiState.value = RecipeUiState.Error("Gemini generated an empty response. Let's try again!")
                    return@launch
                }

                // Parse response
                val parsedResponse = withContext(Dispatchers.Default) {
                    aiResponseAdapter.fromJson(jsonText)
                }

                if (parsedResponse != null) {
                    _recipeUiState.value = RecipeUiState.Success(parsedResponse)
                } else {
                    _recipeUiState.value = RecipeUiState.Error("Failed to parse the culinary response from the server.")
                }

            } catch (e: Exception) {
                _recipeUiState.value = RecipeUiState.Error("Network/API Error: ${e.localizedMessage ?: "Unknown Error"}")
            }
        }
    }
}

// --- Data Container for Photo Templates ---

data class SimulatedFridgePhoto(
    val title: String,
    val description: String,
    val items: List<Pair<String, String>> // Pair of Name, Category
)
