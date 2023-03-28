import com.github.chatgptassistant.assistantback.domain.ChatNode
import org.springframework.context.ApplicationEvent
import java.util.*

data class AIModelResponseEvent(val chatId: UUID, val chatNode: ChatNode) : ApplicationEvent(chatNode)
