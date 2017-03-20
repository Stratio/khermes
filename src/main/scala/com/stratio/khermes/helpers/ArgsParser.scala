/*
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.khermes.helpers

class ArgsParser {
  def commandWord(line: String):String={
    line.split(" ").head
  }
  def parse(line: String): Map[String, List[String]] ={
    val splitWords = line.split("-").filter(x => x != "")
    val filterFirstWord= splitWords.drop(1)
    val options = filterFirstWord.map(x => x.split("\\W+"))
    val result = options.map(c => c.head -> c.tail.toList)
    result.toMap
  }
}