package it.unibo.dcs.service.webapp.verticles

private[verticles] object Addresses {

  object rooms {

    private val prefix = "rooms"

    val created = s"$prefix.created"

    val deleted = s"$prefix.deleted"

    val joined = s"$prefix.joined"

    val left = s"$prefix.left"

  }

  object messages {

    private val prefix = "messages"

    val sent = s"$prefix.sent"

  }

  object users {

    private val prefix = "users"

    val offline = s"$prefix.offline"

    val online = s"$prefix.online"

    val typing = s"$prefix.typing"

    val typingInRoom = s"$prefix.typing.*"

    object hearthbeat {

      private val prefix = s"${users.prefix}.heartbeat"

      val request = s"$prefix.request"

      val response = s"$prefix.response"

    }

  }

  object internal {

    private val prefix = "internal"

    val userOffline = s"$prefix.userOffline"

    object isUserOnline {

      val prefix = s"${internal.prefix}.isUserOnline"

      val request = s"$prefix.request"

      val response = s"$prefix.response"

    }

  }

}
