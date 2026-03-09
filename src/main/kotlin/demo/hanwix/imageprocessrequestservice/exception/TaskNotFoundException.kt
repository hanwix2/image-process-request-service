package demo.hanwix.imageprocessrequestservice.exception

class TaskNotFoundException(id: Long) : RuntimeException("Task not found: $id")
