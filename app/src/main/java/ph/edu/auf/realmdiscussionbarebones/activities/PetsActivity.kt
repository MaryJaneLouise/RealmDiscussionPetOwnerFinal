package ph.edu.auf.realmdiscussionbarebones.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import ph.edu.auf.realmdiscussionbarebones.adapters.PetAdapter
import ph.edu.auf.realmdiscussionbarebones.databinding.ActivityPetsBinding
import ph.edu.auf.realmdiscussionbarebones.dialogs.AddPetDialog
import ph.edu.auf.realmdiscussionbarebones.models.Pet
import ph.edu.auf.realmdiscussionbarebones.models.PetType
import ph.edu.auf.realmdiscussionbarebones.realm.RealmDatabase
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetRealm
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetTypeRealm

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
        adapter = PetAdapter(petList,this, this)

        val layoutManager = LinearLayoutManager(this)
        binding.rvPets.layoutManager = layoutManager
        binding.rvPets.adapter = adapter

        binding.fab.setOnClickListener{
            val addPetDialog = AddPetDialog()
            addPetDialog.refreshDataCallback = this
            addPetDialog.show(supportFragmentManager,null)
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("SearchPets"))
                scope.launch(Dispatchers.IO) {
                    val result = database.getPetsByName(binding.edtSearch.text.toString().lowercase())
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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Nothing to do
            }
        })
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
            getPets()
        }
    }

    override fun updateOwnerForPet(pet: Pet, ownerName: String) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("addOwnerToPet"))
        scope.launch(Dispatchers.IO) {
            database.updateOwnerForPet(pet, ownerName)
            getPets()
        }
    }

    override fun updatePet(
        pet: Pet,
        newPetName: String,
        newAge: Int,
        newOwnerName: String
    ) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("updatePetDetails"))
        scope.launch(Dispatchers.IO) {
            database.updatePet(pet, newPetName, newAge, newOwnerName)
            getPets()
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

    private fun mapPetType(petType: PetTypeRealm) : PetType {
        return PetType(
            id = petType.id.toHexString(),
            petType = petType.petType,
            type = petType.type
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
                if (petList.isEmpty()) {
                    binding.rvPets.visibility = View.GONE
                    binding.txtNoPetsAvailable.visibility = View.VISIBLE
                } else {
                    binding.txtNoPetsAvailable.visibility = View.GONE
                    binding.rvPets.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getPetTypes() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllPetType"))
        scope.launch(Dispatchers.IO) {
            val petType = database.getAllPetTypes()
            val petTypeList = arrayListOf<PetType>()

            petTypeList.addAll(
                petType.map {
                    mapPetType(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updatePetList(petList)
            }
        }
    }
}