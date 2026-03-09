package demo.hanwix.imageprocessrequestservice.exception

class DuplicateImageRequestException(imageUrl: String) :
    RuntimeException("Image process task already exists for: $imageUrl")
