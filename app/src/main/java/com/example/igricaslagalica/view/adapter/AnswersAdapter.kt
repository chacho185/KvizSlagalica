package com.example.igricaslagalica.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Question
import com.example.igricaslagalica.model.questions

class AnswersAdapter(private var answers: List<Connection>,
                     private val onAnswerSelected: (Int) -> Unit
) : RecyclerView.Adapter<AnswersAdapter.AnswerViewHolder>() {
private var selectedPosition = -1
    var isInteractionEnabled = true
    inner class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val answerText: TextView = itemView.findViewById(R.id.answerText)
        init {
            itemView.setOnClickListener {
                if (isInteractionEnabled) {
                    onAnswerSelected(adapterPosition)
                    selectedPosition = adapterPosition
                    notifyDataSetChanged()
                }
            }
        }
        fun bind(answer: Connection, position: Int) {
            answerText.text = answer.answer

            // Set background color based on the selected position
            if (selectedPosition == position) {
                itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                itemView.setBackgroundColor(Color.WHITE) // Replace with the original color
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.answer_item, parent, false)
        return AnswerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return answers.size
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val answer = answers[position]
        holder.bind(answer, position)
    }
    fun updateData(newQuestions: List<Connection>) {
        this.answers = newQuestions
        notifyDataSetChanged()
    }

}
