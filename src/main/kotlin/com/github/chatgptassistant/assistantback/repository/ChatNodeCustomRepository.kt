package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.Chat
import com.github.chatgptassistant.assistantback.domain.ChatNode
import com.github.chatgptassistant.assistantback.domain.Message
import java.util.*

interface ChatNodeCustomRepository {

  /**
   * Fetches a subtree of chat nodes tree, starting from the node with id [currentNodeId] in chat with id [chatId]
   * and going up to [upperLimit] ancestors and down to [lowerLimit] descendants.
   *
   * @param chatId id of the chat
   * @param currentNodeId id of the node from which the subtree is fetched
   * @param upperLimit number of ancestors to fetch. If 0, then no ancestors are fetched.
   * @param lowerLimit number of descendants to fetch. If 0, then no descendants are fetched.
   * @return list of chat nodes in the subtree
   * @throws NoSuchElementException if the node with id [currentNodeId] is not found in chat with id [chatId]
   */
  suspend fun fetchSubTree(chatId: UUID, currentNodeId: UUID, upperLimit: Int = 0, lowerLimit: Int = 0): List<ChatNode>

  /**
   * Creates a new chat node in chat with id [chatId] with parent node with id [parentChatNodeId] and message [message].
   *
   * @param chat chat in which the new chat node is created
   * @param parentChatNodeId id of the parent chat node. If null, then the new chat node is a root node.
   * @param message message to be stored in the new chat node
   * @return the newly created chat node
   * @throws NoSuchElementException if the parent chat node with id [parentChatNodeId] is not found in chat with id [chatId]
   */
  suspend fun createChatNode(chat: Chat, parentChatNodeId: UUID?, message: Message): ChatNode

  /**
   * Deletes a chat node and all its descendants, and updates the parent node children list.
   * @param chatId id of the chat
   * @param chatNodeId id of the chat node to be deleted
   * @return the parent chat node of the deleted chat node
   */
  suspend fun deleteNodeAndDescendants(chatId: UUID, chatNodeId: UUID): ChatNode?
}
