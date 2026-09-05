package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_cards ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM wallet_cards WHERE id = :id")
    suspend fun getCardById(id: Long): CardEntity?

    @Query("SELECT * FROM wallet_cards WHERE cardType = :cardType ORDER BY isFavorite DESC, updatedAt DESC")
    fun getCardsByType(cardType: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM wallet_cards WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteCards(): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("DELETE FROM wallet_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)

    @Query("DELETE FROM wallet_cards")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM wallet_cards")
    fun getCardCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM wallet_cards")
    suspend fun getCardCountOnce(): Int
}
