package com.github.chatgptassistant.assistantback.repository

import com.github.chatgptassistant.assistantback.domain.Chat
import com.github.chatgptassistant.assistantback.domain.ChatNode
import com.github.chatgptassistant.assistantback.domain.Message
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ChatNodeCustomRepositoryImpl(
  val mongoTemplate: ReactiveMongoTemplate,
) : ChatNodeCustomRepository {

  override suspend fun fetchSubTree(
    chatId: UUID,
    currentNodeId: UUID,
    upperLimit: Int,
    lowerLimit: Int
  ): Flow<ChatNode> {
    val currentNode = findByIdAndChatId(currentNodeId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    val ancestors = findAncestors(currentNode, upperLimit).toList()
    val descendants = findDescendants(currentNode, lowerLimit).toList()

    return (ancestors + currentNode + descendants).asFlow()
  }

  override suspend fun createChatNode(chat: Chat, parentChatNodeId: UUID?, message: Message): Pair<ChatNode?, ChatNode> {
    var parentNode: ChatNode? = parentChatNodeId?.let {
      findByIdAndChatId(it, chat.id)
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
      val children = parentNode.children + message.id
      parentNode = save(parentNode.copy(children = children))
    }

    return parentNode to save(newChatNode)
  }

  override suspend fun deleteNodeAndDescendants(chatId: UUID, chatNodeId: UUID): ChatNode? {
    val currentNode = findByIdAndChatId(chatNodeId, chatId)
      ?: throw NoSuchElementException("ChatNode not found")

    findDescendants(currentNode, Int.MAX_VALUE)
      .map { it.id }
      .toList()
      .let { deleteAllById(it) }

    delete(currentNode)

    return currentNode.parent?.let { parentId ->
      val parentNode = findByIdAndChatId(parentId, chatId)
        ?: throw NoSuchElementException("Parent ChatNode not found")

      return save(parentNode.copy(children = parentNode.children.filter { it != chatNodeId }))
    }
  }

  private fun findAncestors(node: ChatNode, upperLimit: Int): Flow<ChatNode> {
    val ancestorsToFetch = node.ancestors.takeLast(upperLimit)
    return findAllById(ancestorsToFetch, Sort.by("message.createTime"))
  }

  private fun findDescendants(node: ChatNode, lowerLimit: Int): Flow<ChatNode> {
    if (lowerLimit == 0) {
      return emptyList<ChatNode>().asFlow()
    }

    return findAllByChatIdAndAncestorsContaining(
      chatId = node.chatId,
      ancestorId = node.id,
      Sort.by("message.createTime")
    )
  }

  fun findAllByChatIdAndAncestorsContaining(chatId: UUID, ancestorId: UUID, sort: Sort): Flow<ChatNode> {
    val query = Query().addCriteria(
      Criteria().andOperator(
        where("chatId").isEqualTo(chatId),
        where("ancestors").isEqualTo(ancestorId)
      )
    ).with(sort)

    return mongoTemplate.find(query, ChatNode::class.java).asFlow()
  }

  private fun findAllById(ids: List<UUID>, sort: Sort): Flow<ChatNode> {
    val query = Query()
      .addCriteria(
        where("_id").`in`(ids.map { it })
      )
      .with(sort)

    return mongoTemplate.find(query, ChatNode::class.java).asFlow()
  }

  private suspend fun findByIdAndChatId(currentNodeId: UUID, chatId: UUID): ChatNode? {
    val query = Query().addCriteria(
      Criteria().andOperator(
        where("_id").isEqualTo(currentNodeId),
        where("chatId").isEqualTo(chatId)
      )
    )

    return mongoTemplate.find(query, ChatNode::class.java)
      .asFlow()
      .firstOrNull()
  }

  private suspend fun save(chatNode: ChatNode): ChatNode =
    mongoTemplate.save(chatNode)
      .asFlow()
      .first()

  private suspend fun delete(currentNode: ChatNode) {
    mongoTemplate.remove(currentNode).asFlow().first()
  }

  private suspend fun deleteAllById(ids: List<UUID>) {
    val query = Query()
      .addCriteria(
        where("_id").`in`(ids.map { it })
      )

    mongoTemplate.remove(query, ChatNode::class.java)
      .asFlow()
      .collect()
  }
}
