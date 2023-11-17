package ph.edu.auf.realmdiscussionbarebones.adapters

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import ph.edu.auf.realmdiscussionbarebones.databinding.ContentOwnerRvBinding
import ph.edu.auf.realmdiscussionbarebones.models.Owner

class OwnerAdapter(private var ownerList: ArrayList<Owner>, private var context: Context, private var ownerAdapterCallback: OwnerAdapterInterface) : RecyclerView.Adapter<OwnerAdapter.OwnerViewHolder>() {

    interface OwnerAdapterInterface {
        fun deleteOwner(id: String)

        fun updateOwner(owner: Owner, newName: String)
    }
    inner class OwnerViewHolder(private val binding: ContentOwnerRvBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(itemData: Owner){
            with(binding){
                if (itemData.pets.isNotEmpty()) {
                    txtOwnerName.text = String.format("Owner name: %s",itemData.name)
                    if (itemData.petCount > 1) {
                        txtNumPets.text = String.format("%s owned pets", itemData.petCount)
                    } else {
                        txtNumPets.text = String.format("%s owned pet", itemData.petCount)
                    }
                    txtNamePets.text = String.format("Pet List: \n• %s", itemData.pets.map { it.name }.joinToString("\n• "))
                } else {
                    txtOwnerName.text = String.format("Owner name: %s",itemData.name)
                    txtNumPets.text = String.format("No owned pets as of now")
                    txtNamePets.visibility = View.GONE
                }

                btnRemove.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Are you sure you want to delete this owner?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes") { dialog, _ ->
                        ownerList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        ownerAdapterCallback.deleteOwner(itemData.id)
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                btnEditOwner.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Edit details for ${itemData.name}")

                    val input = EditText(context)
                    input.inputType = InputType.TYPE_CLASS_TEXT
                    input.setText("${itemData.name}")
                    builder.setView(input)

                    builder.setPositiveButton("Update details") { dialog, _ ->
                        val newOwnerName = input.text.toString()
                        if (newOwnerName.isNotEmpty()) {
                            ownerAdapterCallback.updateOwner(itemData, newOwnerName)
                            dialog.dismiss()
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