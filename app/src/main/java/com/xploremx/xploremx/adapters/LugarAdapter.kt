package com.xploremx.xploremx.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xploremx.xploremx.R
import com.xploremx.xploremx.models.Lugar

class LugarAdapter(
    private val context: Context,
    private val lugares: List<Lugar>,
    private val onItemClick: (Lugar) -> Unit
) : RecyclerView.Adapter<LugarAdapter.LugarViewHolder>() {

    inner class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgLugar: ImageView = itemView.findViewById(R.id.imgLugar)
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtDireccion: TextView = itemView.findViewById(R.id.txtDireccion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_lugar, parent, false)
        return LugarViewHolder(view)
    }

    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {
        val lugar = lugares[position]
        holder.txtNombre.text = lugar.nombre
        holder.txtDireccion.text = lugar.direccion

        Glide.with(context)
            .load("http://192.168.100.56/xploremx/imagenes/${lugar.imagenUrl}")
            .placeholder(R.mipmap.ic_launcher)
            .into(holder.imgLugar)

        holder.itemView.setOnClickListener {
            onItemClick(lugar)
        }
    }

    override fun getItemCount() = lugares.size
}