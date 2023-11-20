package ph.edu.auf.realmdiscussionbarebones.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import ph.edu.auf.realmdiscussionbarebones.RealmDiscussionApplication
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
        getPets()

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

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean { return false }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (position != RecyclerView.NO_POSITION && position < petList.size) {
                    val deletedPet: Pet = petList[viewHolder.adapterPosition]
                    petList.removeAt(viewHolder.adapterPosition)
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                    adapter.petAdapterCallback.deletePet(deletedPet.id)
                    Toast.makeText(this@PetsActivity, "The swiped pet has been deleted successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        }).attachToRecyclerView(binding.rvPets)
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
        newType: String,
        newOwnerName: String
    ) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("updatePetDetails"))
        scope.launch(Dispatchers.IO) {
            database.updatePet(pet, newPetName, newAge, newType, newOwnerName)
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


    private fun getPets() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllPets"))
        scope.launch(Dispatchers.IO) {
            val pets = database.getAllPets()
            petList = arrayListOf()
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
}