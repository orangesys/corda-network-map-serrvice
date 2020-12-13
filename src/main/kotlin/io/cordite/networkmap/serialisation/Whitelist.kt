/**
 *   Copyright 2018, Cordite Foundation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.cordite.networkmap.serialisation

import net.corda.core.node.services.AttachmentId

/**
 * Represents the contents of a whitelist set
 */
data class WhitelistSet(private val contents: Map<String, Set<AttachmentId>> = emptyMap()) : Map<String, Set<AttachmentId>> by contents {
  override fun toString() : String {
    return contents
      .map { (contract, attachmentId) ->
        val attachments = attachmentId.joinToString(",") { it.toString() }
        "$contract:$attachments"
      }
      .joinToString("\n")
  }

  operator fun plus(rhs: WhitelistSet) : WhitelistSet {
    return rhs.entries.fold(this.toMutableMap()) { acc, (className, attachmentIds) ->
      val lhsSet = acc.computeIfAbsent(className) { emptySet() }
      val newSet = lhsSet + attachmentIds
      acc[className] = newSet
      acc
    }.let { WhitelistSet(it) }
  }

  operator fun minus(contractKey: String) : WhitelistSet {
    val newContents = contents - contractKey
    return WhitelistSet(newContents)
  }

  fun toCordaWhitelist() : Map<String, List<AttachmentId>> {
    return contents.mapValues { (_, values) -> values.toList() }
  }
}

fun String.parseWhitelist() : WhitelistSet {
  return this.lines()
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .map { line ->
      val splits = line.split(':')
      check(splits.size == 2) { "line should have exactly two parts separate by ':' - $line "}
      splits[0] to splits[1]
    }.map { (className, rhs) ->
      val attachmentNames = rhs.split(',')
      className to attachmentNames.map { AttachmentId.parse(it) as AttachmentId }
    }.groupBy ({ (className, _) -> className }) { (_, attachments) -> attachments }
    .map { (className, listOfAttachmentList) ->
      val uniqueAttachments = listOfAttachmentList.fold(emptySet<AttachmentId>()) { acc, item -> acc + item}
      className to uniqueAttachments
    }.toMap().let { WhitelistSet(it) }
}

fun Map<String, List<AttachmentId>>.toWhitelistSet() : WhitelistSet {
  return this.map { (contract, attachments) -> contract to attachments.toSet() }.toMap().let { WhitelistSet(it) }
}