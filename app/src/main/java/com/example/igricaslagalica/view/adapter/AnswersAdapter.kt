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
    inner class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val answerText: TextView = itemView.findViewById(R.id.answerText)
        fun bind(answer: Connection, position: Int) {
            answerText.text = answer.answer

            itemView.setOnClickListener {
                onAnswerSelected(adapterPosition)

                // If a different item was selected, refresh the old item (to change its color back)
                if (selectedPosition != position && selectedPosition != -1) {
                    notifyItemChanged(selectedPosition)
                }

                // Set the selected position to this item
                selectedPosition = position

                // Change the item's color
                itemView.setBackgroundColor(Color.LTGRAY)
            }

            // Reset the item's color if it's not selected
            if (selectedPosition != position) {
                itemView.setBackgroundColor(Color.WHITE) // replace with the original color
            }
            // Change the background color if the question is answered
//            if (answer.answered) {
//                itemView.setBackgroundColor(Color.BLUE)
//            }
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
