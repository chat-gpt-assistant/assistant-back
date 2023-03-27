package com.github.chatgptassistant.assistantback.service

import com.github.chatgptassistant.assistantback.domain.Chat
import com.github.chatgptassistant.assistantback.domain.ChatNode
import com.github.chatgptassistant.assistantback.domain.Message
import com.github.chatgptassistant.assistantback.repository.ChatNodeRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

//TODO: do we need an interface for this? Maybe we can turn it into CustomRepository?
@Service
class ChatNodeService(
  private val chatNodeRepository: ChatNodeRepository
) {

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
  fun fetchSubTree(
    chatId: UUID,
    currentNodeId: UUID,
    upperLimit: Int = 0,
    lowerLimit: Int = 0
  ): List<ChatNode> {
    val currentNode = chatNodeRepository.findByIdAndChatId(currentNodeId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    val ancestors = findAncestors(currentNode, upperLimit)
    val descendants = findDescendants(currentNode, lowerLimit)

    return ancestors + currentNode + descendants
  }

  /**
   * Creates a new chat node in chat with id [chatId] with parent node with id [parentChatNodeId] and message [message].
   *
   * @param chat chat in which the new chat node is created
   * @param parentChatNodeId id of the parent chat node. If null, then the new chat node is a root node.
   * @param message message to be stored in the new chat node
   * @return the newly created chat node
   * @throws NoSuchElementException if the parent chat node with id [parentChatNodeId] is not found in chat with id [chatId]
   */
  fun createChatNode(chat: Chat, parentChatNodeId: UUID?, message: Message): ChatNode {
    val parentNode: ChatNode? = parentChatNodeId?.let {
      chatNodeRepository.findByIdAndChatId(it, chat.id)
        ?: throw NoSuchElementException("Parent ChatNode with id $it not found in chat with id $chat.id")
    }

    val ancestors = parentNode?.ancestors?.toMutableList() ?: mutableListOf()
    parentNode?.id?.let { ancestors.add(it) }

    val newChatNode = ChatNode(
      id = message.id,
      userId = chat.userId,
      chatId = chat.id,
      parent = parentChatNodeId,
      ancestors = ancestors,
      children = emptyList(),
      message = message
    )

    if (parentNode != null) {
      val children = parentNode.children.toMutableList()
      children.add(message.id)
      chatNodeRepository.save(parentNode.copy(children = children))
    }

    return chatNodeRepository.save(newChatNode)
  }

  /**
   * Deletes a chat node and all its descendants, and updates the parent node children list.
   * @param chatId id of the chat
   * @param chatNodeId id of the chat node to be deleted
   * @return the parent chat node of the deleted chat node
   */
  fun deleteNodeAndDescendants(chatId: UUID, chatNodeId: UUID): ChatNode? {
    val currentNode = chatNodeRepository.findByIdAndChatId(chatNodeId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    findDescendants(currentNode, Int.MAX_VALUE)
      .map { it.id }
      .let { chatNodeRepository.deleteAllById(it) }

    chatNodeRepository.delete(currentNode)

    return currentNode.parent?.let { parentId ->
      val parentNode = chatNodeRepository.findByIdAndChatId(parentId, chatId)
        ?: throw NoSuchElementException("Parent ChatNode not found")

      chatNodeRepository.save(parentNode.copy(children = parentNode.children.filter { it != chatNodeId }))
      return parentNode
    }
  }

  private fun findAncestors(node: ChatNode, upperLimit: Int): List<ChatNode> {
    val ancestorsToFetch = node.ancestors.takeLast(upperLimit)
    return chatNodeRepository.findAllById(ancestorsToFetch, Sort.by("message.createTime"))
  }

  private fun findDescendants(node: ChatNode, lowerLimit: Int): List<ChatNode> {
    if (lowerLimit == 0) {
      return emptyList()
    }

    return chatNodeRepository.findAllByChatIdAndAncestorsContaining(
      chatId = node.chatId,
      ancestorId = node.id,
      Sort.by("message.createTime")
    )
  }

}

