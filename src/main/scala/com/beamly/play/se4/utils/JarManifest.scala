package com.beamly.play.se4
package utils

import scala.collection.JavaConverters._
import java.net.JarURLConnection

object JarManifest {
  def fromClass(clazz: Class[_]) = {
    val pathName = clazz.getName.replaceAll("\\.", "/")
    for {
      resourceUrl  <- Option(clazz getResource s"/$pathName.class")
      urlConnection = resourceUrl.openConnection() if urlConnection.isInstanceOf[JarURLConnection]
      jarConnection = urlConnection.asInstanceOf[JarURLConnection]
      manifest     <- Option(jarConnection.getManifest)
    } yield
      manifest.getMainAttributes.asScala.map(kv => kv._1.toString -> kv._2.toString).toMap
  }
}
