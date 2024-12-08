import androidx.room.Database
import androidx.room.RoomDatabase
import com.dmitrysukhov.lifetracker.TodoItem

@Database(entities = [TodoItem::class], version = 1)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}