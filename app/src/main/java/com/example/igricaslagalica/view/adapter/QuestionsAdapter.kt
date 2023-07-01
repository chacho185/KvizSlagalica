package com.example.igricaslagalica.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.model.Connection

class QuestionsAdapter(private var questions: List<Connection>,
                       private val onQuestionSelected: (Int) -> Unit
) : RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {
    private var selectedPosition = -1

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)
        fun bind(question: Connection, position: Int) {
            // itemView.questionText.text = question
            questionText.text = question.question

            itemView.setOnClickListener {
                onQuestionSelected(adapterPosition)

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


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.question_item, parent, false)
        return QuestionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        // Disable the item only if it has been answered correctly
//        holder.itemView.isEnabled = !(question.answered && question.correct)
//
//        // Change the color of the item if it has been answered
//        if (question.answered) {
//            if (question.correct) {
//                holder.itemView.setBackgroundColor(Color.GREEN)  // Or whatever color you want
//            } else {
//                holder.itemView.setBackgroundColor(Color.RED)  // Or whatever color you want
//            }
//        } else {
//            holder.itemView.setBackgroundColor(Color.WHITE) // Or whatever color you want
//        }
        holder.bind(question, position)
    }
    fun updateData(newQuestions: List<Connection>) {
        this.questions = newQuestions
        notifyDataSetChanged()
    }

}
