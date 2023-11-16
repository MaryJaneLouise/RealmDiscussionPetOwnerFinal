package ph.edu.auf.realmdiscussionbarebones.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.auf.realmdiscussionbarebones.R
import ph.edu.auf.realmdiscussionbarebones.adapters.OwnerAdapter
import ph.edu.auf.realmdiscussionbarebones.databinding.ActivityOwnersBinding
import ph.edu.auf.realmdiscussionbarebones.models.Owner
import ph.edu.auf.realmdiscussionbarebones.models.Pet
import ph.edu.auf.realmdiscussionbarebones.realm.RealmDatabase
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.OwnerRealm
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetRealm

class OwnersActivity : AppCompatActivity() {
    private lateinit var binding : ActivityOwnersBinding
    private lateinit var adapter: OwnerAdapter
    private lateinit var ownerList: ArrayList<Owner>
    private var database = RealmDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ownerList = arrayListOf()
        adapter = OwnerAdapter(ownerList)

        val layoutManger = LinearLayoutManager(this)
        binding.rvOwner.layoutManager = layoutManger
        binding.rvOwner.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        getOwners()
    }

    //TODO: REALM DISCUSSION HERE
    private fun mapOwner(owner: OwnerRealm): Owner {
        return Owner(
            id = owner.id.toHexString(),
            name = owner.name,
            petCount = owner.pets.size
        )
    }

    private fun getOwners() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllOwners"))
        scope.launch(Dispatchers.IO) {
            val owners = database.getAllOwners()
            val ownerList = arrayListOf<Owner>()

            ownerList.addAll(
                owners.map {
                    mapOwner(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateList(ownerList)
            }
        }
    }

}