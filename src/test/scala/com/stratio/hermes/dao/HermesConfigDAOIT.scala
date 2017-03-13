///*
// * Copyright (C) 2016 Stratio (http://stratio.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *         http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.stratio.hermes.dao
//
//import com.stratio.hermes.exceptions.HermesException
//import org.junit.runner.RunWith
//import org.scalatest.junit.JUnitRunner
//import org.scalatest.{FlatSpec, Matchers}
//
//@RunWith(classOf[JUnitRunner])
//class HermesConfigDAOIT extends FlatSpec with Matchers{
//  val hermesConfigDAO = new ZookeeperConfigDAO
//
//  "An HermesConfig" should "be save in zookeeper path and could be loaded" in {
//    hermesConfigDAO.saveConfig("testZkPath","myConfig")
//    val data = hermesConfigDAO.loadConfig("testZkPath")
//    hermesConfigDAO.removeConfig("stratio")
//    data shouldBe "myConfig"
//  }
//  "An HermesConfig" should "be updated when we save over an existing config" in {
//    hermesConfigDAO.saveConfig("testZkPath","myConfig")
//    hermesConfigDAO.saveConfig("testZkPath","myConfig2")
//    val data = hermesConfigDAO.loadConfig("testZkPath")
//    hermesConfigDAO.removeConfig("stratio")
//    data shouldBe "myConfig2"
//  }
//  it should "raise an exception when it save or load a config in a path that does not exists" in {
//    an[HermesException] should be thrownBy hermesConfigDAO.loadConfig("")
//    an[HermesException] should be thrownBy hermesConfigDAO.saveConfig("","config")
//  }
//}
