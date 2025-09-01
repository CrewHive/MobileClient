data class EventDTO(
    val eventId: Long,
    val eventName: String,
    val description: String?,
    val start: String,   // ISO datetime
    val end: String,     // ISO datetime
    val color: String?,  // es. "#64B5F6"
    val eventType: String?, // "PUBLIC" | "PRIVATE"
    val users: List<EventUserDTO>? // opzionale
)

data class EventUserDTO(
    val id: EventUsersIdDTO?,
    val user: UserDTO?
)

data class EventUsersIdDTO(val userId: Long?, val eventId: Long?)
data class UserDTO(val userId: Long?, val username: String?)
