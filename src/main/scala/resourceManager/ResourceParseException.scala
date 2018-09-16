package resourceManager

final case class ResourceParseException(private val message: String = "",
                                        private val cause: Throwable = None.orNull)
                                        extends Exception(message, cause)