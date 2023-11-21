package ph.edu.auf.realmdiscussionbarebones.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import ph.edu.auf.realmdiscussionbarebones.R
import ph.edu.auf.realmdiscussionbarebones.adapters.OwnerAdapter
import ph.edu.auf.realmdiscussionbarebones.adapters.PetAdapter
import ph.edu.auf.realmdiscussionbarebones.databinding.ActivityOwnersBinding
import ph.edu.auf.realmdiscussionbarebones.models.Owner
import ph.edu.auf.realmdiscussionbarebones.models.Pet
import ph.edu.auf.realmdiscussionbarebones.realm.RealmDatabase
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.OwnerRealm
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetRealm

class OwnersActivity : AppCompatActivity(), OwnerAdapter.OwnerAdapterInterface {
    private lateinit var binding : ActivityOwnersBinding
    private lateinit var adapter: OwnerAdapter
    private lateinit var ownerList: ArrayList<Owner>
    private var database = RealmDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ownerList = arrayListOf()
        adapter = OwnerAdapter(ownerList, this, this)

        val layoutManger = LinearLayoutManager(this)
        binding.rvOwner.layoutManager = layoutManger
        binding.rvOwner.adapter = adapter

        binding.edtSearchOwner.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("SearchAuthors"))
                scope.launch(Dispatchers.IO) {
                    val result = database.getOwnerByName(binding.edtSearchOwner.text.toString().lowercase())
                    ownerList = arrayListOf()
                    ownerList.addAll(
                        result.map {
                            mapOwner(it)
                        }
                    )
                    withContext(Dispatchers.Main) {
                        adapter.updateList(ownerList)
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

                if (position != RecyclerView.NO_POSITION && position < ownerList.size) {
                    val builder = AlertDialog.Builder(this@OwnersActivity)
                    builder.setMessage("Are you sure you want to delete this owner?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        val deletedOwner: Owner = ownerList[viewHolder.adapterPosition]
                        ownerList.removeAt(viewHolder.adapterPosition)
                        adapter.notifyItemRemoved(viewHolder.adapterPosition)
                        adapter.ownerAdapterCallback.deleteOwner(deletedOwner.id)
                        Toast.makeText(this@OwnersActivity, "The swiped owner has been deleted successfully!", Toast.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                        dialog.dismiss()
                    }
                    builder.show()
                }
            }
        }).attachToRecyclerView(binding.rvOwner)

    }

    override fun onResume() {
        super.onResume()
        getOwners()
    }

    //TODO: REALM DISCUSSION HERE
    override fun deleteOwner(id: String) {
        //TODO: REALM DISCUSSION HERE
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("deleteOwner"))
        scope.launch(Dispatchers.IO) {
            database.deleteOwner(BsonObjectId(id))
            getOwners()
        }
    }

    private fun mapOwner(owner: OwnerRealm): Owner {
        return Owner(
            id = owner.id.toHexString(),
            name = owner.name,
            petCount = owner.pets.size,
            pets = owner.pets.map { mapPet(it) }
        )
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

    override fun updateOwner(owner: Owner, newName: String) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("updateOwner"))
        scope.launch(Dispatchers.IO) {
            database.updateOwner(owner, newName)
            getOwners()
        }
    }

    private fun getOwners() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllOwners"))
        scope.launch(Dispatchers.IO) {
            val owners = database.getAllOwners()
            ownerList = arrayListOf<Owner>()

            ownerList.addAll(
                owners.map {
                    mapOwner(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateList(ownerList)

                if (ownerList.isEmpty()) {
                    binding.txtNoOwnersAvailable.visibility = View.VISIBLE
                    binding.rvOwner.visibility = View.GONE
                } else {
                    binding.txtNoOwnersAvailable.visibility = View.GONE
                    binding.rvOwner.visibility = View.VISIBLE
                }
            }
        }
    }

}