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
package io.cordite.networkmap.service

import net.corda.core.identity.CordaX500Name
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.AttributeTypeAndValue
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle

fun X500Name.toCordaX500Name(strictEV: Boolean): CordaX500Name {
  val attributesMap: Map<ASN1ObjectIdentifier, ASN1Encodable> = this.rdNs
    .flatMap { it.typesAndValues.asList() }
    .groupBy(AttributeTypeAndValue::getType, AttributeTypeAndValue::getValue)
    .mapValues {
      require(it.value.size == 1) { "Duplicate attribute ${it.key}" }
      it.value[0]
    }

  val cn = attributesMap[BCStyle.CN]?.toString()
  val ou = attributesMap[BCStyle.OU]?.toString()
  val st = attributesMap[BCStyle.ST]?.toString()
  val o = attributesMap[BCStyle.O]?.toString()
    ?: if (!strictEV) "$cn-web" else throw IllegalArgumentException("X500 name must have an Organisation: ${this}")
  val l = attributesMap[BCStyle.L]?.toString()
    ?: if (!strictEV) "Antarctica" else throw IllegalArgumentException("X500 name must have a Location: ${this}")
  val c = attributesMap[BCStyle.C]?.toString()
    ?: if (!strictEV) "AQ" else throw IllegalArgumentException("X500 name must have a Country: ${this}")

  return CordaX500Name(cn, ou, o, l, st, c)
}