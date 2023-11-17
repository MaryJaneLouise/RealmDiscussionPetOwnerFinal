package ph.edu.auf.realmdiscussionbarebones

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.recyclerview.widget.RecyclerView

class RealmDiscussionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context : Context
            private set

        @SuppressLint("StaticFieldLeak")
        lateinit var petRecyclerView: RecyclerView
            private set
    }
}