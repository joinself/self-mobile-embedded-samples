package com.joinself.sdk.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.models.AttestationRequest
import com.joinself.sdk.models.AttestationResponse
import com.joinself.sdk.models.ChatMessage
import com.joinself.sdk.models.Message
import com.joinself.sdk.models.VerificationResponse
import com.joinself.sdk.sample.signin.databinding.ConversationItemBinding
import kotlin.text.StringBuilder

class ConversationAdapter(val mySelfId: String): ListAdapter<Any, ConversationAdapter.ItemViewholder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewholder {
        val binding = ConversationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewholder(binding, mySelfId)
    }

    class ItemViewholder(val binding: ConversationItemBinding, val mySelfId: String) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Any) {
            if (item is Message) {
                binding.titleTextView.visibility = View.VISIBLE
                binding.subtitleTextView.visibility = View.VISIBLE
                binding.titleTextView.text = if (item.fromIdentifier() == mySelfId) "You" else item.fromIdentifier()
                if (item is ChatMessage) {
                    val msgBuilder = StringBuilder()
                    msgBuilder.append(item.message())
                    if (item.attachments().isNotEmpty()) {
                        val attString = item.attachments().map { "${it.name()} size: ${it.content().size} bytes" }.joinToString(", ")
                        msgBuilder.appendLine()
                        msgBuilder.append(attString)
                    }
                    binding.subtitleTextView.text = msgBuilder
                } else if (item is AttestationRequest) {
                    val factString = item.facts().map { it.name() }.joinToString(", ")
                    binding.subtitleTextView.text = "Fact Req: $factString"
                } else if (item is AttestationResponse) {
                    val factString = item.attestations().map { "${it.fact().name()}:${it.fact().value()}" }.joinToString("\n")
                    binding.subtitleTextView.text = "Fact Resp: ${item.status().name} \n$factString"
                } else if (item is VerificationResponse) {
                    val factString = item.attestations().map { "${it.fact().name()}:${it.fact().value()}" }.joinToString("\n")
                    binding.subtitleTextView.text = "Verification Resp: ${item.status().name} \n$factString"
                }
            } else if (item is Attestation) {
                binding.titleTextView.visibility = View.GONE
                binding.subtitleTextView.visibility = View.VISIBLE
                binding.subtitleTextView.text = "${item.fact().name()}:${item.fact().value()}"
            }
        }
    }

    override fun onBindViewHolder(holder: ItemViewholder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return false
        }
    }
}