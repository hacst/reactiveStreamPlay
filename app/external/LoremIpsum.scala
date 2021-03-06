/**
Copyright (C) 2010 Oliver C Dodd

Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
package external
*/

//Source: https://github.com/oliverdodd/sclorem
package external

import scala.util.Random

object LoremIpsum {
  private val standard = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
  private val lipsumwords = Array(
    "a", "ac", "accumsan", "ad", "adipiscing", "aenean", "aliquam", "aliquet",
    "amet", "ante", "aptent", "arcu", "at", "auctor", "augue", "bibendum",
    "blandit", "class", "commodo", "condimentum", "congue", "consectetur",
    "consequat", "conubia", "convallis", "cras", "cubilia", "cum", "curabitur",
    "curae", "cursus", "dapibus", "diam", "dictum", "dictumst", "dignissim",
    "dis", "dolor", "donec", "dui", "duis", "egestas", "eget", "eleifend",
    "elementum", "elit", "enim", "erat", "eros", "est", "et", "etiam", "eu",
    "euismod", "facilisi", "facilisis", "fames", "faucibus", "felis",
    "fermentum", "feugiat", "fringilla", "fusce", "gravida", "habitant",
    "habitasse", "hac", "hendrerit", "himenaeos", "iaculis", "id", "imperdiet",
    "in", "inceptos", "integer", "interdum", "ipsum", "justo", "lacinia",
    "lacus", "laoreet", "lectus", "leo", "libero", "ligula", "litora",
    "lobortis", "lorem", "luctus", "maecenas", "magna", "magnis", "malesuada",
    "massa", "mattis", "mauris", "metus", "mi", "molestie", "mollis", "montes",
    "morbi", "mus", "nam", "nascetur", "natoque", "nec", "neque", "netus",
    "nibh", "nisi", "nisl", "non", "nostra", "nulla", "nullam", "nunc", "odio",
    "orci", "ornare", "parturient","pellentesque", "penatibus", "per",
    "pharetra", "phasellus", "placerat", "platea", "porta", "porttitor",
    "posuere", "potenti", "praesent", "pretium", "primis", "proin", "pulvinar",
    "purus", "quam", "quis", "quisque", "rhoncus", "ridiculus", "risus",
    "rutrum", "sagittis", "sapien", "scelerisque", "sed", "sem", "semper",
    "senectus", "sit", "sociis", "sociosqu", "sodales", "sollicitudin",
    "suscipit", "suspendisse", "taciti", "tellus", "tempor", "tempus",
    "tincidunt", "torquent", "tortor", "tristique", "turpis", "ullamcorper",
    "ultrices", "ultricies", "urna", "ut", "varius", "vehicula", "vel", "velit",
    "venenatis", "vestibulum", "vitae", "vivamus", "viverra", "volutpat",
    "vulputate")
  private val punctuation = Array(".", "?")
  private val _n = System.getProperty("line.separator")
  private val random = new Random

  def randomWord:String = lipsumwords(random.nextInt(lipsumwords.length))

  def randomPunctuation:String = punctuation(random.nextInt(punctuation.length))

  def words(count:Int):String =
    if (count > 0) (randomWord + " " + words(count - 1)).trim() else ""

  def sentenceFragment:String = words(random.nextInt(10) + 3)

  def sentence:String = {
    var s = new StringBuilder(randomWord.capitalize).append(" ")
    if (random.nextBoolean) {
      (0 to random.nextInt(3)).foreach({
        s.append(sentenceFragment).append(", ")
      })
    }
    s.append(sentenceFragment).append(randomPunctuation).toString
  }

  def sentences(count:Int):String =
    if (count > 0) (sentence + "  " + sentences(count - 1)).trim() else ""

  def paragraph(useStandard:Boolean = false) =
    if (useStandard) standard else sentences(random.nextInt(3) + 2)

  def paragraph:String = paragraph(false)

  def paragraphs(count: Int, useStandard:Boolean = false):String =
    if (count > 0) (paragraph(useStandard) + _n + _n + paragraphs(count - 1)).trim() else ""

}