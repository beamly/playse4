package com.beamly.playse4

import java.net.URI

final class RunbookUrl(val value: URI) extends AnyVal {
  override def toString = value.toString
}
