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
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import ph.edu.auf.realmdiscussionbarebones.R
import ph.edu.auf.realmdiscussionbarebones.adapters.PetAdapter
import ph.edu.auf.realmdiscussionbarebones.databinding.ActivityPetsBinding
import ph.edu.auf.realmdiscussionbarebones.dialogs.AddPetDialog
import ph.edu.auf.realmdiscussionbarebones.models.Pet
import ph.edu.auf.realmdiscussionbarebones.realm.RealmDatabase
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetRealm

class PetsActivity : AppCompatActivity() , AddPetDialog.RefreshDataInterface, PetAdapter.PetAdapterInterface {

    private lateinit var binding: ActivityPetsBinding
    private lateinit var petList: ArrayList<Pet>
    private lateinit var adapter: PetAdapter
    private var database = RealmDatabase()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        petList = arrayListOf()
        adapter = PetAdapter(petList,this,this)

        val layoutManager = LinearLayoutManager(this)
        binding.rvPets.layoutManager = layoutManager
        binding.rvPets.adapter = adapter

        binding.fab.setOnClickListener{
            val addPetDialog = AddPetDialog()
            addPetDialog.refreshDataCallback = this
            addPetDialog.show(supportFragmentManager,null)
        }

        binding.btnSearch.setOnClickListener{
            if(binding.edtSearch.text.toString().isEmpty()){
                binding.edtSearch.error = "Required"
                return@setOnClickListener
            }

            val coroutineContext = Job() + Dispatchers.IO
            val scope = CoroutineScope(coroutineContext + CoroutineName("SearchPets"))
            scope.launch(Dispatchers.IO) {
                val result = database.getPetsByName(binding.edtSearch.text.toString())
                val petList = arrayListOf<Pet>()
                petList.addAll(
                    result.map {
                        mapPet(it)
                    }
                )
                withContext(Dispatchers.Main) {
                    adapter.updatePetList(petList)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        //TODO: REALM DISCUSSION HERE
        getPets()
    }

    override fun refreshData() {
        //TODO: REALM DISCUSSION HERE
        getPets()
    }


    override fun deletePet(id: String) {
        //TODO: REALM DISCUSSION HERE
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("deletePet"))
        scope.launch(Dispatchers.IO) {
            database.deletePet(ObjectId(id))
        }

    }

    private fun mapPet(pet: PetRealm) : Pet {
        return Pet(
            id = pet.id.toHexString(),
            name = pet.name,
            petType = pet.petType,
            age = pet.age,
            ownerName = pet.owner?.name ?: ""
        )
    }

    private fun getPets() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllPets"))
        scope.launch(Dispatchers.IO) {
            val pets = database.getAllPets()
            val petList = arrayListOf<Pet>()

            petList.addAll(
                pets.map {
                    mapPet(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updatePetList(petList)
            }
        }
    }
}