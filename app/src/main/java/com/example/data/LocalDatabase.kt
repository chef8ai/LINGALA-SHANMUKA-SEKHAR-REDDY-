package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., "Produce", "Dairy", "Proteins", "Pantry", "Spices", "Other"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_recipes")
data class SavedRecipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val cookTime: String,
    val wasteSavedText: String,   // Explanation of leftovers reused
    val ingredientsListText: String, // Comma-separated list of ingredients used
    val instructions: String,  // List of step-by-step instructions (newline-delimited)
    val nutrition: String,     // Nutritional details (e.g. Calories, Protein, etc.)
    val savedAt: Long = System.currentTimeMillis()
)

// --- DAOs (Data Access Objects) ---

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients")
    suspend fun clearAllIngredients()
}

@Dao
interface SavedRecipeDao {
    @Query("SELECT * FROM saved_recipes ORDER BY savedAt DESC")
    fun getAllSavedRecipes(): Flow<List<SavedRecipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: SavedRecipe): Long

    @Delete
    suspend fun deleteRecipe(recipe: SavedRecipe)
}

// --- App Database ---

@Database(entities = [Ingredient::class, SavedRecipe::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun savedRecipeDao(): SavedRecipeDao
}

// --- Repository ---

class FridgeChefRepository(private val db: AppDatabase) {
    val ingredients: Flow<List<Ingredient>> = db.ingredientDao().getAllIngredients()
    val savedRecipes: Flow<List<SavedRecipe>> = db.savedRecipeDao().getAllSavedRecipes()

    suspend fun addIngredient(name: String, category: String) {
        db.ingredientDao().insertIngredient(
            Ingredient(name = name.trim().lowercase().capitalize(), category = category)
        )
    }

    suspend fun removeIngredient(ingredient: Ingredient) {
        db.ingredientDao().deleteIngredient(ingredient)
    }

    suspend fun clearFridge() {
        db.ingredientDao().clearAllIngredients()
    }

    suspend fun saveRecipe(recipe: SavedRecipe): Long {
        return db.savedRecipeDao().insertRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: SavedRecipe) {
        db.savedRecipeDao().deleteRecipe(recipe)
    }
}
