package ph.edu.auf.realmdiscussionbarebones.adapters

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ph.edu.auf.realmdiscussionbarebones.R
import ph.edu.auf.realmdiscussionbarebones.databinding.ContentOwnerRvBinding
import ph.edu.auf.realmdiscussionbarebones.models.Owner

class OwnerAdapter(private var ownerList: ArrayList<Owner>, private var context: Context, var ownerAdapterCallback: OwnerAdapterInterface) : RecyclerView.Adapter<OwnerAdapter.OwnerViewHolder>() {

    interface OwnerAdapterInterface {
        fun deleteOwner(id: String)

        fun updateOwner(owner: Owner, newName: String)
    }
    inner class OwnerViewHolder(private val binding: ContentOwnerRvBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(itemData: Owner){
            val ownerListSort = listOf(itemData)
            val sortedOwnerList = ownerListSort.sortedWith(compareBy({ it.pets.isEmpty() }, { it.name }))
            with(binding){
                // Checks if the owner has a pet or none
                if (itemData.pets.isNotEmpty()) {
                    txtOwnerName.text = String.format("%s",itemData.name)

                    // Checks if the count of pets by the owner is greater than 1
                    if (itemData.petCount > 1) {
                        txtNumPets.text = String.format("%s owned pets", itemData.petCount)
                    } else {
                        txtNumPets.text = String.format("%s owned pet", itemData.petCount)
                    }

                    // Checks if the age of the pet is greater than 1 or not
                    if (itemData.pets.map { it.age.toString().toInt() }.joinToString("") > "1") {
                        val petStrings = itemData.pets.map { pet -> "${pet.name} | ${pet.petType} | ${pet.age} years old" }
                        val petListString = petStrings.joinToString("\n• ")

                        txtNamePets.text = "Pet List: \n• $petListString"

                    } else {
                        val petStrings = itemData.pets.map { pet -> "${pet.name} | ${pet.petType} | ${pet.age} year old" }
                        val petListString = petStrings.joinToString("\n• ")

                        txtNamePets.text = "Pet List: \n• $petListString"
                    }
                } else {
                    txtOwnerName.text = String.format("%s",itemData.name)
                    txtNumPets.text = String.format("No owned pets as of now")
                    txtNamePets.visibility = View.GONE
                }

                // Removes the owner from the database
                btnRemove.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Are you sure you want to delete this owner?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        ownerList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        ownerAdapterCallback.deleteOwner(itemData.id)
                        Toast.makeText(context, "The selected owner has been deleted successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                // Edits the owner's details from the database
                btnEditOwner.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.dialog_edit_owner, null)

                    val input = view.findViewById<EditText>(R.id.dialog_edit_owner_name)
                    input.setText("${itemData.name}")

                    builder.setView(view)

                    builder.setPositiveButton("Update details") { dialog, _ ->
                        val newOwnerName = input.text.toString()
                        if (newOwnerName.isNotEmpty() && newOwnerName.isNotBlank()) {
                            ownerAdapterCallback.updateOwner(itemData, newOwnerName)
                            Toast.makeText(context, "The owner's details has been updated!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(context, "You cannot assign a blank name owner.", Toast.LENGTH_SHORT).show()
                            dialog.cancel()
                        }
                    }

                    builder.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }

                    builder.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerViewHolder {
        val binding = ContentOwnerRvBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OwnerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OwnerViewHolder, position: Int) {
        val ownerData = ownerList[position]
        holder.bind(ownerData)
    }

    override fun getItemCount(): Int {
        return ownerList.size
    }

    fun updateList(ownerList: ArrayList<Owner>){
        this.ownerList = arrayListOf()
        notifyDataSetChanged()
        this.ownerList = ownerList
        this.notifyItemInserted(this.ownerList.size)
    }

}