/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.bindingtariffrulingfrontend.views

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.matchers.*

object ViewMatchers {

  private def actualContentWas(node: Element): String =
    if (node == null) {
      "Element did not exist"
    } else {
      s"\nActual content was:\n${node.html}\n"
    }

  private def actualContentWas(node: Elements): String =
    if (node == null) {
      "Elements did not exist"
    } else {
      s"\nActual content was:\n${node.html}\n"
    }

  class ContainElementWithIDMatcher(id: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementById(id) != null,
        s"Document did not contain element with ID {$id}\n${actualContentWas(left)}",
        s"Document contained an element with ID {$id}"
      )
  }

  class ContainElementWithAttribute(key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && !left.getElementsByAttributeValue(key, value).isEmpty,
        s"Document did not contain element with Attribute {$key=$value}\n${actualContentWas(left)}",
        s"Document contained an element with Attribute {$key=$value}"
      )
  }

  class ContainElementWithTagMatcher(tag: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && !left.getElementsByTag(tag).isEmpty,
        s"Document did not contain element with Tag {$tag}\n${actualContentWas(left)}",
        s"Document contained an element with Tag {$tag}"
      )
  }

  class ElementHasClassMatcher(clazz: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.classNames().contains(clazz),
        s"Element did not have class {$clazz}\n${actualContentWas(left)}",
        s"Element had class {$clazz}"
      )
  }

  class ElementContainsTextMatcher(content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.text().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(left)}",
        s"Element contained {$content}"
      )
  }

  class ElementContainsHtmlMatcher(content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.html().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(left)}",
        s"Element contained {$content}"
      )
  }

  class ElementContainsChildWithTextMatcher(tag: String, content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val elements = left.getElementsByTag(tag)
      MatchResult(
        left != null && elements.text().contains(content),
        s"Element did not contain text {$content}\n${actualContentWas(elements)}",
        s"Element contained text {$content}"
      )
    }
  }

  class ElementContainsChildWithAttributeMatcher(tag: String, key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementsByTag(tag).attr(key) == value,
        s"Element attribute {$key} had value {${left.attr(key)}}, expected {$value}",
        s"Element attribute {$key} had value {$value}"
      )
  }

  class ElementHasAttributeValueMatcher(key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.attr(key) == value,
        s"Element attribute {$key} had value {${left.attr(key)}}, expected {$value}",
        s"Element attribute {$key} had value {$value}"
      )
  }

  class ElementHasAttributeMatcher(key: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.hasAttr(key),
        s"Element didnt have attribute {$key}",
        s"Element had attribute {$key}"
      )
  }

  class ElementHasChildCountMatcher(count: Int) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.children().size() == count,
        s"Element had child count {${left.children().size()}}, expected {$count}",
        s"Element had child count {$count}"
      )
  }

  class ElementsHasSizeMatcher(size: Int) extends Matcher[Elements] {
    override def apply(left: Elements): MatchResult =
      MatchResult(
        left != null && left.size() == size,
        s"Elements had size {${left.size()}}, expected {$size}",
        s"Elements had size {$size}"
      )
  }

  class ElementTagMatcher(tag: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.tagName() == tag,
        s"Elements had tag {${left.tagName()}}, expected {$tag}",
        s"Elements had tag {$tag}"
      )
  }

  class ChildMatcherBuilder(tag: String) {
    def containingText(text: String)              = new ElementContainsChildWithTextMatcher(tag, text)
    def withAttribute(key: String, value: String) = new ElementContainsChildWithAttributeMatcher(tag, key, value)
  }

  def containElementWithID(id: String)                        = new ContainElementWithIDMatcher(id)
  def containElementWithAttribute(key: String, value: String) = new ContainElementWithAttribute(key, value)
  def containElementWithTag(tag: String)                      = new ContainElementWithTagMatcher(tag)
  def containElementWithClass(classz: String)                 = containElementWithAttribute("class", classz)
  def containText(text: String)                               = new ElementContainsTextMatcher(text)
  def haveClass(text: String)                                 = new ElementHasClassMatcher(text)
  def containHtml(text: String)                               = new ElementContainsHtmlMatcher(text)
  def haveSize(size: Int)                                     = new ElementsHasSizeMatcher(size)
  def haveAttribute(key: String, value: String)               = new ElementHasAttributeValueMatcher(key, value)
  def haveAttribute(key: String)                              = new ElementHasAttributeMatcher(key)
  def haveTag(tag: String)                                    = new ElementTagMatcher(tag)
  def haveChildCount(count: Int)                              = new ElementHasChildCountMatcher(count)
  def haveChild(tag: String)                                  = new ChildMatcherBuilder(tag)
}
